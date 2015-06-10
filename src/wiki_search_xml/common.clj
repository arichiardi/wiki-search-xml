(ns wiki-search-xml.common
  (:require [clojure.pprint :as pprint]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [<!! alts!! timeout]]
            [com.stuartsierra.component :as component]
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
      (do (log/debug "<t!! - timed out: val" v "on" chan)
          (core/->Msg :timeout))
      (do (log/debug "<t!! - received: val" v "on" c)
          v))))

(defrecord DummyComponent [name deps]
  component/Lifecycle
  (start [this] (do (log/debug "DummyComponent " name "started") (assoc this :started true)))
  (stop [this] (do (log/debug "DummyComponent " name "stopped") (assoc this :started false))))

(defn new-dummy-component [name & deps]
  (component/using (->DummyComponent name deps)
    (vec deps)))

(defmacro with-component-start
  "Starts and stops the component around the execution (do
  body). Exposes the anaphoric symbol started-system! and returns the result
  of body's last sexp. The stopped system is not returned."
  [component & body]
  `(let [~'__started__ (component/start ~component)
         result# (do ~@body)]
     (component/stop ~'__started__)
     result#))
