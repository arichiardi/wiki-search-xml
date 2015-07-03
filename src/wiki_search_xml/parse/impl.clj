(ns wiki-search-xml.parse.impl
  (:require [clojure.tools.trace :refer [deftrace trace] :rename {trace t}]
            [clojure.tools.logging :as log]
            [clojure.core.async :as async]
            [clojure.core.reducers :as r]
            [clojure.data.xml :as xml]
            [clojure.string :as string]
            [wiki-search-xml.text :as txt]))

;;;;;;;;;;;;;;;;;;;;;;;
;;; Trie generation ;;;
;;;;;;;;;;;;;;;;;;;;;;;

(defn- ^:testable
  filter-element-by-tag
  "Lazily returns the elements that match the (variadic)
  keywords-or-tags."
  [element & keywords-or-tags]
  (let [tag-set (set (map keyword keywords-or-tags))]
    (->> element :content (filter #(tag-set (:tag %1))))))

(defn- ^:testable
  join-text-of-tag
  "Returns the text of the children of element with tags matching
  the (variadic) keywords-or-tags. The separator can be used to
  differentiate them. The order of the texts does not follow the input
  tags but the xml."
  [separator element & keywords-or-tags]
  (string/join separator (map (comp first :content) (apply filter-element-by-tag element keywords-or-tags))))

(defn- ^:testable
  text-of-tag
  [element keyword-or-tag]
  (first (:content (first (filter-element-by-tag element keyword-or-tag)))))

(defn- ^:testable
  strip-wikipedia
  "Strips the initial Wikipedia: (case insensitive) from s. Returns the rest
  of the text separated by spaces (in order to avoid losing words during
  the join."
  [^String s]
  (string/replace-first s #"(?i)^wikipedia:" ""))

(defn- ^:testable
  doc->trie-pair
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
  ([trie-value-hook another-trie doc-element]
   (let [title (text-of-tag doc-element :title)
         abstract (text-of-tag doc-element :abstract)
         url (text-of-tag doc-element :url)
         trie-value (trie-value-hook {:title title :abstract abstract :url url})]
     [(txt/text->trie another-trie
                      ;; a separator needed in case the title ends by a word (we don't want to lose it)
                      (string/lower-case (str (strip-wikipedia title) " " abstract))
                      trie-value)
      trie-value]))
  ([trie-value-hook doc]
   (doc->trie-pair trie-value-hook (txt/trie-empty) doc))
  ([trie-value-hook]
   (txt/trie-empty)))

(defn wiki-xml->trie-pair
  "Returns a pair containing:
  1) The prefix trie of the contents of the <doc><abstract> ...
  <title></doc> xml tags.
  2) The list of all the values/payloads {:title ...  :abstract
  ...  :url ...} inserted in the trie.

  The trie-value-hook function is called on the value payload before
  being inserted in the (immutable) trie in order to add/modify its
  fields. It accepts a trie value. If no prior manipulation is needed,
  just pass identity. Typically this is used to add db fields."
  [trie-value-hook xml-root]
  (let [docs (->> xml-root :content (filter #(= :doc (:tag %))))]
    (if-not (= 1 (count docs))
      (do (log/info "wiki-xml->trie-pair - reducing on the <doc> elements, this can take some time")
          (doall (reduce (fn [[acc-trie acc-values] doc]
                           (let [[new-trie new-value] (doc->trie-pair trie-value-hook acc-trie doc)]
                             [new-trie (conj acc-values new-value)]))
                         [(txt/trie-empty) []]
                         docs)))
      (doc->trie-pair trie-value-hook (txt/trie-empty) (first docs)))))

(defn wiki-source->trie-pair
  "Given an already opened stream, builds a pair containing:
  1) The prefix trie of the contents of the <doc><abstract> ...
  <title></doc> xml tags.
  2) The list of all the values/payloads {:title ...  :abstract
  ...  :url ...} inserted in the trie.

  The trie-value-hook function is called on the value payload before
  being inserted in the (immutable) trie in order to add/modify its
  fields. It accepts a trie value. If no prior manipulation is needed,
  just pass identity. Typically this is used to add db fields."
  [trie-value-hook source]
  (wiki-xml->trie-pair trie-value-hook (xml/parse source)))

(def trie
  "Given a trie pair, returns the trie"
  first)

(def values
  "Given a trie pair, returns the list of all its values"
  second)

;;;;;;;;;;;;;;;;;;;;
;;; Parse result ;;;
;;;;;;;;;;;;;;;;;;;;

(defrecord ParseData [state data error read-channel write-channel mult])

(def ^:private empty-data (->ParseData :new nil nil nil nil nil))

(defn empty-data
  "An empty ParseData with state :new"
  []
  empty-data)

(defn parsing?
  [parse-data]
  (= :parsing (:state parse-data)))

(defn waiting?
  [parse-data]
  (= :waiting (:state parse-data)))

(defn parsed?
  [parse-data]
  (= :parsed (:state parse-data)))

(def read-channel
  "Given a parse data record, returns its result channel."
  :read-channel)

(def write-channel
  "Given a parse data record, returns its result channel."
  :write-channel)

(defn result
  "Given a parse data record, returns just its data and error (in a
  map)."
  [{:keys [data error] :or {data nil error nil}}]
  {:data data
   :error error})

(defn error?
  [parse-data]
  (:error (result parse-data)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Parse state machine ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- ->parsing
  "Given all the needed channels, returns a record whose state
  is :parsing."
  [read-ch write-ch mult]
  (->ParseData :parsing nil nil read-ch write-ch mult))

(defn- ->waiting
  "Returns :waiting record for when we don't parse but wait for results."
  [read-ch mult]
  (->ParseData :waiting nil nil read-ch nil mult))

(defn data->parsed
  "Given :data or :error, returns a record whose state is :parsed."
  [{:keys [data error] :or {data nil error nil}}]
  (->ParseData :parsed data error nil nil nil))

(defn update-parse-state
  "Produces a new ParseData based on the current :state following the
  :new->:parsing->:waiting->:parsed flux. This avoids parsing a resource
  twice when concurrent requests are coming. In order to achieve that,
  this function should be used with swap! or alter."
  [old-pd]
  (cond
    (and (parsed? old-pd) (not (error? old-pd))) old-pd ;; if there was an error I will parse again
    (or (waiting? old-pd) (parsing? old-pd)) (let [m (:mult old-pd)]
                                               (->waiting (async/tap m (async/chan 1)) m))
    ;; else setting up the "parsing" state and channels
    ;; I expect just one result of the parsing.
    :else (let [c (async/chan 1)
                m (async/mult c)]
            (->parsing (async/tap m (async/chan 1)) c m))))
