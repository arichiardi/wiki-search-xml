(ns wiki-search-xml.t-search
  (:require [midje.sweet :refer :all]
            [clojure.pprint :refer [pprint]]
            [wiki-search-xml.system :as system]))

(facts "about `searcher`"
  (fact "body is an InputStream"
    (let [config-map (system/make-config)
          system (system/new-system config-map)
          searcher (:web-searcher system)]
      (pprint searcher)
      
    )

  ))
