(ns wiki-search-xml.fetcher
  (:require [clj-http.lite.client :as http]))

(defprotocol Fetch
  "Fetches documents"
  (fetch [this url]
    "Fetching method, returns a map containing the result of fetching the document"))

(defrecord Fetcher [;; config
                    http-option-map]
  Fetch
  (fetch [this url]
    (http/get url {:as :stream})))

(defn new-fetcher [config]
  "Creates an instance of a document fetcher, it accepts a map of additional key/values to be added
  to the request map"
  (map->Fetcher (:fetcher config)))
