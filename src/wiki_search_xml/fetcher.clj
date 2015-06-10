(ns wiki-search-xml.fetcher
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :refer [go-loop chan <! close!]]
            [com.stuartsierra.component :as component]
            [org.httpkit.client :as http]
            [wiki-search-xml.bus :refer [subscribe]]))

(declare process!)

(defprotocol Fetch
  "Fetches documents"
  (fetch [this url options]
    "Fetching method, returns a map containing the result of fetching the document"))

(defrecord Fetcher [ ;; config
                    static-options
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
        (subscribe bus :find c)
        
        (assoc this :subscription c)
        
        )))
  
  Fetch
  (fetch [this url options]
    (http/get url (merge options {:as :stream
                                  :start-time (System/currentTimeMillis)}))))

(defn new-fetcher [config]
  "Creates an instance of a document fetcher, it accepts a map of additional key/values to be added
  to the request map"
  (map->Fetcher (:fetcher config)))

(defn callback [{:keys [status headers body error opts]}]
  ;; opts contains :url :method :header + user defined key(s)
  (let [{:keys [method start-time url]} opts]
    (log/debug method url "status" status "took time"
               (- (System/currentTimeMillis) start-time) "ms")))

(defn process!
  "Main execution loop"
  [this]
  (go-loop []
    (when (:loop this) 
      (let [msg (<! (:subscription this))] 
        (log/debug "Message received: " msg)
        (condp :type msg
          :search (fetch this (concat (:search-key msg) (:abstract-path msg)) key))))))




