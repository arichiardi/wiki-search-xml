(ns wiki-search-xml.t-searcher
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.searcher :refer :all]
            [wiki-search-xml.system :as sys]
            [wiki-search-xml.common :as common]
            [wiki-search-xml.bus :as bus]
            [wiki-search-xml.core :as core]
            [clojure.core.async :refer [chan dropping-buffer go >! >!!]]
            [environ.core :refer [env]]
            [midje.sweet :refer :all]
            [midje.util :refer [expose-testables]]))

(expose-testables wiki-search-xml.searcher)

(facts "about `searcher`"
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)]

    (fact "key in system never nil"
      (:wsx-searcher system) => some?)

    (fact "locations from environment"
      (get-in system [:wsx-searcher :locations]) =not=> nil)

    (fact "unstarted, should have nil dependencies and instance"
      (get-in system [:wsx-searcher :sub-query]) => nil
      (get-in system [:wsx-searcher :bus]) => nil)

    (fact "when started should have non-nil dependencies and instance"
      (common/with-component-start system
        (get-in __started__ [:wsx-searcher :sub-query]) => some?
        (get-in __started__ [:wsx-searcher :bus]) => some?))

    (fact "when started then stopped, should have nil dependencies and instance"
      (let [stopped (common/with-component-start system :test)]
        (get-in stopped [:wsx-searcher :sub-query]) => nil
        (get-in stopped [:wsx-searcher :bus]) => nil))

    ))



(let [config-map (sys/make-config)
      system (sys/new-system config-map)]

  (common/with-component-start system
      (let [searcher (get-in __started__ [:wsx-searcher])]

        (fact "search-location-async with correct key, I should receive :result full"
          :slow
          (common/<t!! (search-for searcher "roberto") 40000) => (contains {:result anything}))

        ;; #_(common/<t!! (search-location "roberto") 45000) => (contains {:result anything})
        #_(fact "search-location-async with INcorrect key, I should receive empty :result"
          :slow
          (common/<t!! (search-for searcher "oberto") 500) => (contains {:result anything}))
)))
