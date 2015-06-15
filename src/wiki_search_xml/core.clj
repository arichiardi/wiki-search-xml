(ns wiki-search-xml.core
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :as log]))

(defrecord Msg [type])

(def timeout-msg (->Msg :timeout))

(def shutdown-msg (->Msg :shutdown))

(defn shutdown?
  "Is shutting down?"
  [msg]
  (= (:type msg) :shutdown))

(defn loop!
  "Starts an async execution go-loop on the sub(scripted)-channel,
  pausing every polling-ms.

  Returns a channel with the final result of the loop which can safely
  be ignored.

  It executes side-effect (1-arity fn with the msg in input) at every
  loop, stopping only when it receives a nil msg (channel closed)."
  [sub-channel side-effect]
  (async/go-loop [sub sub-channel]
    (when-let [msg (async/<! sub)]
      (do (log/info "executing side-effect for" msg)
          (side-effect msg)
          (recur sub)))))
