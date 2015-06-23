(ns wiki-search-xml.t-searcher
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.searcher :refer :all]
            [wiki-search-xml.system :as sys]
            [wiki-search-xml.common :as common]
            [wiki-search-xml.bus :as bus]
            [wiki-search-xml.core :as core]
            [clojure.core.async :refer [chan thread go <!! >! >!!]]
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
      (reduce merge-results {:r () :e ()} [{:r '[:foo]}
                                           {:r '[:bar] :e [:niet]}
                                           {:r '[:baz]}]) => (just {:r '(:foo :bar :baz), :e [:niet]})
      (reduce merge-results {:r () :e ()} [{:r ()}
                                           {:e '(:niet)}
                                           {:r '(:baz)}]) => (just {:r '(:baz), :e '(:niet)})
      (reduce merge-results {:r () :e ()} [{:e '(:foo)}
                                           {:e '(:bar)}]) => (just {:e '(:foo :bar) :r ()})
      (reduce merge-results {:r () :e ()} [{:r '(:foo)}
                                           {:r '(:bar)}]) => (just {:e () :r '(:foo :bar)}))))

(facts "about `search-for`"
  :slow
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)]
    (common/with-component-start system
      (let [searcher (get-in __started__ [:wsx-searcher])
            search-results (search-for searcher "blatnaya")] ;; warming up

        (fact "after warming up I should receive results"
          (core/<t!! search-results 25000) => (contains {:search-results #(> (count %1) 0) :search-errors empty?}))

        #_(fact "with correct key, I should receive results"
            (let [search-ch (search-for searcher "campos")
                  search-results (core/<t!! search-ch 250)]
              search-results => (contains {:search-results #(> (count %1) 0) :search-errors empty?})
              search-results =not=> (contains {:search-results [seq?]})))

        #_(fact "with INcorrect key, I should receive empty results"
            (core/<t!! (search-for searcher "oberto") 250) => (contains {:search-results empty?}))

        (fact "innumerable times should not break and behave correctly"
          (core/<t!! (thread (<!! (search-for searcher "campos"))) 250) => (contains {:search-results #(> (count %1) 0) :search-errors empty?})
          (core/<t!! (thread (<!! (search-for searcher "blatnaya"))) 250) => (contains {:search-results #(> (count %1) 0) :search-errors empty?})
          (core/<t!! (thread (<!! (search-for searcher "appear"))) 250) => (contains {:search-results #(> (count %1) 0) :search-errors empty?})
          (core/<t!! (thread (<!! (search-for searcher "Vancouver"))) 250) => (contains {:search-results empty? :search-errors empty?})
          (core/<t!! (thread (<!! (search-for searcher "blue"))) 250) => (contains {:search-results #(> (count %1) 0) :search-errors empty?}))))))
