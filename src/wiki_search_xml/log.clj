(ns wiki-search-xml.log
  (:require [com.stuartsierra.component :as component]
            [midje.sweet :refer [unfinished]])
  (:import org.slf4j.bridge.SLF4JBridgeHandler
           java.util.logging.Logger))

(declare bridge-jul->slf4j set-default-uncaught-exception-handler)

(defrecord Log [;; config
                name
                ;; instance
                logger]
  component/Lifecycle
  (stop [this]
    (if logger
      (assoc this :logger nil)
      this))
  
  (start [this]
    (if-not logger
      (let [logger (Logger/getLogger name)]
        (bridge-jul->slf4j)
        (set-default-uncaught-exception-handler logger)
        (assoc this :logger logger))
      this)))

(defn new-logger [config]
  (map->Log (:logger config)))

(defn bridge-jul->slf4j
  "Redirects all Java.util.logging logs to sl4fj. Should be called
  upon application startup"
  []
  (SLF4JBridgeHandler/removeHandlersForRootLogger)
  (SLF4JBridgeHandler/install))

(defn set-default-uncaught-exception-handler
  "Installs a default exception handler to log any exception which is
  neither caught by a try/catch nor captured as the result of a
  Future. Should be called upon application startup"
  [logger]
  (Thread/setDefaultUncaughtExceptionHandler
   (reify Thread$UncaughtExceptionHandler
     (uncaughtException [_ thread throwable]
       (.error logger "Uncaught exception" throwable)))))

