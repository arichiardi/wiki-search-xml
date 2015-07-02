(ns wiki-search-xml.fetch
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go >! chan thread] :as async]
            [slingshot.slingshot :refer [try+]]
            [org.httpkit.client :as http]
            [wiki-search-xml.core :as core]
            [nio.core :as nio]))

(defrecord FetchResult [type stream error])

;;;;;;;;;;;;;
;;; fetch ;;;
;;;;;;;;;;;;;

(declare http-callback)

(defmulti fetch!
  "Fetches documents, dispatches on :type and returns result on returned
  channel. The result a FetchResult map which will contain the fetched
  document (stream) or error." :type)

(defmethod fetch! :network-file
  [what]
  ;; HttpKit should not throw but write the error in the response map
  (let [result-ch (async/chan)
        {:keys [type end-point path options]} what]
    (http/get (str end-point (or (first (re-seq #"[/]$" end-point)) "/") path)
              (merge options {:as :stream
                              :start-time (System/currentTimeMillis)
                              :fetch-type type})
              (partial http-callback result-ch))
    result-ch))

(defmethod fetch! :resource-file
  [what]
  (async/thread
    (let [{:keys [type resource-path options]} what]
      (merge (->FetchResult type nil nil)
             (try+
              {:stream (-> resource-path io/resource io/input-stream)}
              (catch Object _
                (let [thr (:throwable &throw-context)]
                  (log/error thr "fetch! error")
                  {:error (.getMessage thr)})))))))

(defmethod fetch! :resource-mmap-file
  [what]
  (async/thread
    (let [{:keys [type resource-path options]} what]
      (merge (->FetchResult type nil nil)
             (try+
              {:stream (-> resource-path io/resource str nio/mmap)}
              (catch Object _
                (let [thr (:throwable &throw-context)]
                  (log/error thr "fetch! error")
                  {:error (.getMessage thr)})))))))

(defmethod fetch! :default
  [_]
  (throw (ex-info "fetch! - you are calling a default implementation, something is wrong"
                  {:ns (ns-name *ns*)
                   :method "fetch"})))

(defn http->fetch-result
  [http-result]
  (let [{:keys [status body error opts]} http-result
        {:keys [fetch-type]} opts]
    (if (and (not error) (= 200 status))
      (->FetchResult fetch-type body nil)
      (->FetchResult fetch-type nil error))))

(defn- http-callback
  "It will be called with the result of the async http fetch."
  [chan http-result]
  (let [{:keys [status headers body error opts]} http-result
        {:keys [method start-time url]} opts]
    (log/debug method url "status" status "took time" (- (System/currentTimeMillis) start-time) "ms"))
  (async/go (async/>! chan (http->fetch-result http-result))))

;;;;;;;;;;;;;
;;; close ;;;
;;;;;;;;;;;;;

(defmulti fetch-close!
  "Closes a fetched document. Side effect, returns nil." :type)

(defmethod fetch-close! :network-file
  [fetch-result]
  (.close (:stream fetch-result)))

(defmethod fetch-close! :resource-file
  [fetch-result]
  (.close (:stream fetch-result)))

(defmethod fetch-close! :resource-mmap-file
  [fetch-result]
  ;; no-op
  )
(defmethod fetch-close! :default
  [_]
  (throw (ex-info "fetch-close! - you are calling a default implementation, something is wrong"
                  {:ns (ns-name *ns*)
                   :method "fetch"})))



