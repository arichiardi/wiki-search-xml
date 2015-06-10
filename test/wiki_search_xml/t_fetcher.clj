(ns wiki-search-xml.t-fetcher
  (:require [clojure.core.async :refer [go chan <! >! close!]]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [wiki-search-xml.fetcher :refer :all]
            [wiki-search-xml.system :as sys]
            [wiki-search-xml.common :as common]
            [wiki-search-xml.core :as core]
            [midje.sweet :refer :all])
  (:import java.io.InputStream))

(facts "about `fetcher`"
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)]

    (fact "key in system never nil"
      (:wsx-fetcher system) => some?)

    (fact "unstarted, should have nil dependencies and instance"
      (get-in system [:wsx-fetcher :subscription]) => nil
      (get-in system [:wsx-fetcher :process!]) => nil
      (get-in system [:wsx-fetcher :fetcher]) => nil
      (get-in system [:wsx-fetcher :bus]) => nil)
    
    (fact "when started then stopped, should have nil dependencies and instance"
      (let [stopped (common/with-component-start system :test)]
        (get-in stopped [:wsx-fetcher :subscription]) => nil
        (get-in stopped [:wsx-fetcher :process!]) => nil
        (get-in stopped [:wsx-fetcher :fetcher]) => nil
        (get-in stopped [:wsx-fetcher :bus]) => nil))
    
    (fact "response :body is an InputStream"
      (let [new-fetcher (assoc (:wsx-fetcher system) :kind :file)
            new-sys (assoc system :wsx-fetcher new-fetcher)] 
        (common/with-component-start new-sys
          (let [bus (get-in __started__ [:wsx-bus :chan])
                response (do (go (>! (:chan bus) (-> (core/->Msg :fetch)
                                                     (merge (apply hash-map (find config-map :test-file))))))
                             (common/<t!! bus 3000))]
            (:body response) => (partial instance? InputStream) 
            (:status response) => 200))))))
