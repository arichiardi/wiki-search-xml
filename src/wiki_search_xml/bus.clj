(ns wiki-search-xml.bus
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [wiki-search-xml.common :as common]
            [clojure.core.async :refer [chan go >! <! close! buffer pipe] :rename {chan new-chan}]))

(defrecord Bus [;; conf
                chan-size
                ;; instance
                chan]
  component/Lifecycle
  (stop [this]
    (if chan
      (do (close! chan)
          (dissoc this :chan :logger))
      this))
  
  (start [this]
    (if chan
      this
      (assoc this :chan (new-chan (or chan-size 1))))))

(defn new-bus [config]
  (map->Bus (:bus config)))

;; (defn- start-dispatch
;;   [bus]
;;   (go (while true
;;         ))
;;   )

(defn subscribe
  "Subscribes chan to the input bus. Returns a channel which will
  receive messages that satisfy the predicate pred"
  [bus pred]
  (let [chan (:chan bus)
        filtered-chan (new-chan (buffer 1) (filter pred))] 
    (pipe chan filtered-chan)))

