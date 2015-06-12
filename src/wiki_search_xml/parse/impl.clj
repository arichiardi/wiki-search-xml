(ns wiki-search-xml.parse.impl
  (:require [clojure.tools.trace :refer [deftrace trace] :rename {trace t}]
            [clojure.data.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zxml]
            [wiki-search-xml.text :as txt]))

(defn- ^:testable xml-text
  [element tag]
  (zxml/text (zxml/xml1-> element (keyword tag))))

;; TODO convert to defmulti?
(defn wiki-xml->trie
  "Returns a lazy prefix trie of the content of all the doc in the..."
  [input-stream ks-vec]
  (let [docs (-> input-stream xml/parse zip/xml-zip (zxml/xml-> :doc))
        ]
    
  ))



(defn doc->trie
  "Given a wiki doc tag, returns a vector of the trie value/payload in
  the form {:title ...  :abstract ...  :url ...} plus the trie built
  around title and abstact of the doc itself."
  [doc]
  (let [title (t :title (xml-text doc :title))
        abstract (xml-text doc :abstract)
        url (xml-text doc :url)
        trie-value {:title title :abstract abstract :url url}]

    ;; AR - It would be nice to have some monad here, the maybe monad
    ;; combined with a monad that concatenates builds of the trie.
    ;; AR - TODO Criterium benchmark for introducing reducers
    (let [title-trie (txt/text->trie title trie-value)]
      (txt/text->trie title-trie abstract trie-value))

    )
  )
