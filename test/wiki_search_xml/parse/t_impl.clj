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
            [midje.util :refer [expose-testables]]))

(expose-testables wiki-search-xml.parse.impl)

(def test-xml "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
               <foo><bar><baz>The baz value</baz></bar></foo>")

(defn- words-size>2
  [s]
  (filter #(> (count %1) 2) (txt/words s)))

(facts "about `parse.impl`"

  (fact "strip-wikipedia should behave correctly"
    (strip-wikipedia "Wikipedia: foo bar baz") => (contains "foo bar baz")
    (strip-wikipedia "Wikipedia: foo wikipedia:bar") => #"[ ]+foo[ ]+bar")
  
  (let [input-xml (java.io.StringReader. test-xml)
        root (-> input-xml xml/parse zip/xml-zip)
        bar (zxml/xml1-> root :bar)]
    
    (fact "xml-text should return the text of an element"
      (xml-text bar :baz) => "The baz value"))

  (let [test-file (get-in (sys/read-config-file) [:config :test-file])]
    (with-open [r (-> test-file io/resource io/file io/reader)]
      (let [root (-> r xml/parse zip/xml-zip)
            doc (second (zxml/xml-> root :doc)) ;; better for testing...
            title (xml-text doc :title)
            abstract (xml-text doc :abstract)
            url (xml-text doc :url)]

        (fact "`text->trie` indexes correctly the title"
          (txt/trie-get (txt/text->trie title 42)
                        (rand-nth (words-size>2 title))) => (just 42))

        (fact "`text->trie` indexes correctly the abstract"
          (txt/trie-get (txt/text->trie abstract 42)
                        (rand-nth (words-size>2 abstract))) => (just 42))

        (fact "`text->trie` indexing twice the abstract adds up values"
          (txt/trie-get (txt/text->trie (txt/text->trie abstract 42) abstract 43)
                        (rand-nth (words-size>2 abstract))) => (just [42 43]))

        ;; AR - The metadata facility is very powerful and neat!
        (doseq [word (words-size>2 (string/lower-case (strip-wikipedia title)))]
          (fact
            {:midje/description (str "`doc->trie` correctly indexes `" word "` from title")}
            (txt/trie-get (first (doc->trie identity doc)) word) => (just {:url url
                                                                           :title title
                                                                           :abstract abstract})))

        (doseq [word (words-size>2 (string/lower-case abstract))]
          (fact
            {:midje/description (str "`doc->trie` correctly indexes `" word "` from abstract")}
            (txt/trie-get (first (doc->trie identity doc)) word) => (just {:url url
                                                                           :title title
                                                                           :abstract abstract})))))))

(facts "about `wiki-xml->trie`" :slow

  (let [test-file (get-in (sys/read-config-file) [:config :test-file])]
    (with-open [stream (-> test-file io/resource io/file io/input-stream)]
      (let [root (-> stream xml/parse zip/xml-zip)
            file-trie (first (wiki-xml->trie identity root))] 
        (for [doc (take 20 (repeatedly (rand-nth (zxml/xml-> root :doc))))
              :let [title (xml-text doc :title)
                    abstract (xml-text doc :abstract)
                    url (xml-text doc :url)]]
          (do 
            (doseq [word (words-size>2 (string/lower-case title))]
              (fact
                {:midje/description (str "it correctly indexes `" word "` from title")}
                (txt/trie-get file-trie word) => (just {:url url
                                                        :title title
                                                        :abstract abstract}))) 
            
            (doseq [word (words-size>2 (string/lower-case abstract))]
              (fact
                {:midje/description (str "it correctly indexes `" word "` from abstract")}
                (txt/trie-get file-trie word) => (just {:url url
                                                        :title title
                                                        :abstract abstract})))))))))

