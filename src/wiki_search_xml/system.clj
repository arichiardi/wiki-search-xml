(ns wiki-search-xml.system
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [wiki-search-xml
             [fetcher :refer [new-fetcher]]
             [search :refer [new-searcher]]]))

(defn make-config
  "Creates a default configuration map."
  []
  {:searcher {} 
   :fetcher {:http-option-map {}} 
   :version (env :wiki-search-xml-version)})

(defn new-system [config-map]
  (component/system-map 
   :web-searcher (new-searcher config-map)
   :web-fetcher (new-fetcher config-map)
   :sys-version (:version config-map)))
