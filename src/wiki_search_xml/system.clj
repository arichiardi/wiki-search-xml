(ns wiki-search-xml.system
  (:require [com.stuartsierra.component :as component]
            [environ.core :refer [env]]))

(defn new-system [config-map]
  (let [{:keys [enpoint path]} config-map]
    (component/system-map 
     :app "My app" 
     :version (env :wiki-search-xml-version))))
