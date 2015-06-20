(ns wiki-search-xml.t-searcher
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.searcher :refer :all]
            [wiki-search-xml.system :as sys]
            [wiki-search-xml.common :as common]
            [wiki-search-xml.bus :as bus]
            [wiki-search-xml.core :as core]
            [clojure.core.async :refer [chan dropping-buffer go <!! >! >!!]]
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
      (get-in system [:wsx-searcher :sub-data]) => nil
      (get-in system [:wsx-searcher :bus]) => nil)

    (fact "when started should have non-nil dependencies and instance"
      (common/with-component-start system
        (get-in __started__ [:wsx-searcher :sub-data]) => some?
        (get-in __started__ [:wsx-searcher :bus]) => some?))

    (fact "when started then stopped, should have nil dependencies and instance"
      (let [stopped (common/with-component-start system :test)]
        (get-in stopped [:wsx-searcher :sub-data]) => nil
        (get-in stopped [:wsx-searcher :bus]) => nil))

    (fact "merge-result behaves correctly"
      (reduce merge-results {} [{:r [:foo]}
                                {:r [:bar] :e [:niet]}
                                {:r [:baz]}]) => (just {:r [:foo :bar :baz], :e [:niet]})
      (reduce merge-results {} [{:r []}
                                {:e [:niet]}
                                {:r [:baz]}]) => (just {:r [:baz], :e [:niet]})
      (reduce merge-results {} [{:e [:foo]}
                                {:e [:bar]}]) => (just {:e [:foo :bar]})
      (reduce merge-results {} [{:r [:foo]}
                                {:r [:bar]}]) => (just {:r [:foo :bar]}))

    (common/with-component-start system
      (let [searcher (get-in __started__ [:wsx-searcher])
            _ (<!! (search-for searcher "roberto"))] ;;warming up

        (fact "search-for with correct key, I should receive :result full"
          :slow
          (let [search-results (search-for searcher "roberto")] 
            (<!! search-results) => (contains {:search-results vector?})
            (<!! search-results) =not=> (contains {:search-results [vector?]})))

        (fact "search-for with INcorrect key, I should receive empty :result"
          :slow
          (core/<t!! (search-for searcher "oberto") 250) => (contains {:search-results empty?})) 

        (future-fact "search-for innumerable times should not break")))
    ))
