(ns wiki-search-xml.search
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.fetcher :refer [fetch]]))

(defprotocol
  Search
  "Contract for searching documents")


(defrecord Searcher [ ;; config?

                     ;; dependecies
                     logger bus fetcher]
  ;; component/Lifecycle
  ;; (stop [this]
    ;; (if 
      ;; unsubscribe from bus
     
      ;; this)
  ;; )
  
  ;; (start [this]
    ;; (if-not
      ;; subscribe to chan
      ;; )
;; )
)

(defn new-searcher
  "Creates a new Searcher."
  [config-map]
  (component/using (map->Searcher (:searcher config-map))
    {:fetcher :wsx-fetcher
     :logger :wsx-logger
     :bus :wsx-bus}))

(defn search-key
  "Performs the search, needs a Searcher and a key to look for."
  [searcher url key]
  (let [{:keys [fetcher]} searcher 
        response (fetch fetcher url)]

    
    ))
