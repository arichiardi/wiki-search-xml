(ns wiki-search-xml.t-common
  (:require  [wiki-search-xml.common :refer :all]
             [clojure.core.async :refer [chan go <! >! timeout]]
             [wiki-search-xml.core :as core]
             [midje.sweet :refer :all]))

(facts  "about `common`"

  (fact "`lazy?` should return true with lazy seqs"
    (lazy? (map inc [1 2 3])) => truthy)

  (fact "`lazy?` should return false with normal seqs"
    (lazy? (list 1 2 3)) =not=> truthy)

  (fact "`lazy?` should return true for lazy Cons cells"
    (lazy? (re-seq #"[0-9]+" "33 4 5")) => truthy)

   (fact "`lazy?` should return true for lazy Cons cells"
    (lazy? (cons \f "oo")) => falsey)
  
  (fact "`<t!!` return the right value or times out"
    (let [c1 (chan)]
      (do (go (>! c1 "test")) (<t!! c1 1000)) => "test"
      (do (go (do (<! (timeout 500)) (>! c1  "test"))) (<t!! c1 1000)) => "test" 
      (do (go (do (<! (timeout 2000)) (>! c1  "test"))) (<t!! c1 1000)) => core/timeout-msg
      (<t!! c1 10) => core/timeout-msg))

  (fact "`with-component-start` should behave correctly"
    (let [dummy (new-dummy-component "test")]
      (with-component-start dummy
        (:started __started__) => truthy)))
  )

