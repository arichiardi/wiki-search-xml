(ns wiki-search-xml.t-common
  (:require  [wiki-search-xml.common :refer :all]
             [midje.sweet :refer :all]))

(facts  "about `common`"

  (fact "lazy? should return true with lazy seqs"
    (lazy? (map inc [1 2 3])) => truthy)

  (fact "lazy? should return false with normal seqs"
    (lazy? (list 1 2 3)) =not=> truthy)
  )
