(ns wiki-search-xml.search
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.fetcher :refer [fetch]]))

(defprotocol
  Search
  "Contract for searching documents")


(defrecord Searcher [ ;; config?

                     ;; dependecies
                     logger bus fetcher
                     ;;
                     subscription
                     ]
  component/Lifecycle
  (stop [this]
    ;; unsubscribe
    (if subscription 
      this
      this)
  )
  
  (start [this]
    (if subscription
      this
      this
      ;; subscribe
      )
)
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
  (let [{:keys [logger fetcher]} searcher 
        response (fetch fetcher url)]

    
    ))
