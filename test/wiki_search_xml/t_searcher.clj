(ns wiki-search-xml.t-searcher
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.system :as sys]
            [wiki-search-xml.common :as common]
            [wiki-search-xml.core :as core]
            [clojure.core.async :refer [go >! >!!]]
            [environ.core :refer [env]]
            [midje.sweet :refer :all]))

(facts "about `searcher`"
  (let [system (sys/new-system (sys/make-config))]

    (fact "key in system never nil"
      (:wsx-searcher system) => some?)

    (fact "locations from environment"
      (get-in system [:wsx-searcher :locations]) =not=> nil)
    
    (fact "unstarted, should have nil dependencies and instance"
      (get-in system [:wsx-searcher :subscription]) => nil
      (get-in system [:wsx-searcher :bus]) => nil)
    
    (fact "when started should have non-nil dependencies and instance"
      (common/with-component-start system
        (get-in __started__ [:wsx-searcher :subscription]) => some?
        (get-in __started__ [:wsx-searcher :bus]) => some?))

    (fact "when started then stopped, should have nil dependencies and instance"
      (let [stopped (common/with-component-start system :test)]
        (get-in stopped [:wsx-searcher :subscription]) => nil
        (get-in stopped [:wsx-searcher :bus]) => nil))

    (fact "when `:query` msg received, it should put :parse on bus"
      (common/with-component-start system
        (let [bus-chan (get-in __started__ [:wsx-bus :chan])] 
          (do (go (>! bus-chan {:type :query :key "search-me"}))
              (common/<t!! bus-chan 2000)) => (contains {:type :parse :for-key "search-me"}))))))

