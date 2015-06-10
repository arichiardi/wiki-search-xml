(ns wiki-search-xml.t-bus
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :refer [chan go <! >!] :rename {chan new-chan} ]
            [midje.sweet :refer :all]
            [wiki-search-xml.bus :refer :all]
            [wiki-search-xml.common :refer :all :as common]
            [wiki-search-xml.system :as sys]
            [wiki-search-xml.core :as core]))

(facts "about `bus`"
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)]

    (fact "key in system never nil"
      (:wsx-bus system) => some?)

    (fact "unstarted, should have nil dependencies and instance"
      (get-in system [:wsx-bus :chan]) => nil)

    (fact "when started should have non-nil dependencies and instance"
      (common/with-component-start system
        (get-in __started__ [:wsx-bus :chan]) => some?))

    (future-fact "when putting on bus, it should correctly send the message"
      (common/with-component-start system
        (let [bus-chan (get-in __started__ [:wsx-bus :chan])]
          (go (>! bus-chan "string"))
          (common/<t!! bus-chan 100)) => "string"))

    (fact "when `subscribe`, it should return a filtered message"
      (common/with-component-start system
        (let [bus (:wsx-bus __started__)
              bus-chan (:chan bus)
              subscription (subscribe bus :test (new-chan 1))]
          (go (>! bus-chan (core/->Msg :test)))
          (common/<t!! subscription 500)) => (core/->Msg :test)))

    (fact "when `subscribe` and send other :type msg should timeout"
      (common/with-component-start system
        (let [bus (:wsx-bus __started__)
              bus-chan (:chan bus)
              subscription (subscribe bus :test (new-chan 1))]
          (go (>! bus-chan {:your :msg}))
          (common/<t!! subscription 500)) => (core/->Msg :timeout)))))

