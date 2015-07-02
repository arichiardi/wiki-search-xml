(ns wiki-search-xml.t-parser
  (:require [clojure.tools.trace :refer [deftrace trace] :rename {trace t}]
            [clojure.core.async :as async]
            [wiki-search-xml.parser :refer :all]
            [wiki-search-xml.system :as sys]
            [wiki-search-xml.core :as core]
            [wiki-search-xml.common :as common]
            [midje.sweet :refer :all]
            [midje.util :refer [expose-testables]]))

(expose-testables wiki-search-xml.parser)

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

    (fact "fetch-and-parse! should produce a parsing record with filled :data"
        :slow
        (let [test-location (:test-resource-location config-map)]
          (core/<t!! (fetch-and-parse! identity test-location) 30000) => (contains {:data some?})))
    
    (fact "parse-location! should not work twice but give results for all requesters"
        :slow
        (common/with-component-start system
          (let [parser (get-in __started__ [:wsx-parser])
                test-location (:test-resource-location config-map)]
            (core/<t!! (async/reduce (fn [c _] (inc c)) 0
                                     (async/merge [(async/go (async/<! (parse-location! parser test-location)))
                                                   (async/go (async/<! (parse-location! parser test-location)))
                                                   (async/go (async/<! (parse-location! parser test-location)))
                                                   (async/go (async/<! (parse-location! parser test-location)))]))
                       30000) => 4)))

    (fact "parse-location! should produce :parsed-xml data"
        :slow
        (common/with-component-start system
          (let [parser (get-in __started__ [:wsx-parser])
                test-location (:test-resource-location config-map)]
            (core/<t!! (parse-location! parser test-location)
                       30000) => (contains {:data some?}))))

    (fact "parse-location! should produce data from cache (faster) the second time over"
        :slow
        (common/with-component-start system
          (let [parser (get-in __started__ [:wsx-parser])
                test-location (:test-resource-location config-map)]
            (do (core/<t!! (parse-location! parser test-location) 30000)
                (core/<t!! (parse-location! parser test-location)
                           500))) => (contains {:data some?})))

    (fact "parse-location! should produce data from cache (faster) the second time over"
      :slow
      (common/with-component-start system
        (let [parser (get-in __started__ [:wsx-parser])
              msg {:type :parse :location (:test-resource-location config-map)}]
          (core/<t!! (msg->parsed! parser msg)
                     35000) => (contains {:type :data :class :parsed-xml :data some?}))))))

