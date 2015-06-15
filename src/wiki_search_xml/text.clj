(ns wiki-search-xml.text
  "Text functions"
  (:require [clojure.tools.trace :refer [deftrace trace] :rename {trace t}]
            [wiki-search-xml.text.impl :as impl]))

(defn words
  "Parses s and produces a lazy list of words. Removes everything that
  is not a word."
  [s]
  (re-seq #"\p{L}+|\w+" s))

(defn trie-empty
  []
  (impl/map->Node {}))

(defn trie-insert
  "Insert of the trie. The version without initial node will create a
  trie. Does not bother to handle the insertion of strings less than 3
  characters in size. If str exists in the node/tree the value will be
  conjoined to the :values key."
  ([] (trie-empty))
  ([str value] (impl/trie-insert-children str value))
  ([node str value]
   {:pre [(> (count str) 2)]}
   (impl/trie-insert-recursive node str value)))

(defn trie-get
  "Get the value(s) for str or nil. See trie-insert for details on the
  trie implementation. This implementation does not bother to handle the
  insertion of strings less than 3 characters in size."
  [node str]
  (:values (impl/trie-find node str)))

(defn text->trie
  "Builds the trie given the text. This implementation will filter
  strings with less than 3 characters. For all words it will append the
  same trie-value."
  ([text value]
   (text->trie (trie-empty) text value))
  ([another-trie text value] 
   (if-let [ws (seq (distinct (filter #(> (count %1) 2) (words text))))]
     (if-not (= (count ws) 1)
       (reduce #(trie-insert %1 %2 value) another-trie ws) 
       (trie-insert (first ws) value))
     (trie-empty))))

;; (defn merge-trie
;;   "Merge for tries. Useful for clojure.core.reducers.
;;   TODO"
;;   ([]
;;    (ex-info "Function not yet implemented")
;;    (trie-empty))
;;   ([& tries] (ex-info "Function not yet implemented")))
