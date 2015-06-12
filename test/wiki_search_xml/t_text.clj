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

  (fact "`words` should return just words (UNICODE)"
    (words "Västerbotten County founded in 1923.") => (just ["Västerbotten" "County" "founded" "in" "1923"]))

  (fact "`words` consistency checks"
    (words "") => empty?
    (words "two") => (just ["two"])
    (words "two three four") => (just ["two" "three" "four"]))

  (future-fact "`words` should skip links"
    (words "Västerbotten County founded in 1923 http://www.bolletinen.") => (just ["Västerbotten" "County" "founded" "in" "1923"])
    (words "Västerbotten County founded in 1923http://www.bolletinen.") => (just ["Västerbotten" "County" "founded" "in" "1923"]))

  (fact "`text->trie` should produce a trie and find within it"
    (text->trie "" 3) => (trie-empty)
    (trie-get (text->trie "foo" 3) "foo") => (just [3])
    (let [trie (text->trie "two three four" 3)]
      (trie-get trie "two") => (just [3])
      (trie-get trie "three") => (just [3])
      (trie-get trie "four") => (just [3])))

  (fact "`text->trie` should filter words with length < 3"
    (let [trie (text->trie "a nice foo, followed by a bar" 3)]
      (trie-get trie "a") => nil
      (trie-get trie "nice") => (just [3])
      (trie-get trie "foo") => (just [3])
      (trie-get trie "followed") => (just [3])
      (trie-get trie "by") => nil
      (trie-get trie "a") => nil
      (trie-get trie "bar") => (just [3])))

  
  )
