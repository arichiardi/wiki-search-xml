(ns wiki-search-xml.searcher
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.bus :refer [subscribe]]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go-loop chan <! close!]]))

(declare listens!)

(defrecord Searcher [ ;; config?
                     end-point
                     ;; dependecies
                     bus fetcher
                     ;; state
                     subscription listens!]
  component/Lifecycle
  (stop [this]
    ;; unsubscribe
    (if subscription 
      (do (close! subscription)
          (-> this
              (dissoc :subscription)
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
  (log/debug "Starting searching " key " in " path)

  (let [{:keys [fetcher]} searcher])
  ;; (if ) ;; find in db

  ;; else fetch
  ;; (go (<! (fetch )))
  )

(defn listens!
  "Main execution loop"
  [this]
  (go-loop []
    (when (:listens! this) 
      (let [msg (<! (:subscription this))] 
        (log/debug "Message received: " msg)
        (condp :type msg
          :search (search-key this (concat (:search-key msg) (:abstract-path msg)) key))))))

