(ns wiki-search-xml.text.impl
  "Code for wiki-search-xml.text, including an implementation of a
  prefix tree."
  (:require [clojure.tools.trace :refer [deftrace trace] :rename {trace t}]))

(defrecord Node [sym values child next]
  java.lang.Object
  (toString [_]
    (str "[" (clojure.string/join " " (map str [(or sym "*") (or values "*") (or child "*") (or next "*")])) "]")))

(defn- ^:testable conjm
  "Conjoins a value within the key of a map"
  [m k v]
  (assoc m k (conj (or (get m k nil) []) v)))

(defn trie-insert-children
  "Assuming I just want to create a node from a word, creates the
  nodes."
  ([str value]
   (when-let [rev (reverse str)]
     (trie-insert-children (rest rev) value
                           (conjm (map->Node {:sym (first rev)}) :values value))))

  ([str value acc-node]
   (if (seq str)
     (recur (rest str) value (map->Node {:sym (first str) :child acc-node}))
     acc-node)))

(defn trie-insert-recursive
  "This helper uses direct recursion because the depth will never be
  larger then the word size * number of letter in the alphabet. Does not
  bother to handle the insertion of strings less than 3 character in
  size.

  Following wiki's singly linked implementation, the trie stores nodes
  as follows for {baby, bad, bank, box, dad, dance}:

  b--next----> d
  |            |
  a--next-> o  a
  |         |  |
  b->d->n   x  d->n
  |     |         |
  y     k         c
                  |
                  e

  This avoids wasting space for the arrays of letters and allows to be
  more flexible with dictionaries (the equality is on the :sym field of
  the Node record, which can be anything). The entries are not sorted."
  [node s value]
  (if-let [sym (first s)]
    (if (= sym (:sym node))
      (if-let [sym-tail (next s)]
        (assoc node :child (if-let [child (:child node)]
                             (trie-insert-recursive child sym-tail value)
                             (trie-insert-children sym-tail value))) 
        (conjm node :values value))
      (assoc node :next (if-let [next (:next node)]
                          (trie-insert-recursive next s value)
                          (trie-insert-children s value)))) 
    (trie-insert-children s value)))

(defn trie-find
  "Finds the input s in the node. Returns the Node record of the last
  symbol (the one that containes the values) or nil."
  [node s]
  {:pre [(instance? Node node)]}
  (if-let [sym (first s)]
    (if (= sym (:sym node))
      (if-let [sym-tail (next s)] 
        (if-let [child (:child node)]
          (trie-find child sym-tail)
          node)
        node)
      (when-let [next (:next node)]
        (trie-find next s)))))

