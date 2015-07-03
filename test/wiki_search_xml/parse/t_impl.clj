(ns wiki-search-xml.parse.t-impl
  (:require [clojure.tools.trace :refer [deftrace trace] :rename {trace t}]
            [clojure.java.io :as io]
            [clojure.data.xml :as xml]
            [clojure.zip :as zip]
            [clojure.core.async :as async]
            [clojure.data.zip.xml :as zxml]
            [clojure.string :as string]
            [wiki-search-xml.common :as common]
            [wiki-search-xml.system :as sys]
            [wiki-search-xml.text :as txt]
            [wiki-search-xml.fetch :as fetch]
            [wiki-search-xml.parse.impl :refer :all]
            [midje.sweet :refer :all]
            [midje.util :refer [expose-testables]]
            [criterium.core :refer :all])
  (:import java.io.StringReader))

(expose-testables wiki-search-xml.parse.impl)

(def test-xml "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
               <foo><bar><baz>The baz value</baz></bar></foo>")

(def parsed-nzb {:tag :nzb,
                 :attrs {:xmlns "http://www.newzbin.com/DTD/2003/nzb"},
                 :content [{:tag :foo, :attrs nil, :content ["The foo text"]}
                           {:tag :bar, :attrs nil, :content ["The bar text"]}]})

(defn- words-size>2
  [s]
  (filter #(> (count %1) 2) (txt/words s)))

(facts "about `parse.impl`"

  (fact "strip-wikipedia should behave correctly"
    (strip-wikipedia "Wikipedia: foo bar baz") => #"[ ]*foo bar baz[ ]*"
    (strip-wikipedia "Wikipedia:foo bar baz") => #"[ ]*foo bar baz[ ]*"
    (strip-wikipedia "foo bar") => #"[ ]*foo bar[ ]*")

  (let [test-file (get-in (sys/read-config-file) [:config :test-file])]
    (with-open [r (-> test-file io/resource io/file io/reader)]
      (let [root (xml/parse r)
            doc (second (doall (->> root :content (filter #(= :doc (:tag %))))))
            title (text-of-tag doc :title)
            abstract (text-of-tag doc :abstract)
            url (text-of-tag doc :url)]

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
            title (text-of-tag doc :title)
            abstract (text-of-tag doc :abstract)
            url (text-of-tag doc :url)]

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
        (let [title (text-of-tag doc :title)
              abstract (text-of-tag doc :abstract)
              url (text-of-tag doc :url)]

          (doseq [word (words-size>2 (string/lower-case (strip-wikipedia title)))]
            (fact
              {:midje/description (str "it correctly indexes `" word "` from title")}
              (txt/trie-get file-trie word) => (complement nil?)))

          (doseq [word (words-size>2 (string/lower-case abstract))]
            (fact
              {:midje/description (str "it correctly indexes `" word "` from abstract")}
              (txt/trie-get file-trie word) => (complement nil?))))))))

(facts "about XML accessors"

  (fact "text-of-tag should return the text of an element"
    (text-of-tag parsed-nzb :bar) => "The bar text"
    (text-of-tag parsed-nzb :baz) => empty?)

  (fact "filter-element-by-tag should filter correctly by tag or keyword"
    (filter-element-by-tag parsed-nzb :bar) => #(= (:tag (first %1)) :bar)
    (filter-element-by-tag parsed-nzb "foo") => #(= (:tag (first %1)) :foo)
    (filter-element-by-tag parsed-nzb :baz) => empty?
    (filter-element-by-tag parsed-nzb :bar :foo) => #(every? #{:foo :bar} (map :tag %1))
    (filter-element-by-tag parsed-nzb "baz" :foo) => #(some #{:foo} (map :tag %1))
    (filter-element-by-tag parsed-nzb "baz" :foo) =not=> #(some #{:baz} (map :tag %1)))

  (fact "should return empty string if no tag matches"
    (join-text-of-tag "" parsed-nzb :baz) => "")

  (fact "should merge one tag's text"
    (join-text-of-tag "" parsed-nzb :bar) => "The bar text")

  (fact "should merge many tag's text, order does not count, follows xml"
    (join-text-of-tag "" parsed-nzb :foo :bar) => "The foo textThe bar text"
    (join-text-of-tag "" parsed-nzb :bar :foo) => "The foo textThe bar text"))

(fact "benchmarking `wiki-source->trie-pair`"
  :bench
  (binding [*sample-count* 10] 
    (let [config-map (sys/make-config)
          location (:test-resource-location config-map)]
      (with-progress-reporting
        (quick-bench (when-let [fr (async/<!! (fetch/fetch! location))]
                       (wiki-source->trie-pair identity (:stream fr))
                       (fetch/fetch-close! fr)))))))

;; After removing xml zippers and some redundant call to tuple/vector in text.impl
;; Execution time mean : 7.804550 sec
;;     Execution time std-deviation : 600.689596 ms
;;    Execution time lower quantile : 7.292294 sec ( 2.5%)
;;    Execution time upper quantile : 8.712986 sec (97.5%)
;;                    Overhead used : 1.927362 ns
