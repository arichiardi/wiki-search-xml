(ns wiki-search-xml.common
  (:require [clojure.pprint :as pprint]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [<!! alts!! timeout]]))

(defn lazy?
  [xs]
  (instance? clojure.lang.LazySeq xs))

(defn str-pprint
  "Pretty prints object to a string."
  [object]
  (with-out-str (pprint/pprint object)))

(defn <t!!
  "Takes (blocking) from the input chan waiting for timeout-ms.
  Returns nil when it times out."
  [chan timeout-ms]
  (log/debug "<t!! - input chan" chan " timeout-ms " timeout-ms)
  (let [[v c] (alts!! [chan (timeout timeout-ms)] :priority true)]
    (log/debug "<t!! - returning v: " v " of " c)
    v))
