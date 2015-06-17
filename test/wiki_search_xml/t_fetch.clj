(ns wiki-search-xml.t-fetch
  (:require [com.stuartsierra.component :as component]
            [midje.sweet :refer :all]
            [wiki-search-xml.common :as common]
            [wiki-search-xml.fetch :refer :all]
            [wiki-search-xml.system :as sys])
  (:import [java.io InputStream IOException]))

(facts "about `fetch`"

  (fact "fetch with type :resource-file (config.edn's locations) returns :stream as InputStream."
    (let [config-map (sys/make-config)
          ;; I know what is the first location here
          resp-channel (fetch (first (get-in config-map [:searcher :locations])))
          response (common/<t!! resp-channel 2000)
          stream (:stream response)]
      stream => (partial instance? InputStream)
      (.close stream)))

  (fact "fetch with type :network-file (config.edn test-network-location) has :stream as InputStream."
    :network
    (let [config-map (sys/make-config)
          ;; I know what is the first location here
          resp-channel (fetch (get config-map :test-network-location))
          response (common/<t!! resp-channel 7000)
          stream (:stream response)]
      stream => (partial instance? InputStream)
      (.close stream))))



