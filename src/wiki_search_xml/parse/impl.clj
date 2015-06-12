(ns wiki-search-xml.parse.impl
  (:require [clojure.data.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zxml]))

(defn- ^:testable xml-text
  [element tag]
  (zxml/text (zxml/xml1-> element (keyword tag))))

;; TODO convert to defmulti?
(defn xml-tags->trie
  "Returns a lazy prefix trie of the content of all the doc in the..."
  [input-stream ks-vec]
  (let [docs (-> input-stream xml/parse zip/xml-zip (zxml/xml-> :doc))
        ]
    
  ))

(defn doc->abstract-trie
  [doc]
  (let [title (xml-text doc :title)
        abstract (xml-text doc :abstract)
        url (xml-text doc :url)
        trie-value {:title title :abstract abstract :url url}]


    )
  )
