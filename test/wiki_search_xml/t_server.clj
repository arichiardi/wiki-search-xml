(ns wiki-search-xml.t-server
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.server :refer :all]
            [wiki-search-xml.system :as sys]
            [midje.sweet :refer :all]))

(facts "about `server`"
  (let [config-map (sys/make-config)
        system (sys/new-system config-map)]

    (future-fact "key in system never nil"
      (:wsx-server system) => some?)

    (future-fact "unstarted, should have nil dependencies and instance"
                 (get-in system [:wsx-server :subscription] => nil)
                 (get-in system [:wsx-server :parser]) => nil
                 (get-in system [:wsx-server :bus]) => nil
                 (get-in system [:wsx-server :logger]) => nil)
    
    (future-fact "when started should have non-nil dependencies and instance"
                 (let [started-system (component/start system)]
                   (get-in started-system [:wsx-server :subscription] => some?)
                   (get-in started-system [:wsx-server :parser]) => some?
                   (get-in started-system [:wsx-server :bus]) => some?
                   (get-in started-system [:wsx-server :logger]) => some?))

    (future-fact "when started then stopped, should have nil dependencies and instance"
                 (let [stopped-system (component/stop (component/start system))]
                   (get-in stopped-system [:wsx-server :subscription] => nil)
                   (get-in stopped-system [:wsx-server :parser]) => nil
                   (get-in stopped-system [:wsx-server :bus]) => nil
                   (get-in stopped-system [:wsx-server :logger]) => nil))


))
