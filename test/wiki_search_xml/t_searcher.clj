(ns wiki-search-xml.t-searcher
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.system :as sys]
            [wiki-search-xml.common :as common]
            [wiki-search-xml.bus :as bus]
            [wiki-search-xml.core :as core]
            [clojure.core.async :refer [chan go >! >!!]]
            [environ.core :refer [env]]
            [midje.sweet :refer :all]
            [midje.util :refer [expose-testables]]))

(expose-testables wiki-search-xml.searcher)

(facts "about `searcher`"
  (let [system (sys/new-system (sys/make-config))]

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

    (fact "search-location-async with correct key, I should receive :result full"
      :slow
      (common/with-component-start system
        (let [bus (get-in __started__ [:wsx-bus])
              bus-chan (:chan bus)
              sub-data (bus/subscribe bus :data (chan))
              location (:test-resource-location config-map)
              search-location (partial search-location-async bus-chan sub-data 15000 location)]
          (common/<t!! (search-location "bueno") 20000) => (contains {:result [:title anything]})
          (bus/unsubscribe bus :data sub-data))))
    
    #_(fact "search-location-async with INcorrect key, I should receive :key-queried and :error"
      :slow
      (common/with-component-start system
        (let [bus (get-in __started__ [:wsx-bus])
              bus-chan (:chan bus)
              sub-data (bus/subscribe bus :data (chan))
              location (:test-resource-location config-map)
              search-location (partial search-location-async bus-chan sub-data 15000 location)]
          (common/<t!! (search-location "Roberto") 20000) => nil? #_ (contains {:type :data
                                                                              :class :key-queried
                                                                              :error anything})
          (bus/unsubscribe bus :data sub-data)))))
