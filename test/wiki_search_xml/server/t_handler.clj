(ns wiki-search-xml.server.t-handler
  (:require [wiki-search-xml.server.handler :refer :all]
            [wiki-search-xml.system :as sys]
            [wiki-search-xml.common :as common]
            [midje.sweet :refer :all]))

(facts "about `handler`"
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)]

    (fact "key in system never nil"
      (:wsx-handler system) => some?)

    (fact "unstarted, should have nil dependencies and instance"
      (get-in system [:wsx-handler :searcher]) => nil)
    
    (fact "when started should have non-nil dependencies and instance"
      (common/with-component-start system
        (get-in __started__ [:wsx-handler :searcher]) => some?))

    (fact "when started then stopped, should have nil dependencies and instance"
      (let [stopped (common/with-component-start system :test)]
        (get-in stopped [:wsx-handler :searcher]) => nil))

    )
  )
