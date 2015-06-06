(ns wiki-search-xml.t-system
  (:require [midje.sweet :refer :all]
            [clojure.pprint :refer [pprint]]
            [wiki-search-xml.system :refer :all]))

(facts "about `system`"
  (fact "version is matching project.clj"
    (let [system (new-system (make-config))]
      (:wsx-version system) => "0.1.0-SNAPSHOT")))
