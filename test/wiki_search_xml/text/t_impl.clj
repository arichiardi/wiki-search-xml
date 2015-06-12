(ns wiki-search-xml.text.t-impl
  (:require [midje.sweet :refer :all]
            [midje.util :refer [expose-testables]]
            [wiki-search-xml.text.impl :refer :all]
            [wiki-search-xml.text.t-impl-sample :refer :all]))

(expose-testables wiki-search-xml.text.impl)

(facts "about `text.impl`"

  (fact "`conjm` should create key if not present"
    (conjm {:foo 1} :bar 2) => {:foo 1 :bar [2]})

  (fact "`conjm` should conj if key is there"
    (conjm {:foo 1 :bar [2]} :bar 3) => {:foo 1 :bar [2 3]})

  (fact "`conjm` should work with any key"
    (conjm {"foo" 1} "bar" 2) => {"foo" 1 "bar" [2]})

  (fact "`create-word-nodes` should create nodes correctly"
    (create-word-nodes "baby" 3) => baby-trie)

  (fact "`trie-insert` on empty should create b node"
    (trie-insert "b" 1) => (map->Node (map->Node {:sym \b :values [1]})))

  (fact "`trie-insert` of 'baby' should create b with child a with child b..."
    (trie-insert "baby" 3) => baby-trie)

  (fact "`trie-insert` of 'badly' in 'bad' should create..."
    (trie-insert "bad" 3) => bad-trie
    (trie-insert bad-trie "badly" 4) => bad-badly-trie)

  (fact "`trie-insert` of 'bad' in 'baby'"
    (trie-insert baby-trie "bad" 4) => baby-bad-trie)

  (fact "`trie-insert` of 'dad' in 'baby'"
    (trie-insert baby-trie "dad" 6) => baby-dad-trie)

  (fact "`trie-insert` of 'bank' in 'baby, bad'"
    (trie-insert baby-bad-trie "bank" 5) => baby-bad-bank-trie)

  (fact "`trie-insert` of 'badly' in 'baby, bad, bank'"
    (trie-insert baby-bad-bank-trie "badly" 6) => baby-bad-bank-badly-trie)

  (fact "`trie-insert` of 'box' in 'baby, bad, bank'"
    (trie-insert baby-bad-bank-trie "box" 2) => baby-bad-bank-box-trie)

  (fact "`trie-insert` of 'dad, dance' in 'baby, bad, bank, box'"
    (let [t (trie-insert baby-bad-bank-box-trie "dad" 6)] 
      (trie-insert t "dance" 7)) => baby-bad-bank-box-dad-dance-trie)

  (fact "`trie-find` on some words always returns the node containing the value"
    (trie-find wiki-trie "baby") => (every-checker (contains {:sym \y :values anything}))
    (trie-find wiki-trie "dad") => (every-checker (contains {:sym \d :values anything}))
    (trie-find wiki-trie "bank") => (every-checker (contains {:sym \k :values anything}))
    (trie-find wiki-trie "zip") => nil)

  (fact "`trie-get` on some words always returns the value"
    (trie-get wiki-trie "baby") => (just [3])
    (trie-get wiki-trie "dad") => (just [6])
    (trie-get wiki-trie "zip") => nil)

  (fact "`trie-insert` of 'baby' again in 'baby, bad' conjs the value"
    (trie-get (trie-insert baby-bad-trie "baby" 7) "baby") => (just [3 7]))

  (fact "`trie-insert` of 'dad' again in wiki-trie conjs the value"
    (trie-get (trie-insert wiki-trie "dad" 7) "dad") => (just [6 7]))
  
  )

