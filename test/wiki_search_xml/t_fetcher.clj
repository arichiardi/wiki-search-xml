(ns wiki-search-xml.t-fetcher
  (:require [com.stuartsierra.component :as component]
            [midje.sweet :refer :all]
            [wiki-search-xml
             [common :as common]
             [fetcher :refer :all]
             [system :as sys]])
  (:import [java.io InputStream IOException]))

(facts "about `fetcher`"
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)]

    (fact "key in system never nil"
      (:wsx-fetcher system) => some?)

    (fact "unstarted, should have nil dependencies and instance"
      (get-in system [:wsx-fetcher :fetcher]) => nil)

    (fact "when started then stopped, should have nil dependencies and instance"
      (let [stopped (common/with-component-start system :test)]
        (get-in stopped [:wsx-fetcher :fetcher]) => nil))

    (fact "response :body is an InputStream"
      (let [new-fetcher (assoc (:wsx-fetcher system) :type :wiki-search-xml.fetcher/file)
            new-sys (assoc system :wsx-fetcher new-fetcher)]
        
        (common/with-component-start new-sys
          (let [resp-channel (fetch new-fetcher (:test-file config-map) {})
                response (common/<t!! resp-channel 3000)
                stream (:body response)]
            stream => (partial instance? InputStream)
            (doto stream (.close) (.read)) => (throws IOException)))))

    ))
