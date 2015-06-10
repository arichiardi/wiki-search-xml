(ns wiki-search-xml.t-bus
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan go <! >! <!! >!! alts! alts!! timeout]]
            [midje.sweet :refer :all]
            [wiki-search-xml.bus :refer :all]
            [wiki-search-xml.common :refer :all :as common]
            [wiki-search-xml.system :as sys]
            [wiki-search-xml.core :as core] ))

(facts "about `bus`"
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)]

    (fact "key in system never nil"
      (:wsx-bus system) => some?)

    (fact "unstarted, should have nil dependencies and instance"
      (get-in system [:wsx-bus :chan]) => nil)

    (fact "when started should have non-nil dependencies and instance"
      (let [started-system (component/start system)]
        (get-in started-system [:wsx-bus :chan]) => some?
        (component/stop started-system)))

    (fact "when started then stopped, should have nil dependencies and instance"
      (let [stopped-system (component/stop (component/start system))]
        (get-in stopped-system [:wsx-bus :chan]) => nil))

    (fact "when putting on bus, it should correctly send the message"
      (let [started-system (component/start system)
            chan (:chan (:wsx-bus started-system))]
        (do (common/<t!! (go (>! chan (core/->Msg :test))
                             (<! chan)) 1000)) => (core/->Msg :test) 
        (component/stop started-system)))

    (fact "when `subscribe`, it should return only filtered messages"
      (let [started-system (component/start system)
            bus (:wsx-bus started-system)
            chan (chan 1)
            subscription (subscribe bus  chan)]
        (do (go (>! (:chan bus) (core/->Msg :test)))
            (common/<t!! chan 500)) => (core/->Msg :test)
        (do (go (>! (:chan bus) {:yours :msg}))
            (common/<t!! chan 500)) => (core/->Msg :timeout)
        (component/stop started-system)))

    ))

