(ns wiki-search-xml.fetch
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go >! chan thread] :as async]
            [slingshot.slingshot :refer [try+]]
            [org.httpkit.client :as http]
            [wiki-search-xml.core :as core]))

(defrecord Fetch [type options])

(defrecord FetchResult [stream error])

;;;;;;;;;;;;;
;;; fetch ;;;
;;;;;;;;;;;;;

(declare http-callback)

(defmulti fetch
  "Fetches documents, dispatches on :type and returns result on returned
  channel. The result is the typical ring map, where :body contains the
  stream to read from." :type)

(defmethod fetch :network-file
  [what]
  (log/debug "fetch of " what)
  ;; TODO wrap with slingshot's try-catch
  (let [result (async/chan)
        {:keys [end-point path options]} what]
    (http/get (str end-point (or (first (re-seq #"[/]$" end-point)) "/") path)
              (merge options {:as :stream
                              :start-time (System/currentTimeMillis)}) 
              (partial http-callback result))
    result))

(defmethod fetch :resource-file
  [what]
  (log/debug "fetch of" what)
  (let [{:keys [resource-path options]} what] 
    (async/thread (try+
                   (map->FetchResult {:stream
                                      (-> resource-path io/resource io/file io/input-stream)})
                   (catch Object _
                     (let [thr (:throwable &throw-context)] 
                       (log/error thr "Fetching error") 
                       (map->FetchResult {:error (.getMessage thr)})))))))

(defmethod fetch :default
  [_ _ _]
  (throw (ex-info "fetch - you are calling a default implementation, something is wrong"
                  {:ns (ns-name *ns*)
                   :method "fetch"})))

(defn http->fetch-result
  [result]
  (log/debug "fetch-result -" result )
  (let [{:keys [status headers body error opts]} result]
    (if (and (not error) (= 200 status))
      (->FetchResult body nil)
      (->FetchResult nil error))))

(defn- http-callback
  "It will be called with the result of the async http fetch."
  [chan http-result]
  (let [{:keys [status headers body error opts]} http-result
        {:keys [method start-time url]} opts]
    
    (log/debug method url "status" status "took time"
               (- (System/currentTimeMillis) start-time) "ms"))
  
  (async/go (async/>! chan (http->fetch-result http-result))))







