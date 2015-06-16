(ns wiki-search-xml.system
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go >!]]
            [com.stuartsierra.component :as component]
            [environ.core :refer [env]]
            [wiki-search-xml.core :as core]
            [wiki-search-xml.bus :refer [new-bus]]
            [wiki-search-xml.parser :refer [new-parser]]
            [wiki-search-xml.logger :refer [new-logger]]
            [wiki-search-xml.searcher :refer [new-searcher]]
            [wiki-search-xml.server :refer [new-server]]
            [wiki-search-xml.server.handler :refer [new-handler]]))

(defn read-config-file []
  (try
    (with-open [r (-> "config.edn" io/resource io/reader (java.io.PushbackReader.))]
      (edn/read r))
    (catch Exception e
      (log/warn "config.edn error: " (.getLocalizedMessage e)))))

(defn make-config
  "Creates a default configuration map."
  []
  (merge {:logger {:name (:wsx-logger-name env)}
          :bus {:bus-conf {:buffer-type :sliding
                           :buffer-size 10}
                :pub-type-conf {:buffer-type :dropping
                                :buffer-size 100}}
          :version (:wiki-search-xml-version env)}
         (:config (read-config-file))))

(defn new-system [config-map]
  (log/infof "creating new system (config: %s)" config-map)
  (component/system-map
   :wsx-bus (new-bus config-map)
   :wsx-logger (new-logger config-map)
   :wsx-searcher (new-searcher config-map)
   :wsx-parser (new-parser config-map)
   :wsx-handler (new-handler config-map)
   :wsx-server (new-server config-map)
   :wsx-version (:version config-map)))

