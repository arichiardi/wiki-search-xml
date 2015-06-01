(ns wiki-search-xml.t-system
  (:require [wiki-search-xml.system :refer :all] 
            [midje.sweet :refer :all]))

(facts "about `system`"
  (fact "version is matching project.clj"
    (let [system (new-system {})]
      (:version system) => "0.1.0-SNAPSHOT"))
  )
