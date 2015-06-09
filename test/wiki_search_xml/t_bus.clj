(ns wiki-search-xml.t-bus
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.bus :refer :all]
            [wiki-search-xml.common :refer :all :as common]
            [wiki-search-xml.system :as sys]
            [clojure.core.async :refer [chan go <! >! <!! >!! alts! alts!! timeout]]
            [midje.sweet :refer :all]))

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

    #_(fact "when putting on bus, it should correctly send the message"
      (let [started-system (component/start system)
            bus (:wsx-bus started-system)
            chan (:chan bus)]
        (do (common/<t!! (go (>! chan {:type :msg})
                             (<! chan)) 1000)) => {:type :msg} 
        (component/stop started-system)))

    (fact "when `subscribe`, it should return only filtered messages"
      (let [started-system (component/start system)
            bus (:wsx-bus started-system)
            chan (chan 1)
            subscription (subscribe bus :mine chan)]
        (do (go (>! (:chan bus) {:msg-to :mine, :msg "hi"}))
            (common/<t!! chan 500)) => {:msg-to :mine, :msg "hi"}
        ;; (do (go (>! (:chan bus) {:yours :msg}))
            ;; (common/<t!! chan 500)) => nil
        (component/stop started-system)))

    ))

