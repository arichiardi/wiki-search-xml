(ns wiki-search-xml.system
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [wiki-search-xml
             [log :refer [new-logger]]
             [bus :refer [new-bus]]
             [fetcher :refer [new-fetcher]]
             [search :refer [new-searcher]]]))

(defn- read-config-file []
  (try
    (with-open [r (-> "config.edn" io/resource io/reader (java.io.PushbackReader.))]
      (edn/read r))
    (catch Exception e
      (println (str "WARNING: config.edn error: " (.getLocalizedMessage e))))))

(defn make-config
  "Creates a default configuration map."
  []
  (let [config-edn (:config (read-config-file))]
    {:searcher {} 
     :fetcher {:http-option-map {}}
     :logger {:name (:wsx-logger-name env)}
     :bus {:timeout 1000}
     :version (:wiki-search-xml-version env)
     :test-file (:wsx-test-xml config-edn)}))

(defn new-system [config-map]
  (component/system-map
   :wsx-bus (new-bus config-map)
   :wsx-logger (new-logger config-map)
   :wsx-searcher (new-searcher config-map)
   :wsx-fetcher (new-fetcher config-map)
   :wsx-version (:version config-map)))
