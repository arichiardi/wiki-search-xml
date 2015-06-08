(ns wiki-search-xml.t-search
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.system :as sys]
            [midje.sweet :refer :all]))

(facts "about `searcher`"
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)]

    (fact "key in system never nil"
      (:wsx-searcher system) => some?)

    (future-fact "unstarted, should have nil dependencies and instance"
      (get-in system [:wsx-searcher :subscription]) => nil
      (get-in system [:wsx-searcher :fetcher]) => nil
      (get-in system [:wsx-searcher :bus]) => nil
      (get-in system [:wsx-searcher :logger]) => nil)
    
    (future-fact "when started should have non-nil dependencies and instance"
      (let [started-system (component/start system)]
        (get-in started-system [:wsx-searcher :subscription]) => some?
        (get-in started-system [:wsx-searcher :fetcher]) => some?
        (get-in started-system [:wsx-searcher :bus]) => some?
        (get-in started-system [:wsx-searcher :logger]) => some?))

    (future-fact "when started then stopped, should have nil dependencies and instance"
      (let [stopped-system (component/stop (component/start system))]
        (get-in stopped-system [:wsx-searcher :subscription]) => nil
        (get-in stopped-system [:wsx-searcher :fetcher]) => nil
        (get-in stopped-system [:wsx-searcher :bus]) => nil
        (get-in stopped-system [:wsx-searcher :logger]) => nil))

  ))
