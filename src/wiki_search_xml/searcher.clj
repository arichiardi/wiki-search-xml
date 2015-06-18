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

(defrecord Searcher [ ;; config?
                     data-buffer-conf locations
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
      (let [sq (subscribe bus :data (common/conf->buffer data-buffer-conf))
            component (assoc this :sub-data sq)]
        ;; (core/loop! (partial consume! component) sq)
        component))))

(defn new-searcher
  "Creates a new Searcher."
  [config-map]
  (component/using (map->Searcher (:searcher config-map))
    {:bus :wsx-bus}))

(defn reduce-search-results
  "Reduces the results in a map of list of :result and :error."
  [acc-result-maps result-map]
  (let [[results errors] acc-result-maps]
    [(conj results (:result result-map))
     (conj errors (:error result-map))]))

;; Cannot use alt! with subscribed channels???
;;http://dev.clojure.org/jira/browse/ASYNC-75?page=com.atlassian.jira.plugin.system.issuetabpanels:changehistory-tabpanel
;; (async/timeout timeout-ms) {:error (str "Search in location " location " timed out")}

(defn search-for
  "Performs the search, needs a Searcher and a key to look for.
  This method is synchronous, but it parks if cannot go on. Returns a
  vector of {:result ... :error} for the searched key."
  [this key]
  (let [{:keys [bus sub-data locations]} this]
    (async/go
      (doseq [loc locations]
        (do (log/debugf "dispatching msg(s) for key %s at location %s" key loc)
            (async/>!! (:chan bus) (core/map->Msg {:type :parse
                                                      :location loc
                                                      :for-key key}))))

      (log/debug "collecting results")
      (loop []
        (let [{:keys [class data error location] :as data} (async/<! sub-data)]
          (when-not data (log/warn "returned data on bus was nil, something might be wrong"))
          (log/debugf "received data of class %s for location %s (error: %s)" class location error)

          (if (= :parsed-xml class)
            (if data
              {:result (or (text/trie-get (parse/trie data) key) [])}
              {:error error})
            (recur)))))))


(defn consume!
  "Consumes the input message."
  [this msg]
  (case (:type msg)
    :query (search-for this msg)
    (log/warn "message is not for me")))

