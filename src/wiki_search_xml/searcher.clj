(ns wiki-search-xml.searcher
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.bus :refer [subscribe]]
            [wiki-search-xml.core :as core]
            [wiki-search-xml.common :as common]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go go-loop chan <! >! close!]]))

(declare consume!)

(defrecord Searcher [ ;; config?
                     buffer-conf locations
                     ;; dependecies
                     bus
                     ;; state
                     subscription]
  component/Lifecycle
  (stop [this]
    (if subscription
      (let [component (dissoc this :subscription)]
        (close! subscription)
        (log/debug "stopped component" component))
      this))

  (start [this]
    (if subscription
      this
      (let [c (common/conf->buffer buffer-conf)
            subscription (subscribe bus :query c)
            component (assoc this :subscription subscription)]
        (core/loop! subscription (partial consume! component))
        (log/debug "started component" component)
        component))))

(defn new-searcher
  "Creates a new Searcher."
  [config-map]
  (component/using (map->Searcher (:searcher config-map))
    {:bus :wsx-bus}))

(defn search-for
  "Performs the search, needs a Searcher and a key to look for."
  [this msg]
  (let [{:keys [bus locations]} this
        {:keys [key]} msg]

    (doseq [loc locations]
      (do (log/debugf "searching for \"%s\" at location %s" key  loc)

          ;; (if ) ;; find in db

          ;; if not delegate to parse
          (go (>! (:chan bus) (core/map->Msg {:type :parse
                                              :location loc
                                              :for-key key})))))))

(defn consume!
  "Consumes the input message."
  [this msg]
  (log/debug "consuming" msg)
  (case (:type msg)
    :query (search-for this msg)
    (log/warn "message is not for me")))

