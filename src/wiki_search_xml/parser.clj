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
  corresponds to parse/wiki-stream->trie-pair. Value hook modifies the
  value payload before insertion and is tipically used to assoc some
  db-specific fields."
  [value-hook fetch-channel]
  (async/thread (let [{:keys [stream error]} (async/<!! fetch-channel)]
                  (log/debugf "fetch-result ready (payload not displayed but error was: %s)" error)
                  (if stream 
                    {:data (parse/wiki-stream->trie-pair value-hook stream)}
                    {:error error}))))

(defn parse-location
  "Parses a location, returning a channel that will yield a
  core/msg->DataMsg with class :parsed-xml whose data is the result of
  either fetching or just returning the parsed document according to the
  location found in the input msg."
  [this msg]
  (let [{:keys [parsed-cache]} this
        {:keys [location]} msg]
    (log/debug "parsing location" location)

    (async/go
      (core/msg->DataMsg msg
                         (merge {:class :parsed-xml} 
                                (if-let [parsed (get @parsed-cache location)]
                                  {:data parsed}
                                  (let [parsed (async/<! (parse-stream-async identity (fetch location)))
                                        fp (second (:data parsed))]
                                    (log/debugf "ssssssssssss---  %s" (first fp))
                                    (when-let [data (:data parsed)]
                                      (swap! parsed-cache assoc location data))
                                    parsed)))))))

(defn consume!
  "Consumes the input message."
  [this msg]
  (let [bus (get-in this [:bus :chan] this)]
    (case (:type msg) 
      :parse (async/pipe (parse-location this msg) bus)
      (log/debug "message is not for me"))))
