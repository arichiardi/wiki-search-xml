(ns wiki-search-xml.server
  (:require [com.stuartsierra.component :as component]
            [ring.adapter.jetty :as jetty]
            [wiki-search-xml.system :as sys]))
 
(defrecord Server [ ;; config
                   server-opts
                   ;; dependecies
                   handler logger
                   ;; instance
                   server]
  component/Lifecycle

  (stop [this]
    (if server
      (do (.stop server)
          (.join server)
          (dissoc this :server))
      this)) 

  (start [this]
    (if server
      this
      (let [options (-> server-opts (assoc :join? false))
            server  (jetty/run-jetty handler options)]
        (assoc this :server server)))))
  
(defn new-server
  [config-map]
  (map->Server {:server-opts config-map}))
