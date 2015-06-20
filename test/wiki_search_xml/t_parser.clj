(ns wiki-search-xml.t-parser
  (:require [clojure.core.async :refer [go >! >!!]]
            [wiki-search-xml.parser :refer :all]
            [wiki-search-xml.system :as sys]
            [wiki-search-xml.core :as core]
            [wiki-search-xml.common :as common]
            [midje.sweet :refer :all]))

(facts "about `parser`"
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)]

    (fact "key in system never nil"
      (:wsx-parser system) => some?)

    (fact "unstarted, should have nil dependencies and instance"
      (get-in system [:wsx-parser :sub-parse]) => nil
      (get-in system [:wsx-parser :bus]) => nil)
    
    (fact "when started should have non-nil dependencies and instance"
      (common/with-component-start system
        (get-in __started__ [:wsx-parser :sub-parse]) => some?
        (get-in __started__ [:wsx-parser :bus]) => some?))

    (fact "when started then stopped, should have nil dependencies and instance"
      (let [stopped (common/with-component-start system :test)]
        (get-in stopped [:wsx-parser :sub-parse]) => nil
        (get-in stopped [:wsx-parser :bus]) => nil))

    (fact "parse-location should produce :parsed-xml data"
      :slow
      (common/with-component-start system
        (let [parser (get-in __started__ [:wsx-parser])
              test-location (:test-resource-location config-map)] 
          (core/<t!! (parse-location parser {:location test-location})
                       25000)) => (contains {:type :data :class :parsed-xml})))

    (fact "parse-location should produce data from cache (faster) the second time over"
      :slow
      (common/with-component-start system
        (let [parser (get-in __started__ [:wsx-parser])
              test-location (:test-resource-location config-map)] 
          (do (core/<t!! (parse-location parser {:location test-location}) 45000) 
              (core/<t!! (parse-location parser {:location test-location})
                           500))) => (contains {:type :data :class :parsed-xml})))))
