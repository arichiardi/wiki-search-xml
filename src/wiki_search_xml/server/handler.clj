(ns wiki-search-xml.server.handler
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :refer [go >!]]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :as http]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [compojure.route :refer [not-found]]
            [compojure.core :refer [routes GET]]
            [wiki-search-xml.searcher :as search]))

(declare make-routes search-for)

(defrecord Handler [ ;; config
                    options
                    ;; dependecies
                    searcher bus
                    ;; instance
                    app]
  component/Lifecycle

  (stop [this]
    (if app
      (assoc this :app nil)
      this)) 

  (start [this]
    (if app
      this
      (let [a (-> (make-routes this) wrap-params wrap-keyword-params)]
        (assoc this :app a )))))

(defn new-handler
  [config-map]
  (component/using (map->Handler (:handler config-map))
    {:searcher :wsx-searcher
     :bus :wsx-bus}))

(defn make-routes
  [this]
  (let [{:keys [searcher]} this] 
    (routes
     (GET "/search" [] (partial search-for this))
     ;; (GET "/async" [] async-handler) ;; asynchronous(long polling)
     (not-found "<p>Page not found.</p>"))))

(defn search-for [handler req]
  (log/info "received http request" req)

  ;; for now the request is handled in a sync way
  ;; and on the same thread, but everything is ready
  ;; for websocket/long polling and return results
  ;; while they are ready
  (let [{:keys [searcher bus]} handler
        key (get-in req [:query-params :q])]

    (search/search-for searcher key))
  )

;; TODO
;; (defn wrap-async-handler [req]
;;   (http/with-channel req channel
;;     (http/on-close channel (fn [status] (log/warn "HTTP channel closed")))
;;     (if (http/websocket? channel)
;;       (log/debug "WebSocket channel")
;;       (log/debug "HTTP channel"))
;;     #_(http/on-receive channel (fn [data] (http/send! channel data)))))
