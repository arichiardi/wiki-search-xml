(ns wiki-search-xml.searcher
  (:require [clojure.tools.trace :refer [deftrace trace] :rename {trace t}]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [wiki-search-xml.bus :refer [subscribe unsubscribe]]
            [wiki-search-xml.core :as core]
            [wiki-search-xml.text :as text]
            [wiki-search-xml.parse.impl :as parse]
            [wiki-search-xml.common :as common]))

(declare consume!)

(defrecord Searcher [;; config?
                     data-sub-conf parse-timeout locations
                     ;; dependecies
                     bus
                     ;; state
                     sub-data]
  component/Lifecycle
  (stop [this]
    (if sub-data
      (do (async/close! sub-data)
          (assoc this :sub-data nil))
      this))

  (start [this]
    (if sub-data
      this
      (let [ch (async/chan (common/conf->buffer data-sub-conf))
            sq (subscribe bus :data ch)
            component (assoc this :sub-data ch)]
        ;; (core/loop! (partial consume! component) sq)
        component))))

(defn new-searcher
  "Creates a new Searcher."
  [config-map]
  (component/using (map->Searcher (:searcher config-map))
    {:bus :wsx-bus}))


;;;;;;;;;;;;;;;;;;
;; Search logic ;;
;;;;;;;;;;;;;;;;;;

(defn- ^:testable
  merge-results
  "Reduces the results in a map of list of :result and :error."
  [acc-result-maps result-or-error]
  (merge-with concat acc-result-maps result-or-error))

(defn- parsed-msg->search-results
  [msg]
  (let [{:keys [class for-key data error location] :as data} msg]
    (when-not data (log/warn "returned data on bus was nil, something might be wrong"))
    (log/debugf "received data of class %s for location %s (error: %s)" class location error)

    (if data
      {:search-results (or (text/trie-get (parse/trie data) for-key) [])}
      {:search-errors error})))

(defn- parse-and-search-location!
  [dispatch-ch result-ch timeout key location]
  ;; Cannot use alt! with subscribed channels???
  ;;http://dev.clojure.org/jira/browse/ASYNC-75?page=com.atlassian.jira.plugin.system.issuetabpanels:changehistory-tabpanel
  ;; (async/timeout timeout-ms) {:error (str "Search in location " location " timed out")}
  (async/go (let [timeout-ch (async/timeout timeout)
                  [v c] (async/alts!
                         [(core/>!-dispatch-<!-apply! dispatch-ch result-ch
                                                      #(= :parsed-xml (:class %1))
                                                      parsed-msg->search-results
                                                      (core/map->Msg {:type :parse
                                                                      :location location
                                                                      :for-key key})) 
                          ] :priority true)]
              (when (= c timeout-ch)
                (log/warn "timed out parsing location" location))
              v)))

(defn search-for
  "Performs the search, needs a Searcher and a key to look for.
  This method is synchronous, but it parks if cannot go on. Returns a
  vector of {:result ... :error} for the searched key."
  [this key]
  (let [{:keys [bus sub-data locations parse-timeout]} this
        search-location! (partial parse-and-search-location!
                                  (:chan bus) sub-data (or parse-timeout 15000) ;; channels
                                  key)]
    ;; Dispatch, search parsed results and collect
    (async/reduce merge-results
                  {}
                  (async/merge (map search-location! locations)))))

(defn- consume!
  "Consumes the input message."
  [this msg]
  (case (:type msg)
    :query (search-for this msg)
    (log/warn "message is not for me")))


