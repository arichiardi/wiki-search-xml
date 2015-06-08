(ns wiki-search-xml.common
  (:require [clojure.pprint :as pprint]
            [clojure.core.async :refer [<!! alts!! timeout]]))

(defn lazy?
  [xs]
  (instance? clojure.lang.LazySeq xs))

(defn str-pprint
  "Pretty prints object to a string."
  [object]
  (with-out-str (pprint/pprint object)))

(defn <t!!
  "Takes (blocking) from the input chan waiting for timeout-ms."
  [chan timeout-ms]
  (let [t-chan (timeout timeout-ms)
        [v c] (alts!! [chan t-chan])]
    (if (= c t-chan)
      :!!timed-out!!
      v)))
