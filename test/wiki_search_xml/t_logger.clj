(ns wiki-search-xml.t-logger
  (:require [wiki-search-xml.logger :refer :all]
            [wiki-search-xml.system :as sys]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [midje.sweet :refer :all]))

(facts "about `logger`"
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)]

    (fact "key in system never nil"
      (:wsx-logger system) => some?)
    
    (fact ":name from environment"
      (:name (new-logger config-map)) => (:wsx-logger-name env)
      (get-in system [:wsx-logger :name]) => (:wsx-logger-name env))

    (fact "unstarted, should have nil dependencies and instance"
      (get-in system [:wsx-server :handler]) => nil)
    

))
