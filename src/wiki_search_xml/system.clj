(ns wiki-search-xml.system
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.edn :as edn]
            [wiki-search-xml.logger :refer [new-logger]]
            [wiki-search-xml.bus :refer [new-bus]]
            [wiki-search-xml.fetcher :refer [new-fetcher]]
            [wiki-search-xml.searcher :refer [new-searcher]]))

(defn- read-config-file []
  (try
    (with-open [r (-> "config.edn" io/resource io/reader (java.io.PushbackReader.))]
      (edn/read r))
    (catch Exception e
      (log/warn "config.edn error: " (.getLocalizedMessage e)))))

(defn make-config
  "Creates a default configuration map."
  []
  (merge {:searcher {} 
          :fetcher {:type :wiki-search-xml.fetcher/network}
          :logger {:name (:wsx-logger-name env)}
          :bus {:buffer-size 10}
          :version (:wiki-search-xml-version env)}
         (:config (read-config-file))))

(defn new-system [config-map]
  (component/system-map
   :wsx-bus (new-bus config-map)
   :wsx-logger (new-logger config-map)
   :wsx-searcher (new-searcher config-map)
   :wsx-fetcher (new-fetcher config-map)
   :wsx-version (:version config-map)))
