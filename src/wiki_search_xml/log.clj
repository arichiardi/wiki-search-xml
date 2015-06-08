(ns wiki-search-xml.log
  (:require [com.stuartsierra.component :as component])
  (:import org.apache.logging.log4j.LogManager))

(declare set-default-uncaught-exception-handler)

(defrecord Logger [;; config
                   name
                   ;; instance
                   logger]
  
  component/Lifecycle
  (stop [this]
    (if logger
      (dissoc this :logger)
      this))
  
  (start [this]
    (if-not logger
      (let [logger (LogManager/getRootLogger)]
        ;; (bridge-jul->slf4j)
        (set-default-uncaught-exception-handler logger)
        (assoc this :logger logger))
      this)))

(defn new-logger [config]
  (map->Logger (:logger config)))

;; (defn bridge-jul->slf4j
;;   "Redirects all Java.util.logging logs to sl4fj. Should be called
;;   upon application startup"
;;   []
;;   (SLF4JBridgeHandler/removeHandlersForRootLogger)
;;   (SLF4JBridgeHandler/install))

(defn set-default-uncaught-exception-handler
  "Installs a default exception handler to log any exception which is
  neither caught by a try/catch nor captured as the result of a
  Future. Should be called upon application startup"
  [logger]
  (Thread/setDefaultUncaughtExceptionHandler
   (reify Thread$UncaughtExceptionHandler
     (uncaughtException [_ thread throwable]
       (.fatal logger throwable "Uncaught exception")))))

