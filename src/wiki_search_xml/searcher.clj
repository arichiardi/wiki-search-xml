(ns wiki-search-xml.searcher
  (:require [clojure.tools.trace :refer [deftrace trace] :rename {trace t}]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [wiki-search-xml.bus :refer [subscribe unsubscribe]]
            [wiki-search-xml.core :as core]
            [wiki-search-xml.text :as text]
            [wiki-search-xml.parse.impl :as parse]
            [wiki-search-xml.common :as common]))

(declare consume!)

(defrecord Searcher [ ;; config?
                     buffer-conf parse-timeout locations
                     ;; dependecies
                     bus
                     ;; state
                     sub-query]
  component/Lifecycle
  (stop [this]
    (if sub-query
      (let [component (assoc this :sub-query nil)]
        (async/close! sub-query)
        component)
      this))

  (start [this]
    (if sub-query
      this
      (let [c (common/conf->buffer buffer-conf)
            sq (subscribe bus :query c)
            component (assoc this :sub-query sq)]
        (core/loop! (partial consume! component) sq)
        component))))

(defn new-searcher
  "Creates a new Searcher."
  [config-map]
  (component/using (map->Searcher (:searcher config-map))
    {:bus :wsx-bus}))

(defn- ^:testable
  search-location-async
  "Search for a location, interacting with the input channels if
  necessary and waiting timeout for the underlying data grinding to
  complete before returning an error.

  It always returns a channel which will always contain a record with
  either :result, a vector of found results or :error. Note that :result
  will never be nil. If no result is found, it will be an empty vector."
  [bus-channel data-channel timeout-ms location key]
  (async/go
    (log/debugf "Searching for key %s in location %s" key location)
    (async/>! bus-channel (core/map->Msg {:type :parse
                                          :location location
                                          :for-key key}))
    (log/debugf "Waiting for :parse request to complete (timeout %s)" timeout-ms)
    (async/alt!
      data-channel ([{:keys [class data error location]} _]
                    (log/debugf "received data of class %s for location %s (error: %s)" class location error)
                    (if (= :parsed-xml class)
                      (if data
                        {:result (or (t "result" (text/trie-get (parse/trie data) (t "key" key))) [])}
                        {:error error})))
      (async/timeout timeout-ms) {:error (str "Search in location " location " timed out")}
      :priority true)))

(defn search-for
  "Performs the search, needs a Searcher and a key to look for.
  This method is synchronous, but it parks if cannot go on. Resurns a
  vector of results for key."
  [this key]
  (let [{:keys [bus locations]} this
        sub-data (subscribe bus :data (async/chan))]

    ;; Parsing/Retrieving the parsed documents in locations
    (doseq [loc locations]
      (do (log/debugf "searching for key %s at location %s" key loc)

          ;; (if ) ;; find in db

          ;; if not delegate to parse
          ))

    (unsubscribe bus :data sub-data)))


(defn consume!
  "Consumes the input message."
  [this msg]
  (case (:type msg)
    :query (search-for this msg)
    (log/warn "message is not for me")))

