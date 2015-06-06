(ns wiki-search-xml.common)

(defn lazy?
  [xs]
  (instance? clojure.lang.LazySeq xs))
