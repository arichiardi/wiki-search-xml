(ns wiki-search-xml.bus
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [wiki-search-xml.common :as common]
            [clojure.core.async :refer [chan go >! <! close! buffer pub sub] :rename {chan new-chan}]))

(defrecord Bus [;; conf
                chan-size
                ;; instance
                chan pub-chan]
  component/Lifecycle
  (stop [this]
    (if chan
      (do (close! chan)
          (-> (dissoc this :chan)
              (dissoc :pub-chan)))
      this))

  (start [this]
    (if chan
      this
      (let [c (new-chan (or chan-size 1))]
        (-> (assoc this :pub-chan (pub c :msg-to))
            (assoc :chan c))))))

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
  [bus topic ch]
  (sub (:pub-chan bus) topic ch))
