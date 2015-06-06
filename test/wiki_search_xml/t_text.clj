(ns wiki-search-xml.t-text
  (:require [wiki-search-xml.text :refer :all]
            [wiki-search-xml.common :refer :all]
            [midje.sweet :refer :all]))

(facts "about `text`"

  (fact "`words` should return a lazy list"
    (lazy? (words "three two one")) => truthy)

  (fact "`words` should return just words"
    (words "                  ") => empty?
    (words ",\n,,..'.\"[") => empty?
    (words "\"four\"  three, two. \\one  \n zero") => (just ["four" "three" "two" "one" "zero"]))

  (fact "`words` consistency checks"
    (words "") => empty?
    (words "two") => (just ["two"])
    (words "two three four") => (just ["two" "three" "four"]))

  (future-fact "`words` should skip links"
    (words "Västerbotten County founded in 1923 http://www.bolletinen.") => (just ["Västerbotten" "County" "founded" "in" "1923"])
    (words "Västerbotten County founded in 1923http://www.bolletinen.") => (just ["Västerbotten" "County" "founded" "in" "1923"]))

  )
