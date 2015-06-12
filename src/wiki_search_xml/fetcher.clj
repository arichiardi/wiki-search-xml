(ns wiki-search-xml.fetcher
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go chan <! >! timeout]]
            [com.stuartsierra.component :as component]
            [org.httpkit.client :as http]
            [wiki-search-xml.core :as core]))

(declare http-callback)

(defrecord Fetcher [;; config
                    type static-options])
  
(defn new-fetcher [config]
  "Creates an instance of a document fetcher, it accepts a map of additional key/values to be added
  to the request map"
  (map->Fetcher (:fetcher config)))

;;;;;;;;;;;;;
;;; fetch ;;;
;;;;;;;;;;;;;

(defmulti fetch
  "Fetches documents, dispatches on :type and returns result on returned
  channel."
  (fn [fetcher url-or-path options] (:type fetcher)))

(defmethod fetch ::network
  [fetcher url-or-path options]
  (log/debug "fetch - :network called")
  (let [result (chan)]
    (http/get url-or-path
              (merge options {:as :stream
                              :start-time (System/currentTimeMillis)}) 
              (partial http-callback result))
    result))

(defmethod fetch ::file
  [fetcher url-or-path options]
  (log/debug "fetch - :file called")
  (go (<! (timeout 1000)) ;; fake sleep
      (log/debug "fetch - sending back the result")
      {:status 200
       :headers {"Date" "Sun, 12 Nov 2015 07:03:49 GMT"
                 "Content-Type" "text/xml; charset=UTF-8"}
       :body (-> url-or-path io/resource io/file io/input-stream)}))

(defmethod fetch :default
  [_ _ _]
  (throw (ex-info "You are calling a default implementation, something is wrong"
                  {:ns (ns-name *ns*)
                   :method "fetch"})))

(defn http-callback
  "It will be called with the result "
  [chan http-result]
  (let [{:keys [status headers body error opts]} http-result
        {:keys [method start-time url]} opts]
    
    (log/debug method url "status" status "took time"
               (- (System/currentTimeMillis) start-time) "ms"))
  
  (go (>! chan (-> (core/->Msg :stream) (merge http-result)))))

;; (defn process!
;;   "Main execution loop"
;;   [^Fetcher this]
;;   (go-loop []
;;     (when (:process! this) 
;;       (let [msg (<! (:subscription this))] 
;;         (log/debug "Message received: " msg)
;;         (condp :type msg
;;           :fetch (fetch this (concat (:search-key msg) (:abstract-path msg)) {})))
;;       (recur))))





