(ns wiki-search-xml.parse.t-impl
  (:require [clojure.java.io :as io]
            [clojure.data.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zxml]
            [wiki-search-xml.common :as common]
            [wiki-search-xml.system :as sys]
            [wiki-search-xml.text :as txt]
            [wiki-search-xml.parse.impl :refer :all]
            [midje.sweet :refer :all]
            [midje.util :refer [expose-testables]]))

(expose-testables wiki-search-xml.parse.impl)

(def test-xml "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
               <foo><bar><baz>The baz value</baz></bar></foo>")

(def test-string "Blatnaya pesnya (Russian: Блатна́я пе́сня), or “criminals’ songs,” is a genre of Russian song characterized by depictions of criminal subculture and the urban underworld. These depictions are oftentimes romanticized and humorous in nature.")

(facts "about `parse.impl`"

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
            abstract (xml-text doc :abstract) ]
        
        (fact "`text->trie` indexes correctly the title" 
          (txt/trie-get (txt/text->trie title 42) (rand-nth (txt/words title))) => (just 42))

        (fact "`text->trie` indexes correctly the abstract" 
          (txt/trie-get (txt/text->trie abstract 42) (rand-nth (txt/words abstract))) => (just 42))

        (fact "`text->trie` indexing twice the abstract adds up values" 
          (txt/trie-get (txt/text->trie (txt/text->trie abstract 42) abstract 43)
                        (rand-nth (txt/words abstract))) => (just [42 43]))

        (fact "`doc->trie` correctly indexes from title" 
          (txt/trie-get (doc->trie doc) (rand-nth (txt/words title))) => anything)))))





















































