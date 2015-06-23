(ns wiki-search-xml.parser
  (:require [clojure.tools.trace :refer [deftrace trace] :rename {trace t}]
            [com.stuartsierra.component :as component]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [wiki-search-xml.bus :refer [subscribe]]
            [wiki-search-xml.fetch :refer [fetch]]
            [wiki-search-xml.core :as core]
            [wiki-search-xml.common :as common]
            [wiki-search-xml.parse.impl :as p]))

(declare consume!)

(defrecord Parser [;; config
                   parse-sub-conf
                   ;; dependecies
                   bus
                   ;; state
                   sub-parse parse-data-cache]
  component/Lifecycle
  (stop [this]
    ;; unsubscribe
    (if sub-parse
      (do (async/close! sub-parse)
          (-> this
              (assoc :sub-parse nil)
              (assoc :parse-data-cache nil)))
      this))

  (start [this]
    (if sub-parse
      this
      (let [c (async/chan (common/conf->buffer parse-sub-conf))
            sp (subscribe bus :parse c)
            component (-> this (assoc :sub-parse sp)
                               (assoc :parse-data-cache (atom nil)))]
        (core/<!-do-loop! sp (partial consume! component))
        component))))

(defn new-parser [config]
  "Creates an instance of a document fetcher, it accepts a map of additional key/values to be added
  to the request map"
  (component/using (map->Parser (:parser config))
    {:bus :wsx-bus}))

;;;;;;;;;;;;;
;;; parse ;;;
;;;;;;;;;;;;;

(defn- ^:testable
  fetch-and-parse!
  "Synchronously and blocking parse and fetch from location.
  The output depends on the format of the parsing function, which now
  corresponds to parse/wiki-*->trie-pair. Value-hook modifies the value
  payload before insertion and is tipically used to assoc some
  db-specific fields. Returns a channel that will contains a map
  with :data and :error or nil if id does not make sense to recover from
  errors."
  [value-hook location]
  (async/thread
    (when-let [fetch-result (async/<!! (fetch location))]
      (let [{:keys [stream error]} fetch-result]
        (log/debugf "fetch-result ready (payload not displayed but error was: %s)" error)
        (if stream
          {:data (p/wiki-source->trie-pair value-hook stream)}
          {:error error})))))

(defn- ensure-location-in-cache
  [old-cache location]
  (if-let [loc (get old-cache location)]
    old-cache
    (assoc old-cache location (atom (p/empty-data)))))

(defn- ^:testable
  parse-location!
  "Asynchronously parses the location and updates the parser data cache
  as side effect. Returns the new data for the location. It returns a
  chann4el with the result."
  [parser location]
  (async/go
    (log/debug "processing location" location)

    (let [{:keys [parse-data-cache]} parser
          pd (get @parse-data-cache location)]
      (if (and (p/parsed? pd) (not (p/error? pd)))
        (do (log/debug "already parsed, returning cached value") 
            (p/result pd))
        (do (swap! parse-data-cache ensure-location-in-cache location)
            ;; point of sync
            (let [cached-pd (get @parse-data-cache location)
                  altered-pd (swap! cached-pd p/update-parse-state)
                  result (if (p/parsed? altered-pd)
                           (p/result altered-pd)
                           (do
                             (when (p/parsing? altered-pd)
                               (do (log/debug "parsing and writing results")
                                   (async/pipe (fetch-and-parse! identity location)
                                               (p/write-channel altered-pd))))
                             (async/<! (p/read-channel altered-pd))))]
              (log/debugf "location parsed (error was: %s)" (or (:error result) "-"))
              (swap! parse-data-cache assoc location (p/data->parsed result))
              result))))))


(defn- ^:testable
  msg->parsed!
  "Parses a location, returning a channel that will yield a
  core/msg->DataMsg with class :parsed-xml whose data is the result of
  either fetching or just returning the parsed document according to the
  location found in the input msg."
  [parser msg]
  (async/go
    (let [{:keys [location]} msg]
      (core/msg->DataMsg msg
                         (merge {:class :parsed-xml}
                                (async/<! (parse-location! parser location)))))))

(defn consume!
  "Consumes the input message."
  [parser msg]
  (let [chan (get-in parser [:bus :chan])]
    (case (:type msg)
      :parse (async/pipe (msg->parsed! parser msg) chan false)
      (log/debug "message is not for me"))))
