(ns wiki-search-xml.t-server
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.server :refer :all]
            [wiki-search-xml.common :as common]
            [wiki-search-xml.system :as sys]
            [midje.sweet :refer :all]))

(facts "about `server`"
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)]

    (fact "key in system never nil"
      (:wsx-server system) => some?)

    (fact "unstarted, should have nil dependencies and instance"
      (get-in system [:wsx-server :handler]) => nil)
    
    (fact "when started should have non-nil dependencies and instance"
      (common/with-component-start system
        (get-in __started__ [:wsx-server :handler]) => some?))

    (fact "when started then stopped, should have nil dependencies and instance"
      (let [stopped (common/with-component-start system :test)]
        (get-in stopped [:wsx-parser :handles]) => nil))

    )
)
