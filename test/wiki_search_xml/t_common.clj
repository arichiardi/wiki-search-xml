(ns wiki-search-xml.t-common
  (:require  [wiki-search-xml.common :refer :all]
             [clojure.core.async :refer [chan go <! >! timeout]]
             [midje.sweet :refer :all]))

(facts  "about `common`"

  (fact "`lazy?` should return true with lazy seqs"
    (lazy? (map inc [1 2 3])) => truthy)

  (fact "`lazy?` should return false with normal seqs"
    (lazy? (list 1 2 3)) =not=> truthy)

  (fact "`<t!!` return the right value or times out"
    (let [c1 (chan)]
      (do (go (>! c1 "test")) (<t!! c1 1000)) => "test"
      (do (go (do (<! (timeout 500)) (>! c1  "test"))) (<t!! c1 1000)) => "test" 
      (do (go (do (<! (timeout 2000)) (>! c1  "test"))) (<t!! c1 1000)) => nil
      (<t!! c1 10) => nil
      )
    )
  )

