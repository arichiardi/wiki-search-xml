(ns wiki-search-xml.text.t-impl
  (:require [midje.sweet :refer :all]
            [midje.util :refer [expose-testables]]
            [wiki-search-xml.text.impl :refer :all]
            [wiki-search-xml.text.t-impl-sample :refer :all])
  (:import java.lang.AssertionError))

(expose-testables wiki-search-xml.text.impl)

(facts "about `text.impl`"

  (fact "`conjm` should create key if not present"
    (conjm {:foo 1} :bar 2) => {:foo 1 :bar [2]})

  (fact "`conjm` should conj if key is there"
    (conjm {:foo 1 :bar [2]} :bar 3) => {:foo 1 :bar [2 3]})

  (fact "`conjm` should work with any key"
    (conjm {"foo" 1} "bar" 2) => {"foo" 1 "bar" [2]})

  (fact "`trie-word-nodes` should create nodes correctly"
    (trie-word-nodes "baby" 3) => baby-trie)

  (fact "`trie-insert-recursive` of 'badly' in 'bad' should create..."
    (trie-insert-recursive bad-trie "badly" 4) => bad-badly-trie)

  (fact "`trie-insert-recursive` of 'bad' in 'baby'"
    (trie-insert-recursive baby-trie "bad" 4) => baby-bad-trie)

  (fact "`trie-insert-recursive` of 'dad' in 'baby'"
    (trie-insert-recursive baby-trie "dad" 6) => baby-dad-trie)

  (fact "`trie-insert-recursive` of 'bank' in 'baby, bad'"
    (trie-insert-recursive baby-bad-trie "bank" 5) => baby-bad-bank-trie)

  (fact "`trie-insert-recursive` of 'badly' in 'baby, bad, bank'"
    (trie-insert-recursive baby-bad-bank-trie "badly" 6) => baby-bad-bank-badly-trie)

  (fact "`trie-insert-recursive` of 'box' in 'baby, bad, bank'"
    (trie-insert-recursive baby-bad-bank-trie "box" 2) => baby-bad-bank-box-trie)

  (fact "`trie-insert-recursive` of 'dad, dance' in 'baby, bad, bank, box'"
    (let [t (trie-insert-recursive baby-bad-bank-box-trie "dad" 6)]
      (trie-insert-recursive t "dance" 7)) => baby-bad-bank-box-dad-dance-trie)

  (fact "`trie-find` on some words always returns the node containing the value"
    (trie-find wiki-trie "baby") => (every-checker (contains {:sym \y :values anything}))
    (trie-find wiki-trie "dad") => (every-checker (contains {:sym \d :values anything}))
    (trie-find wiki-trie "bank") => (every-checker (contains {:sym \k :values anything}))
    (trie-find wiki-trie "zip") => nil)

  (fact "`trie-find` inserting suffix of a word in trie should not interfere"
    (let [song-trie (trie-insert-recursive (map->Node {}) "song" 3)
          song-songs-trie (trie-insert-recursive song-trie "songs" 4)]
      (trie-find song-songs-trie "song") => (contains {:sym \g :values [3]})
      (trie-find song-songs-trie "song") =not=> (contains {:sym \s :values [4]})
      (trie-find song-songs-trie "songs") =not=> (contains {:sym \g :values [3]})
      (trie-find song-songs-trie "songs") => (contains {:sym \s :values [4]})))

  (fact "`trie-find` on some words always returns the value"
    (trie-find wiki-trie "baby") => (contains {:values [3]})
    (trie-find wiki-trie "dad") => (contains {:values [6]})
    (trie-find wiki-trie "zip") => nil)

  (fact "`trie-find` inserting suffix of a word in trie should not interfere"
    (let [song-trie (trie-insert-recursive (map->Node {}) "song" 3)
          song-songs-trie (trie-insert-recursive song-trie "songs" 4)]
      (trie-find song-songs-trie "song") => (contains {:sym \g :values [3]})
      (trie-find song-songs-trie "song") =not=> (contains {:sym \s :values [4]})
      (trie-find song-songs-trie "songs") =not=> (contains {:sym \g :values [3]})
      (trie-find song-songs-trie "songs") => (contains {:sym \s :values [4]})))

  (fact "`trie-insert-recursive` of 'baby' again in 'baby, bad' conjs the value"
    (trie-find (trie-insert-recursive baby-bad-trie "baby" 7) "baby") => (contains {:values [3 7]}))

  (fact "`trie-insert-recursive` inserting song and songs should not conj values"
    (let [song-trie (trie-insert-recursive (map->Node {}) "song" 3)
          song-songs-trie (trie-insert-recursive song-trie "songs" 4)]
      (trie-find song-songs-trie "song") => (contains {:sym \g :values [3]})
      (trie-find song-songs-trie "songs") => (contains {:sym \s :values [4]})))

  (fact "`trie-insert-recursive` of 'dad' again in wiki-trie conjs the value"
    (trie-find (trie-insert-recursive wiki-trie "dad" 7) "dad") => (contains {:values [6 7]}))

  (fact "`trie-insert-recursive` inserting twice should conj values"
    (let [foobar-trie (trie-insert-recursive (map->Node {}) "foobar" 3)
          foobar2-trie (trie-insert-recursive foobar-trie "foobar" 4) ]
      (trie-find foobar2-trie "foobar") => (contains {:values [3 4]}))))

;.;. A journey of a thousand miles begins with a single step. -- @alanmstokes
;.;. TRACE n: nil
;.;. TRACE s: \b



