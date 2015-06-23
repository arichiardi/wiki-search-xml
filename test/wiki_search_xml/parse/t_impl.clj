(ns wiki-search-xml.parse.t-impl
  (:require [clojure.tools.trace :refer [deftrace trace] :rename {trace t}]
            [clojure.java.io :as io]
            [clojure.data.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zxml]
            [clojure.string :as string]
            [wiki-search-xml.common :as common]
            [wiki-search-xml.system :as sys]
            [wiki-search-xml.text :as txt]
            [wiki-search-xml.parse.impl :refer :all]
            [midje.sweet :refer :all]
            [midje.util :refer [expose-testables]])
  (:import java.io.StringReader))

(expose-testables wiki-search-xml.parse.impl)

(def test-xml "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
               <foo><bar><baz>The baz value</baz></bar></foo>")

(defn- words-size>2
  [s]
  (filter #(> (count %1) 2) (txt/words s)))

(facts "about `parse.impl`"

  (fact "strip-wikipedia should behave correctly"
    (strip-wikipedia "Wikipedia: foo bar baz") => #"[ ]*foo bar baz[ ]*"
    (strip-wikipedia "Wikipedia:foo bar baz") => #"[ ]*foo bar baz[ ]*"
    (strip-wikipedia "foo bar") => #"[ ]*foo bar[ ]*")

  (let [input-xml (StringReader. test-xml)
        root (-> input-xml xml/parse zip/xml-zip)
        bar (zxml/xml1-> root :bar)]

    (fact "xml-text should return the text of an element"
      (xml-text bar :baz) => "The baz value"))

  (let [test-file (get-in (sys/read-config-file) [:config :test-file])]
    (with-open [r (-> test-file io/resource io/file io/reader)]
      (let [root (xml/parse r)
            doc (second (doall (->> root :content (filter #(= :doc (:tag %))))))
            zipped-doc (zip/xml-zip doc)
            title (xml-text zipped-doc :title)
            abstract (xml-text zipped-doc :abstract)
            url (xml-text zipped-doc :url)]

        (fact "`text->trie` indexes correctly the title"
          (txt/trie-get (txt/text->trie title 42)
                        (rand-nth (words-size>2 title))) => (just 42))

        (fact "`text->trie` indexes correctly the abstract"
          (txt/trie-get (txt/text->trie abstract 42)
                        (rand-nth (words-size>2 abstract))) => (just 42))

        (fact "`text->trie` indexing twice the abstract adds up values"
          (txt/trie-get (txt/text->trie (txt/text->trie abstract 42) abstract 43)
                        (rand-nth (words-size>2 abstract))) => (just [42 43]))))))

(facts "about `doc->trie-pair`"
  (let [test-file (get-in (sys/read-config-file) [:config :test-file])]
    (with-open [r (-> test-file io/resource io/file io/reader)]
      (let [doc (second (doall (->> (xml/parse r) :content (filter #(= :doc (:tag %))))))
            zipped-doc (zip/xml-zip doc)
            title (xml-text zipped-doc :title)
            abstract (xml-text zipped-doc :abstract)
            url (xml-text zipped-doc :url)]

        ;; AR - The metadata facility is very powerful and neat!
        (doseq [word (words-size>2 (string/lower-case (strip-wikipedia title)))]
          (fact
            {:midje/description (str "correctly indexes `" word "` from title")}
            (txt/trie-get (trie (doc->trie-pair identity doc)) word) => (just {:url url
                                                                               :title title
                                                                               :abstract abstract})))
        (doseq [word (words-size>2 (string/lower-case abstract))]
          (fact
            {:midje/description (str "correctly indexes `" word "` from abstract")}
            (txt/trie-get (trie (doc->trie-pair identity doc)) word) => (just {:url url
                                                                               :title title
                                                                               :abstract abstract})))))))
(facts "about `wiki-xml->trie-pair`" :slow

  (let [test-file (get-in (sys/read-config-file) [:config :test-file])
        xml-string (slurp (-> test-file io/resource io/file))
        file-trie (trie (wiki-source->trie-pair identity (StringReader. xml-string)))]
    #_(spit "dump.trie" file-trie)

    (let [docs (->> xml-string xml/parse-str :content (filter #(= :doc (:tag %))))]
      (doseq [doc (conj (take 100 docs) (first docs) (last docs))]
        (let [zipped-doc (zip/xml-zip doc)
              title (xml-text zipped-doc :title)
              abstract (xml-text zipped-doc :abstract)
              url (xml-text zipped-doc :url)]

          (doseq [word (words-size>2 (string/lower-case (strip-wikipedia title)))]
            (fact
              {:midje/description (str "it correctly indexes `" word "` from title")}
              (txt/trie-get file-trie word) => (complement nil?)))

          (doseq [word (words-size>2 (string/lower-case abstract))]
            (fact
              {:midje/description (str "it correctly indexes `" word "` from abstract")}
              (txt/trie-get file-trie word) => (complement nil?))))))))
































