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

(defn- ^:testable create-word-nodes
  "Assuming I just want to create a node from a word, creates the
  nodes."
  ([str value]
   (let [rev (reverse str)]
     (create-word-nodes (rest rev) value
                        (conjm (map->Node {:sym (first rev)}) :values value))))

  ([str value acc-node]
   (if (seq str)
     (recur (rest str) value (map->Node {:sym (first str) :child acc-node}))
     acc-node)))

(defn- ^:testable insert-helper
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
  the Node record, which can be anything)."
  [node str value]
  (if-let [char (first str)]
    (if (= char (:sym node))
      (assoc node :child (if-let [child (:child node)]
                           (insert-helper child (rest str) value)
                           (if-let [tail (next str)]
                             (create-word-nodes tail value)
                             (conjm node :values value))))
      (assoc node :next (if-let [next (:next node)]
                          (insert-helper next str value)
                          (create-word-nodes str value))))
    (conjm node :values value)))

(defn trie-insert
  "Insert of the trie. The version without initial node will create a
  trie. Does not bother to handle the insertion of strings less than 3
  character in size. If str exists in the node/tree the value will be
  conjoined to the :values key."
  ([str value] (create-word-nodes str value))
  ([node str value]
   {:pre [(instance? Node node) (> (count str) 2)]}
   (insert-helper node str value)))

(defn- ^:testable trie-find
  "Finds the input str in the node. Returns the Node record of the last
  symbol (the one that containes the values) or nil."
  [node str]
  {:pre [(instance? Node node)]}
  (if-let [char (first str)]
    (if (= char (:sym node))
      (if-let [child (:child node)]
        (trie-find child (rest str))
        node)
      (when-let [next (:next node)]
        (trie-find next str)))
    node))

(defn trie-get
  "Get the value(s) for str or nil. See trie-insert for details on the
  trie implementation."
  [node str]
  (:values (trie-find node str)))
