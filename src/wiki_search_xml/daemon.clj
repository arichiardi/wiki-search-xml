(ns wiki-search-xml.daemon
  "Implements the Commons Daemon interface"
  (:require [com.stuartsierra.component :as component]
            [wiki-search-xml.system :as sys])
  (:gen-class :implements [org.apache.commons.daemon.Daemon]))

(def system nil)

(defn -init
  "Initializes a new system given a commons daemon context."
  [_ daemon-context]
  (->> sys/make-config
       sys/new-system
       constantly
       (alter-var-root #'system)))

(defn -start [_]
  (alter-var-root #'system component/start))

(defn -stop [_]
  (alter-var-root #'system component/stop))

(defn -destroy [_]
  (alter-var-root #'system (constantly nil)))

;;----------------Command Line Usage---------------------

;; jsvc \
;; -user blah \
;; -out-file /var/log/blah/out.log \
;; -Xmx3072m \
;; -cp system.jar
;; daemon
;; config.edn
