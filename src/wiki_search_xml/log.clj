(ns wiki-search-xml.log
  (:require [com.stuartsierra.component :as component])
  (:import org.slf4j.bridge.SLF4JBridgeHandler))

(declare bridge-jul->slf4j set-default-uncaught-exception-handler)

(defprotocol Log
  "Logs"
  (e [this msg] "Logs Error messages")
  (w [this msg] "Logs Warning messages")
  (i [this msg] "Logs Information messages")
  (d [this msg] "Logs Debug messages")
  (v [this msg] "Logs Verbose messages"))

(defrecord Logger [;; config
                   name
                   ;; instance
                   logger]
  Log
  (e [this msg] (.severe logger msg))
  (w [this msg] (.warning logger msg))
  (i [this msg] (.info logger msg))
  (d [this msg] (.fine logger msg))
  (v [this msg] (.finest logger msg))
  
  component/Lifecycle
  (stop [this]
    (if logger
      (assoc this :logger nil)
      this))
  
  (start [this]
    (if-not logger
      (let [logger (java.util.logging.Logger/getLogger name)]
        (bridge-jul->slf4j)
        (set-default-uncaught-exception-handler logger)
        (assoc this :logger logger))
      this)))

(defn new-logger [config]
  (map->Logger (:logger config)))

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

