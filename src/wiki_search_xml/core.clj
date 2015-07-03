(ns wiki-search-xml.core
  (:require [clojure.tools.trace :refer [deftrace trace] :rename {trace t}]
            [clojure.core.async :as async]
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

(defn <!-do-loop!
  "When receiving on channel, executes side-effect (1-arity fn with the
  msg in input), stopping only when it msg is nil (channel
  closed). Returns a channel with the final result of the loop which can
  safely be ignored."
  [channel side-effect]
  (async/go-loop []
    (let [msg (async/<! channel)]
      (when-not (nil? msg)
        (side-effect msg)
        (recur)))))

(defn >!-dispatch-<!-apply!
  "Dispatches the input message on dispatch-chan and waits (parking) for
  results on result-chan (presumably a sub) only if the message has been
  actually sent (async/>! returns true).

  Then, when non-nil data is received on result-chan, applies f to it only
  when (pred msg) yields true, looping if not.

  Returns a channel containing the result of (f msg) or nil.

  Note that by this semantic nil means either the input message could not be
  dispatched in the first place or the result-chan is/has been closed."
  [dispatch-chan result-chan pred f dispatch-msg]
  (async/go
    ;; TODO, replace with offer! with the new release
    (let [put? (async/>! dispatch-chan dispatch-msg)]
      (if put?
        (do (log/debug ">!-dispatch-<!-apply! - message dispatched, now waiting for result channel")
            (loop []
              (let [result-value (async/<! result-chan)]
                (when-not (nil? result-value)
                  (if (pred result-value)
                    (f result-value)
                    (recur))))))
        (log/warn ">!-dispatch-<!-apply! message could not be dispatched")))))

(defn <t!!
  "Takes (blocking) from the input chan waiting for timeout-ms.
  Returns nil when it times out."
  [chan timeout-ms]
  (let [timeout-ch (async/timeout timeout-ms)
        [v c] (async/alts!! [chan timeout-ch] :priority true)]
    (if (= c timeout-ch)
      (do (log/warn "<t!! - timed out on" chan)
          timeout-msg)
      v)))

(defn <t-shut!
  "Takes (parking) from channel but closes it if the take times out,
  returning nil."
  [chan timeout-ms]
  (async/go
    (let [timeout-ch (async/timeout timeout-ms)
          [v c] (async/alts! [chan timeout-ch] :priority true)]
      (if (= c timeout-ch)
        (do (log/warn "<t-shut! - timed out and will close" chan)
            (async/close! chan)
            nil)
        v))))
