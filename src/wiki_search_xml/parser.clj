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
                   buffer-conf fetch-timeout
                   ;; dependecies
                   bus
                   ;; state
                   subscription parsed-locations]
  component/Lifecycle
  (stop [this]
    ;; unsubscribe
    (if subscription 
      (do (async/close! subscription)
          (-> this
              (dissoc :subscription)
              (dissoc :parsed-cache)))
      this))
  
  (start [this]
    (if subscription
      this
      (let [c (common/conf->buffer buffer-conf)
            subscription (subscribe bus :parse c)
            component (-> this (assoc :subscription subscription)
                               (assoc :parsed-cache (atom nil)))]
        (core/loop! subscription (partial consume! component))
        (log/debug "started component" component)
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
  "Asynchronously parse from the input coming from channel"
  [channel]
  (async/thread (let [{:keys [stream error]} (async/<!! channel)]
                  (log/debug "fetch-result ready, stream:" stream "error:" error)
                  (when stream 
                    (parse/xml-stream->trie identity stream)))))

(defn parse-location
  [this msg]
  (let [{:keys [bus locations parsed-cache fetch-timeout]} this
        {:keys [location]} msg]
    (log/debug "parsing location" location)
    
    (if-let [parsed (get @parsed-cache location)]
       parsed
       (async/go
         (async/alt!
           (async/timeout (or fetch-timeout 10000)) :timeout

           (parse-stream-async (fetch location)) :success
           :priority true
           )
         )
      )
    
    )
  )

(defn consume!
  "Consumes the input message."
  [this msg]
  (case (:type msg) 
    :parse (parse-location this msg)
    (log/debug "message is not for me")))
