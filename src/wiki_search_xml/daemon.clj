(ns wiki-search-xml.daemon
  "Implements the Commons Daemon interface"
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [slingshot.slingshot :refer [try+ throw+]]
            [wiki-search-xml.system :as sys])
  (:gen-class :implements [org.apache.commons.daemon.Daemon]))

(def system nil)

(defn -init
  "Initializes a new system given a commons daemon context."
  [_ daemon-context]
  (->> (sys/make-config)
       (sys/new-system)
       (constantly)
       (alter-var-root #'system)))

(defn -start [_]
  (try+
   (alter-var-root #'system component/start)
   (catch Object _
     (let [thr (:throwable &throw-context)]
       (log/error thr "Component error on start")
       (throw+)))))

(defn -stop [_]
  (try+
   (alter-var-root #'system (fn [sys] (when sys (component/stop sys))))
   (catch Object _
     (let [thr (:throwable &throw-context)]
       (log/error thr "Component error on stop")
       (throw+)))))

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
