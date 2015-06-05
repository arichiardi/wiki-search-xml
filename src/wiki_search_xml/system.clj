(ns wiki-search-xml.system
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [wiki-search-xml
             [log :refer [new-logger]]
             [bus :refer [new-bus]]
             [fetcher :refer [new-fetcher]]
             [search :refer [new-searcher]]]))

(defn make-config
  "Creates a default configuration map."
  []
  {:searcher {} 
   :fetcher {:http-option-map {}}
   :logger {:name (env :wsx-logger-name)}
   :bus {:timeout 1000}
   :version (env :wiki-search-xml-version)})

(defn new-system [config-map]
  (component/system-map
   :wsx-bus (new-bus config-map)
   :wsx-logger (new-logger config-map)
   :wsx-searcher (new-searcher config-map)
   :wsx-fetcher (new-fetcher config-map)
   :wsx-version (:version config-map)))
