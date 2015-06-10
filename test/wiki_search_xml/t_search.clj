(ns wiki-search-xml.t-search
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.system :as sys]
            [wiki-search-xml.common :as common]
            [clojure.core.async :refer [go >! >!!]]
            [environ.core :refer [env]]
            [midje.sweet :refer :all]))

(facts "about `searcher`"
  (let [system (sys/new-system (sys/make-config))]

    (fact "key in system never nil"
      (:wsx-searcher system) => some?)

    (fact "wiki end point and url from environment"
      (get-in system [:wsx-searcher :end-point]) =not=> nil)
    
    (future-fact "unstarted, should have nil dependencies and instance"
      (get-in system [:wsx-searcher :subscription]) => nil
      (get-in system [:wsx-searcher :fetcher]) => nil
      (get-in system [:wsx-searcher :bus]) => nil)
    
    (future-fact "when started should have non-nil dependencies and instance"
      (common/with-component-start system
        (get-in __started__ [:wsx-searcher :subscription]) => some?
        (get-in __started__ [:wsx-searcher :fetcher]) => some?
        (get-in __started__ [:wsx-searcher :bus]) => some?
        (component/stop started-system)))

    (future-fact "when started then stopped, should have nil dependencies and instance"
      (let [stopped (common/with-component-start system :test)]
        (get-in stopped [:wsx-searcher :subscription]) => nil
        (get-in stopped [:wsx-searcher :fetcher]) => nil
        (get-in stopped [:wsx-searcher :bus]) => nil))

    (fact "when `subscribe`, it should return only filtered messages"
      (let [started-system (component/start system)
            sub-chan (get-in started-system [:wsx-searcher :subscription])
            bus-chan (get-in started-system [:wsx-bus :chan])]
        (do (go (>! bus-chan {:type :search, :msg "hi"}))
            (common/<t!! sub-chan 500)) => {:type :search :msg "hi"}
        ;; (do (go (>! (:chan bus) {:yours :msg}))
            ;; (common/<t!! chan 500)) => nil
        (component/stop started-system)))
  ))
