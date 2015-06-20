(ns wiki-search-xml.t-system
  (:require [clojure.pprint :refer [pprint]]
            [wiki-search-xml.system :refer :all]
            [midje.sweet :refer :all]))

(facts "about `system`"
  (fact "version is matching project.clj"
    (let [system (new-system (make-config))]
      (:wsx-version system) => "0.3.0-SNAPSHOT")))
