(ns wiki-search-xml.couchdb)

(defrecord Couchdb [;; config
                    end-point
                    ;; dependecies
                    bus
                    ;; state
                    state]
  
   component/Lifecycle
  (stop [this]
    ;; unsubscribe
    (if state 
      #_(do (close! subscription)
          (-> (dissoc this :subscription)
              (dissoc :loop)))
      this))
  
  (start [this]
    (if state
      this
      this
      #_(let [c (chan 1)]
        (subscribe bus :find c)
        
        (assoc this :subscription c)
        
        )))
