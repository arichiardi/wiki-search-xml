(ns wiki-search-xml.fetcher
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go go-loop chan <! >! close!]]
            [com.stuartsierra.component :as component]
            [org.httpkit.client :as http]
            [wiki-search-xml.bus :as xbus]
            [wiki-search-xml.core :as xcore]))

(declare process! callback)

;; (defprotocol Fetch
  ;; "Fetches documents"
  ;; (fetch [this url options] "Fetching method, returns a map containing the result "))

(defrecord Fetcher [;; config
                    kind static-options
                    ;; dependecies
                    bus
                    ;; state
                    subscription process!]
  
  component/Lifecycle
  (stop [this]
    ;; unsubscribe
    (if subscription 
      (do (close! subscription)
          (-> this
              (dissoc :subscription)
              (dissoc :process!)))
      this))
  
  (start [this]
    (if subscription
      this
      (let [c (chan 1)]
        (xbus/subscribe bus :find c)
        (-> this 
            (assoc :subscription c)
            (assoc :process! process!))))))

(defn new-fetcher [config]
  "Creates an instance of a document fetcher, it accepts a map of additional key/values to be added
  to the request map"
  (component/using (map->Fetcher (:fetcher config))
    {:bus :wsx-bus}))

(defmulti fetch
  "Fetching method" :kind)

(defmethod fetch :network
  [this url options]
  (log/debug "fetch for network called")
  (let [chan (:bus this)] 
    (http/get url
              (merge options {:as :stream
                              :start-time (System/currentTimeMillis)}) 
              (partial callback chan))))

(defmethod fetch :file [_ path _]
  (log/debug "fetch for files called")
  {:status 200
   :headers {"Date" "Sun, 12 Nov 2015 07:03:49 GMT"
             "Content-Type" "text/xml; charset=UTF-8"}
   :body (-> path io/resource io/file io/input-stream)})

(defmethod fetch :default
  [this url options]
  (throw (ex-info "You are calling a default implementation, something is wrong"
                  {:ns (ns-name *ns*)
                   :method "fetch"})))

(defn callback
  "It will be called with the result "
  [chan http-result]
  (let [{:keys [status headers body error opts]} http-result
        {:keys [method start-time url]} opts]
    (log/debug method url "status" status "took time"
               (- (System/currentTimeMillis) start-time) "ms"))
  
  (go (>! chan (-> (xcore/->Msg :stream) (merge http-result)))))

(defn process!
  "Main execution loop"
  [^Fetcher this]
  (go-loop []
    (when (:process! this) 
      (let [msg (<! (:subscription this))] 
        (log/debug "Message received: " msg)
        (condp :type msg
          :fetch (fetch this (concat (:search-key msg) (:abstract-path msg)) {})))
      (recur))))





