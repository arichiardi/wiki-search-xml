(ns wiki-search-xml.t-log
  (:require [wiki-search-xml.log :refer :all]
            [wiki-search-xml.system :as sys]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [midje.sweet :refer :all]))

(facts "about `log`"
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)]

    (fact ":logger never nil"
      (:wsx-logger system) => some?)
    
    (fact ":name from environment"
      (:name (new-logger config-map)) => (:wsx-logger-name env)
      (get-in system [:wsx-logger :name]) => (:wsx-logger-name env))
    
    (fact "unstarted, should have nil :logger"
      (get-in system [:wsx-logger :logger]))
    
    (fact "started, should have its instance"
      (let [started-system (component/start system)]
        (get-in started-system [:wsx-logger :logger]) => some?))

    (fact "started then stopped, should have nil instance"
      (let [stopped-system (component/stop (component/start system))]
        (get-in stopped-system [:wsx-logger :logger]) => nil))

))
