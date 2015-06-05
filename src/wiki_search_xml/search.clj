(ns wiki-search-xml.search
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.fetcher :refer [fetch]]))

(defprotocol
  Search
  "Contract for searching documents")


(defrecord Searcher [;; config
                     
                     ]
    )

(defn new-searcher
  "Creates a new Searcher."
  [config-map]
  (component/using (map->Searcher (:searcher config-map))
    {:fetcher :wsx-fetcher}))

(defn search-key
  "Performs the search, needs a Searcher and a key to look for."
  [searcher url key]
  (let [{:keys [fetcher]} searcher 
        response (fetch fetcher url)]

    
    ))
