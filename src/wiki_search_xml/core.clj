(ns wiki-search-xml.core
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :as log]))

(defrecord Msg [type])

(def timeout-msg (->Msg :timeout))

(defrecord DataMsg [type class data error])

(defn ->DataMsg*
  [{:keys [class data error] :or {class :unknown data nil error nil}}]
  (->DataMsg :data class data error))

(defn msg->DataMsg
  "Converts any message into a ResultMsg, overriding :type and
  adding :data or :error."
  [another-msg {:keys [class data error] :or {class :unknown data nil error nil}}]
  (merge another-msg (->DataMsg :data class data error)))

(defn loop!
  "Starts an async execution go-loop on the sub(scripted)-channels,
  giving priority to their order.

  Returns a channel with the final result of the loop which can safely
  be ignored.

  It executes side-effect (1-arity fn with the msg in input) at every
  loop, stopping only when it receives a nil msg (channel closed)."
  [side-effect & sub-channels]
  (async/go-loop []
    (let [[msg channel] (async/alts! (vec sub-channels) :priority true)]
      (when msg
        (do (side-effect msg)
            (recur))))))
