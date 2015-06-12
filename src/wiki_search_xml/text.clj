(ns wiki-search-xml.text
  "Text functions"
  (:require [wiki-search-xml.text.impl :refer :all]))


(defn words
  "Parses s and produces a lazy list of words. Removes everything that
  is not a word."
  [s]
  )

(defn words->trie
  "Produces a trie from words, associating the input value to them. If a
  node is given, adds words and value to it."
  ([words value] )
  ([words value node])
  )



