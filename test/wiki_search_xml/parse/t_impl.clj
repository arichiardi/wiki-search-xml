(ns wiki-search-xml.parse.t-impl
  (:require [clojure.pprint :refer [pprint]]
            [clojure.data.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zxml]
            [wiki-search-xml.parse.impl :refer :all]
            [midje.sweet :refer :all]
            [midje.util :refer [expose-testables]]))

(expose-testables wiki-search-xml.parse.impl)

(def test-xml "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
               <foo><bar><baz>The baz value</baz></bar></foo>")

(facts "about `parse.impl`"

  (let [input-xml (java.io.StringReader. test-xml)
        root (-> input-xml xml/parse zip/xml-zip)
        bar (zxml/xml1-> root :bar)]
    (fact "xml-text should return the text of an element"
      (xml-text bar :baz) => "The baz value")


    ))
