(ns wiki-search-xml.bus
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as a]))

(defrecord Bus [;; conf
                timeout buffer-size
                ;; deps
                logger
                ;; instance
                chan]
  component/Lifecycle
  (stop [this]
    (if chan
      (do (a/close! chan)
          (assoc this :chan nil))
      this))
  
  (start [this]
    (if-not chan
      (assoc this :chan (a/chan (or buffer-size 1)))
      this)))

(defn new-bus [config]
  (component/using (map->Bus (:bus config))
    {:logger :wsx-logger}))

