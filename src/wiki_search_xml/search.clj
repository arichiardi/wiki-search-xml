(ns wiki-search-xml.search
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.bus :refer [subscribe]]
            [clojure.tools.logging :as log]
            [wiki-search-xml.fetcher :refer [fetch]]
            [clojure.core.async :refer [go chan <! close!]]))

(declare search-key)

(defrecord Searcher [ ;; config?
                     end-point
                     ;; dependecies
                     bus fetcher
                     ;;
                     subscription
                     ]
  component/Lifecycle
  (stop [this]
    ;; unsubscribe
    (if subscription 
      (do (close! subscription)
          (dissoc this :subscription))
      this))
  
  (start [this]
    (if subscription
      this
      (let [c (chan 1)]
        (subscribe bus :searcher c)
        (while true 
          (go (let [msg (<! c)] 
                (log/debug "Message received: " msg) 
                (search-key this (concat (:search-key msg) (:abstract-path msg)) key))))
        (assoc this :subscription c)))))

(defn new-searcher
  "Creates a new Searcher."
  [config-map]
  (component/using (map->Searcher (:searcher config-map))
    {:fetcher :wsx-fetcher
     :bus :wsx-bus}))

(defn search-key
  "Performs the search, needs a Searcher and a key to look for."
  [searcher path key]
  #_(log/debug "Starting searching " key " in " path)
  (let [{:keys [fetcher]} searcher
        
        ;; response (fetch fetcher url)
        ]

    
    ))
