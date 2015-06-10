(ns wiki-search-xml.common
  (:require [clojure.pprint :as pprint]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [<!! alts!! timeout]]
            [wiki-search-xml.core :as core]))

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
  (let [timeout-ch (timeout timeout-ms)
        [v c] (alts!! [chan timeout-ch] :priority true)]
    (if (= c timeout-ch)
      (do (log/debug "<t!! - timed out on " c)
          (core/->Msg :timeout))
      (do (log/debug "<t!! - received: " v " on " c)
          v))))

