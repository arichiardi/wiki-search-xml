(ns wiki-search-xml.searcher
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.bus :refer [subscribe]]
            [wiki-search-xml.text :as text]
            [wiki-search-xml.core :as core]
            [wiki-search-xml.common :as common]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go go-loop chan <! >! close!]]))

(declare consume!)

(defrecord Searcher [ ;; config?
                     buffer-conf locations
                     ;; dependecies
                     bus
                     ;; state
                     sub-query sub-result]
  component/Lifecycle
  (stop [this]
    (if sub-query
      (let [component (-> this
                          (assoc :sub-query nil)
                          (assoc :sub-result nil))]
        (close! sub-query)
        (close! sub-result))
      this))

  (start [this]
    (if sub-query
      this
      (let [c (common/conf->buffer buffer-conf)
            sq (subscribe bus :query c)
            sr (subscribe bus :result c)
            component (-> this
                          (assoc :sub-query sq)
                          (assoc :sub-result sr))]
        (core/loop! (partial consume! component) sq sr)
        component))))

(defn new-searcher
  "Creates a new Searcher."
  [config-map]
  (component/using (map->Searcher (:searcher config-map))
    {:bus :wsx-bus}))

(defn search-for
  "Performs the search, needs a Searcher and a key to look for.
  This method is synchronous, but it parks if cannot go on. Resurns a
  vector of results for key."
  [this key]
  (let [{:keys [bus locations sub-result]} this]

    ;; Parsing/Retrieving the parsed documents in locations
    (doseq [loc locations]
      (do (log/debugf "searching for key %s at location %s" key loc)

          ;; (if ) ;; find in db

          ;; if not delegate to parse
          (go (>! (:chan bus) (core/map->Msg {:type :parse
                                              :location loc
                                              :for-key key})))))

    (go-loop [] 
      (let [{:keys [sender for-key data]} (<! sub-result)]
        (log/debugf "received result from %s for key %s" sender key)
        (if (and (= :parser sender))
          (text/trie-get data key)
          (recur))))))



(defn consume!
  "Consumes the input message."
  [this msg]
  (case (:type msg)
    :query (search-for this msg)
    :result (case (:sender msg)
              :parser (consume-parse-result this msg))
    (log/warn "message is not for me")))

