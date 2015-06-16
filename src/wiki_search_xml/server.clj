(ns wiki-search-xml.server
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :as httpkit]))


(defrecord Server [ ;; config
                   options
                   ;; dependecies
                   handler
                   ;; instance
                   server]
  component/Lifecycle

  (stop [this]
    (if server
      ;; http-kit run-server returns a stopping function
      (do (log/info "stopping server")
          (server :timeout 250)
          (assoc this :server nil))
      this)) 

  (start [this]
    (if server
      this
      (do (log/info "launching server with options" options) 
          (let [server (httpkit/run-server (:app handler) options)]
            (assoc this :server server))))))
  
(defn new-server
  [config-map]
  (component/using (map->Server (:server config-map))
    {:handler :wsx-handler}))


