(ns wiki-search-xml.t-search
  (:require [midje.sweet :refer :all]
            [com.stuartsierra.component :as component]
            [clojure.pprint :refer [pprint]]
            [wiki-search-xml.system :as sys]))

(facts "about `searcher`"
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)]

    (fact ":searcher never nil"
      (:wsx-searcher system) => some?)
    
    (let [searcher (:wsx-searcher system)]

      (fact "dependencies are set when starded"
        (let [started-system (component/start system)]
          (get-in started-system [:wsx-searcher :fetcher]) => some?
          (get-in started-system [:wsx-searcher :bus]) => some?
          (get-in started-system [:wsx-searcher :logger]) => some?
          ))

      (fact "unstarted, should have nil dependencies"
        (get-in system [:wsx-searcher :fetcher]) => nil
        (get-in system [:wsx-searcher :bus]) => nil
        (get-in system [:wsx-searcher :logger]) => nil
        ))

    (fact "started then stopped, should have nil instance"
      (let [stopped-system (component/stop (component/start system))]
        (get-in system [:wsx-searcher :fetcher]) => nil
        (get-in system [:wsx-searcher :bus]) => nil
        (get-in system [:wsx-searcher :logger]) => nil))

  ))
