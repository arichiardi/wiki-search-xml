(ns wiki-search-xml.t-bus
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.bus :refer :all]
            [wiki-search-xml.system :as sys]
            [midje.sweet :refer :all]))

(facts "about `bus`"
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)]

    (fact ":wsx-bus never nil"
      (get system :wsx-bus) => some?)
    
    (fact "unstarted, should have nil :logger"
      (get-in system [:wsx-bus :chan]))
    
    (fact "started, should have :chan instance"
      (let [started-system (component/start system)]
        (get-in started-system [:wsx-bus :chan]) => some?))

    (fact "started then stopped, should have nil :logger"
      (let [stopped-system (component/stop (component/start system))]
        (get-in stopped-system [:wsx-bus :chan]) => nil))))
