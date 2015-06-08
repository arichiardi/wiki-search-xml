(ns wiki-search-xml.t-bus
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.bus :refer :all]
            [wiki-search-xml.common :refer :all :as common]
            [wiki-search-xml.system :as sys]
            [clojure.core.async :refer [go <! >! <!! >!! alts! alts!! timeout]]
            [midje.sweet :refer :all]))

(facts "about `bus`"
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)
        timeout-ch (timeout 2000)]

    (fact "key in system never nil"
      (:wsx-bus system) => some?)

    (fact "unstarted, should have nil dependencies and instance"
      (get-in system [:wsx-bus :chan]) => nil)

    (fact "when started should have non-nil dependencies and instance"
      (let [started-system (component/start system)]
        (get-in started-system [:wsx-bus :chan]) => some?))

    (fact "when started then stopped, should have nil dependencies and instance"
      (let [stopped-system (component/stop (component/start system))]
        (get-in stopped-system [:wsx-bus :chan]) => nil))

    (fact "`post` should correctly send the input message"
      (let [started-system (component/start system)
            bus (:wsx-bus started-system)]
        (do (go (>! (:chan bus) {:type :msg})) 
          (common/<t!! (:chan bus) 1000))) => {:type :msg})

    (fact "`subscribe` should return only filtered messages"
      (let [started-system (component/start system)
            bus (:wsx-bus started-system)
            subscription (subscribe bus :mine)]
        (do (go (>! (:chan bus) {:mine :msg}))
            (common/<t!! subscription 1000)) => {:mine :msg}
        (do (go (>! (:chan bus) {:yours :msg}))
            (common/<t!! subscription 1000)) => :!!timed-out!!))

    ))

