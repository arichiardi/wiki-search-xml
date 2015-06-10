(ns wiki-search-xml.search
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.bus :refer [subscribe]]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go-loop chan <! close!]]))

(declare process!)

(defrecord Searcher [ ;; config?
                     end-point
                     ;; dependecies
                     bus
                     ;; state
                     loop subscription process!]
  component/Lifecycle
  (stop [this]
    ;; unsubscribe
    (if subscription 
      (do (close! subscription)
          (-> this
              (dissoc :subscription)
              (dissoc :loop)
              (dissoc :process!)))
      this))
  
  (start [this]
    (if subscription
      this
      (let [c (chan 1)]
        (subscribe bus :search c)
        (assoc this :subscription c)
     
        ))))

(defn new-searcher
  "Creates a new Searcher."
  [config-map]
  (component/using (map->Searcher (:searcher config-map))
    {:fetcher :wsx-fetcher
     :bus :wsx-bus}))

(defn search-key
  "Performs the search, needs a Searcher and a key to look for."
  [searcher path key]
  (log/debug "Starting searching " key " in " path))

(defn process!
  "Main execution loop"
  [this]
  (go-loop []
    (when (:loop this) 
      (let [msg (<! (:subscription this))] 
        (log/debug "Message received: " msg)
        (condp :type msg
          :search (search-key this (concat (:search-key msg) (:abstract-path msg)) key))))))

