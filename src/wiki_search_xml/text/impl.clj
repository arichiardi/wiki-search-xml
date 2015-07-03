(ns wiki-search-xml.text.impl
  "Code for wiki-search-xml.text, including an implementation of a
  prefix tree."
  (:refer-clojure :exclude [empty?] :rename {next cnext, empty? cempty?})
  (:require [clojure.tools.trace :refer [deftrace trace] :rename {trace t}]
            [clj-tuple :as tuple]))

(set! *warn-on-reflection* true)

(defn map->Node
  [{:keys [sym values child next] :or [sym nil values nil child nil next nil]}]
  (tuple/vector sym values child next))

(defn ->Node
  [sym values child next]
  (tuple/vector sym values child next))

(def empty-node (tuple/vector nil nil nil nil))

;; Accessors
(defn sym [node] (nth node 0 nil))
(defn values [node] (nth node 1 nil))
(defn child [node] (nth node 2 nil))
(defn next [node] (nth node 3 nil))

(defn empty? [node] (sym node))

(defn- add-child
  "Adds a new child to node"
  [node new-child]
  (assoc node 2 new-child))

(defn- add-next
  "Adds a new next to node"
  [node new-next]
  (assoc node 3 new-next))

(defn- add-values
  "Adds a new value(s) field to node"
  [node new-values]
  (assoc node 1 new-values))

(defn- ^:testable conj-value
  "Conjoins a value within the node"
  [node value]
  (add-values node (conj (or (values node) (tuple/vector)) value)))

(defn trie-insert-children
  "Assuming I just want to create a node from a word, creates the
  nodes."
  ([^String s value]
   (when-let [rev (reverse s)]
     (trie-insert-children (rest rev) value
                           (conj-value (->Node (first rev) nil nil nil) value))))
  ([s value acc-node]
   (if (seq s)
     (recur (rest s) value (->Node (first s) nil acc-node nil))
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
  [node ^String s v]
  (if-let [symbol (first s)]
    (if (= symbol (sym node))
      (if-let [sym-tail (cnext s)]
        (add-child node (if-let [c (child node)]
                          (trie-insert-recursive c sym-tail v)
                          (trie-insert-children sym-tail v)))
        (conj-value node v))
      (add-next node (if-let [n (next node)]
                       (trie-insert-recursive n s v)
                       (trie-insert-children s v))))
    (trie-insert-children s v)))

(defn trie-find
  "Finds the input s in the node. Returns the Node record of the last
  symbol (the one that containes the values) or nil."
  [node ^String s]
  (if-let [symbol (first s)]
    (if (= symbol (sym node))
      (if-let [sym-tail (cnext s)]
        (if-let [c (child node)]
          (trie-find c sym-tail)
          node)
        node)
      (when-let [n (next node)]
        (trie-find n s)))))
