(ns wiki-search-xml.t-search
  (:require [midje.sweet :refer :all]
            [clojure.pprint :refer [pprint]]
            [wiki-search-xml.system :as sys]))

(facts "about `searcher`"
  (fact "body is an InputStream"
    (let [config-map (sys/make-config)
          system (sys/new-system config-map)
          searcher (:web-searcher system)]
      
    )

  ))
