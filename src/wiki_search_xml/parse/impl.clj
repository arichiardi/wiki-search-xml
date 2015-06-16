(ns wiki-search-xml.parse.impl
  (:require [clojure.tools.trace :refer [deftrace trace] :rename {trace t}]
            [clojure.tools.logging :as log]
            [clojure.data.xml :as xml]
            [clojure.zip :as zip]
            [clojure.string :as string]
            [clojure.data.zip.xml :as zxml]
            [wiki-search-xml.text :as txt]))

(defn- ^:testable xml-text
  [element tag]
  (zxml/text (zxml/xml1-> element (keyword tag))))

(defn- ^:testable strip-wikipedia
  "Strips the word Wikipedia (case insensitive) from s. Returns the rest
  of the text separated by spaces (in order to avoid losing words during
  the join."
  [s]
  (string/join " " (filter (complement empty?) (string/split s #"(?i)wikipedia:"))))

(defn doc->trie
  "Given a wiki doc tag, returns a pair with the trie built around title
  and abstract plus a vector of the trie value/payload in the form
  {:title ...  :abstract ...  :url ...}.

  The title and abstract are lowered cased, concatenated and put into
  the trie. The arity without trie param creates a new another-trie
  while the other appends to an existing one.

  The trie-value-hook function is called on the value payload before being
  inserted in the (immutable) trie in order to add/modify its fields.
  It accepts a record. If no prior manipulation is needed, just pass
  identity. Typically this is used to add db fields."
  ([trie-value-hook another-trie doc]
   (let [title (xml-text doc :title)
         abstract (xml-text doc :abstract)
         url (xml-text doc :url)
         trie-value (trie-value-hook {:title title :abstract abstract :url url})]

     ;; AR - TODO Criterium benchmark for introducing reducers
     [(txt/text->trie another-trie
                      ;; a separator needed in case the title ends by a word (we don't want to lose it)
                      (string/lower-case (str (strip-wikipedia title) " " abstract))
                      trie-value)
      trie-value]))
  ([trie-value-hook doc]
   (doc->trie trie-value-hook (txt/trie-empty) doc)))

(defn wiki-xml->trie
  "Returns a pair containing:
  1) The prefix trie of the contents of the <doc><abstract> and
  <doc><title> xml tags.
  2) The list of all the values/payloads {:title ...  :abstract
  ...  :url ...} inserted in the trie.

  The trie-value-hook function is called on the value payload before
  being inserted in the (immutable) trie in order to add/modify its
  fields. It accepts a trie value. If no prior manipulation is needed,
  just pass identity. Typically this is used to add db fields."
  [trie-value-hook xml-root]
  (let [docs (zxml/xml-> xml-root :doc)]
    (condp = (count docs)
      0 []
      1 (doc->trie trie-value-hook (first docs))
      (do (log/info "wiki-xml->trie - reducing on the <doc> elements, this can take some time")
          (reduce (fn [[acc-trie acc-value] doc]
                    (let [[new-trie new-value] (doc->trie trie-value-hook acc-trie doc)]
                      [new-trie (conj acc-value new-value)]))
                  [(txt/trie-empty) []]
                  docs)))))

(defn wiki-stream->trie
  "Given an already opened stream, builds a pair containing:
  1) The prefix trie of the contents of the <doc><abstract> and
  <doc><title> xml tags.
  2) The list of all the values/payloads {:title ...  :abstract
  ...  :url ...} inserted in the trie.

  The trie-value-hook function is called on the value payload before
  being inserted in the (immutable) trie in order to add/modify its
  fields. It accepts a trie value. If no prior manipulation is needed,
  just pass identity. Typically this is used to add db fields."
  [trie-value-hook xml-stream]
  (wiki-xml->trie trie-value-hook (-> xml-stream xml/parse zip/xml-zip)))
  
