(ns wiki-search-xml.server.handler
  (:require [clojure.tools.trace :refer [deftrace trace] :rename {trace t}]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [clojure.core.async :as async]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :as http]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [compojure.route :refer [not-found]]
            [compojure.core :refer [routes GET]]
            [cheshire.core :refer [generate-string]]
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
      (let [a (-> (make-routes this)
                  wrap-params
                  wrap-keyword-params
                  wrap-json-response)]
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
  (log/info "received http request with query params" (:query-params req))
  ;; for now the request is handled in a sync way and on the same
  ;; thread (httpkit has 4 thread per request by default, but everything
  ;; is ready for websocket/long polling and return results while they are ready
  (let [{:keys [searcher]} handler
        key (get-in req [:query-params "q"])
        timeout-ch (async/timeout 20000)
        results (let [[res ch] (async/alts!! [(search/search-for searcher (string/lower-case key)) 
                                                   timeout-ch] :priority true)]
                       (if-not (= ch timeout-ch)
                         {:results (or (:result res) [])} ;; TODO better error handling
                         {:results []}))]
    (log/debugf "search key was %s -> generated json contains %s results" key (count (:results results))) 
    {:body (assoc results :q key)}))

;; TODO
;; (defn wrap-async-handler [req]
;;   (http/with-channel req channel
;;     (http/on-close channel (fn [status] (log/warn "HTTP channel closed")))
;;     (if (http/websocket? channel)
;;       (log/debug "WebSocket channel")
;;       (log/debug "HTTP channel"))
;;     #_(http/on-receive channel (fn [data] (http/send! channel data)))))
