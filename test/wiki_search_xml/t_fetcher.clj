(ns wiki-search-xml.t-fetcher
  (:require [com.stuartsierra.component :as component]
            [clojure.java.io :as io]
            [environ.core :refer [env]]
            [wiki-search-xml.fetcher :refer :all]
            [wiki-search-xml.system :as sys]
            [midje.sweet :refer :all])
  (:import java.io.InputStream))

(defrecord FileFetcher [http-option-map]
  Fetch
  (fetch [this url]
    {:status 200
     :headers {"Date" "Sun, 12 Nov 2015 07:03:49 GMT"
               "Content-Type" "text/xml; charset=UTF-8"}
     :body (-> url io/resource io/file io/input-stream)}))


(facts "about `fetcher`"
  (let [config-map (sys/make-config)
        fetcher (map->FileFetcher (:fetcher config-map))]

    (fact "response :body is an InputStream"
      (let [response (fetch fetcher (:test-file config-map))]
        (:body response) => (partial instance? InputStream) 
        (:status response) => 200))))
