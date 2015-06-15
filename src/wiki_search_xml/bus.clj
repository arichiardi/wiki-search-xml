(ns wiki-search-xml.bus
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [chan close! pub sub] :as async]
            [wiki-search-xml.common :as common]))

(defrecord Bus [ ;; conf
                bus-conf pub-type-conf
                ;; instance
                chan pub-type]
  component/Lifecycle
  (stop [this]
    (if chan
      (do (async/close! chan)
          (-> this
              (dissoc :chan)
              (dissoc :pub-type)))
      this))

  (start [this]
    (if chan
      this
      (let [c (async/chan (common/conf->buffer bus-conf))]
        (-> this
            (assoc :pub-type (async/pub c
                                        :type
                                        (fn [_] (common/conf->buffer pub-type-conf))))
            (assoc :chan c))))))

(defn new-bus [config]
  (map->Bus (:bus config)))

(defn subscribe
  "Subscribes chan to the input bus. Returns a channel which will
  receive messages that satisfy the topic"
  [bus topic ch]
  (async/sub (:pub-type bus) topic ch))

