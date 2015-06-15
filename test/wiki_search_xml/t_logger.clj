(ns wiki-search-xml.t-logger
  (:require [wiki-search-xml.logger :refer :all]
            [wiki-search-xml.system :as sys]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [midje.sweet :refer :all]))

(facts "about `log`"
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)]

    (fact "key in system never nil"
      (:wsx-logger system) => some?)
    
    (fact ":name from environment"
      (:name (new-logger config-map)) => (:wsx-logger-name env)
      (get-in system [:wsx-logger :name]) => (:wsx-logger-name env))
    
    (fact "unstarted, should have nil instance"
      (get-in system [:wsx-logger :logger]))
    
    (fact "started, should have non-nil instance"
      (let [started-system (component/start system)]
        (get-in started-system [:wsx-logger :logger]) => some?
        (component/stop started-system)))

    (fact "started then stopped, should have nil instance"
      (let [stopped-system (component/stop (component/start system))]
        (get-in stopped-system [:wsx-logger :logger]) => nil))

))
