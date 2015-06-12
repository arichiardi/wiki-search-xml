(ns wiki-search-xml.parser)

(defrecord Parser [;; config
                   ])
  
(defn new-parser [config]
  "Creates an instance of a document fetcher, it accepts a map of additional key/values to be added
  to the request map"
  (map->Parser (:parser config)))

;;;;;;;;;;;;;
;;; parse ;;;
;;;;;;;;;;;;;



