(ns wiki-search-xml.parser
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [wiki-search-xml.bus :refer [subscribe]]
            [wiki-search-xml.fetch :refer [fetch]]
            [wiki-search-xml.core :as core]
            [wiki-search-xml.common :as common]
            [wiki-search-xml.parse.impl :as parse]))

(declare consume!)

(defrecord Parser [;; config
                   buffer-conf
                   ;; dependecies
                   bus
                   ;; state
                   sub-parse parsed-locations]
  component/Lifecycle
  (stop [this]
    ;; unsubscribe
    (if sub-parse 
      (do (async/close! sub-parse)
          (-> this
              (dissoc :sub-parse)
              (dissoc :parsed-cache)))
      this))
  
  (start [this]
    (if sub-parse
      this
      (let [c (common/conf->buffer buffer-conf)
            sp (subscribe bus :parse c)
            component (-> this (assoc :sub-parse sp)
                               (assoc :parsed-cache (atom nil)))]
        (core/loop! (partial consume! component) sp)
        component))))
  
(defn new-parser [config]
  "Creates an instance of a document fetcher, it accepts a map of additional key/values to be added
  to the request map"
  (component/using (map->Parser (:parser config))
    {:bus :wsx-bus}))

;;;;;;;;;;;;;
;;; parse ;;;
;;;;;;;;;;;;;

(defn parse-stream-async
  "Asynchronously parse the FetchResult coming from channel.
  The output depends on the format of the parsing function, which now
  corresponds to parse/wiki-stream->trie."
  [fetch-channel]
  (async/thread (let [{:keys [stream error]} (async/<!! fetch-channel)]
                  (log/debug "fetch-result ready, stream:" stream "error:" error)
                  (if stream 
                    {:data (parse/wiki-stream->trie identity stream)}
                    {:error error}))))

(defn parse-location
  [this msg]
  (let [{:keys [bus parsed-cache]} this
        {:keys [chan]} bus
        {:keys [location for-key]} msg]
    (log/debug "parsing location" location)

    (async/go
      (async/>! chan
                (core/msg->result :parser msg 
                                  (if-let [parsed (get @parsed-cache location)]
                                    {:data parsed}
                                    (let [parsed (async/<! (parse-stream-async (fetch location)))]
                                      (when-let [data (:data parsed)]
                                        (swap! parsed-cache assoc location data))
                                      parsed)))))))

(defn consume!
  "Consumes the input message."
  [this msg]
  (case (:type msg) 
    :parse (parse-location this msg)
    (log/debug "message is not for me")))

