(ns user
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [wiki-search-xml.system :refer (new-system)]
            [midje.repl :as mr]))

(def system nil)

(defn init
  "Constructs the current development system."
  []
  (println "Initializing system...")
  (alter-var-root #'system
    (constantly (new-system {}))))

(defn start
  "Starts the current development system."
  []
  (println "Starting system...")
  (alter-var-root #'system component/start))

(defn stop
  "Shuts down and destroys the current development system."
  []
  (println "Stopping system...")
  (alter-var-root #'system
                  (fn [s] (when s (component/stop s)))))

(defn go
  "Initializes the current development system and starts it running."
  []
  (println "Go!")
  (init)
  (start))

(defn reset []
  (println "Resetting system...")
  (stop)
  (refresh :after 'user/go))

(defn autotest
  []
  (println "Starting automatic tests...")
  (midje.repl/autotest))


