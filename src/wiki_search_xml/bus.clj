(ns wiki-search-xml.bus
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [chan buffer close! pub sub unsub unsub-all] :rename {chan async-chan}]
            [wiki-search-xml.common :as common]))

(defrecord Bus [ ;; conf
                bus-conf pub-type-parse-conf pub-type-data-conf
                ;; instance
                chan pub-type]
  component/Lifecycle
  (stop [this]
    (if chan
      (do (close! chan)
          (unsub-all pub-type)
          (-> this
              (assoc :chan nil)
              (assoc :pub-type nil)))
      this))

  (start [this]
    (if chan
      this
      (let [c (async-chan (common/conf->buffer bus-conf))]
        (-> this
            (assoc :pub-type (pub c :type
                                  #(condp = %1
                                     :parse (common/conf->buffer pub-type-parse-conf)
                                     :data (common/conf->buffer pub-type-data-conf)
                                     (buffer 1))))
            (assoc :chan c))))))

(defn new-bus [config]
  (map->Bus (:bus config)))

(defn subscribe
  "Subscribes chan to the input bus. Returns a channel which will
  receive messages that satisfy the topic"
  [bus topic ch]
  (sub (:pub-type bus) topic ch))

(defn unsubscribe
  "Unsubscribes chan from to the input bus."
  [bus topic ch]
  (unsub (:pub-type bus) topic ch))
