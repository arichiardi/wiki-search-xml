(ns wiki-search-xml.t-fetcher
  (:require [clojure.java.io :as io]
            [environ.core :refer [env]]
            [wiki-search-xml
             [fetcher :refer :all]
             [system :as system]])
  (:import java.io.InputStream))

(defrecord FileFetcher [http-option-map]
  Fetch
  (fetch [this url]
    {:status 200
     :headers {"Date" "Sun, 12 Nov 2015 07:03:49 GMT"
               "Content-Type" "text/xml; charset=UTF-8"}
     :body (-> url io/resource io/file io/input-stream)}))


(facts "about `fetcher`"
  (fact "body is an InputStream"
    (let [config-map (system/make-config)
          fetcher (map->FileFetcher (:fetcher config-map))
          response (fetch fetcher (env :wiki-text-xml))]
      (:body response) => (partial instance? InputStream)
      (:status response) => 200)))
