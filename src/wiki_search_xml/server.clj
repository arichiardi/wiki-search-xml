(ns wiki-search-xml.server
  (:require [com.stuartsierra.component :as component]
            [ring.adapter.jetty :as jetty]))
 
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

;; run-jetty
;; (run-jetty handler options)

;; Start a Jetty webserver to serve the given handler according to the
;; supplied options:

;; :configurator   - a function called with the Jetty Server instance
;; :port           - the port to listen on (defaults to 80)
;; :host           - the hostname to listen on
;; :join?          - blocks the thread until server ends (defaults to true)
;; :daemon?        - use daemon threads (defaults to false)
;; :ssl?           - allow connections over HTTPS
;; :ssl-port       - the SSL port to listen on (defaults to 443, implies :ssl?)
;; :keystore       - the keystore to use for SSL connections
;; :key-password   - the password to the keystore
;; :truststore     - a truststore to use for SSL connections
;; :trust-password - the password to the truststore
;; :max-threads    - the maximum number of threads to use (default 50)
;; :min-threads    - the minimum number of threads to use (default 8)
;; :max-queued     - the maximum number of requests to queue (default unbounded)
;; :max-idle-time  - the maximum idle time in milliseconds for a connection (default 200000)
;; :client-auth    - SSL client certificate authenticate, may be set to :need,
;;                   :want or :none (defaults to :none)
