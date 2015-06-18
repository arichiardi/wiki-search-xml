(ns wiki-search-xml.text.t-impl
  (:refer-clojure :exclude [next])
  (:require [midje.sweet :refer :all]
            [midje.util :refer [expose-testables]]
            [wiki-search-xml.text.impl :refer :all]
            [wiki-search-xml.text.t-impl-sample :refer :all]))

(expose-testables wiki-search-xml.text.impl)

(facts "about `text.impl`"

  (fact "`conj-value` should create key if not present"
    (conj-value (map->Node {:sym \g}) 2) => (contains [\g [2]] :gaps-ok))

  (fact "`conj-value` should conj if key is there"
    (conj-value (map->Node {:sym \a :values [2]}) 3) => (contains [\a [2 3]] :gaps-ok))

  (fact "`conj-value` should preserve structure"
    (let [node-with-structure (map->Node {:sym \g :values [1]
                                          :child (map->Node {:sym \u})
                                          :next (map->Node {:sym \p})})]
    (conj-value node-with-structure 2) => (just [\g [1 2] [\u nil nil nil] [\p nil nil nil]])))

  (fact "`trie-insert-childrens` should create nodes correctly"
    (trie-insert-children "baby" 3) => baby-trie)

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

  (fact "`trie-find` on all wiki-trie always returns the node containing the value"
    (trie-find wiki-trie "baby") => (contains \y :gaps-ok)
    (trie-find wiki-trie "bad") => (contains \d :gaps-ok)
    (trie-find wiki-trie "bank") => (contains \k :gaps-ok)
    (trie-find wiki-trie "box") => (contains \x :gaps-ok)
    (trie-find wiki-trie "dad") => (contains \d :gaps-ok)
    (trie-find wiki-trie "dance") => (contains \e :gaps-ok)
    (trie-find wiki-trie "niet") => nil)

  (fact "`trie-find` inserting suffix of a word in trie should not interfere"
    (let [song-trie (trie-insert-recursive (map->Node {}) "song" 3)
          song-songs-trie (trie-insert-recursive song-trie "songs" 4)]
      (trie-find song-songs-trie "song") => (contains [\g [3]] :gaps-ok)
      (trie-find song-songs-trie "song") =not=> (contains [\s [4]]) :gaps-ok
      (trie-find song-songs-trie "songs") =not=> (contains [\g [3]] :gaps-ok)
      (trie-find song-songs-trie "songs") => (contains [\s [4]] :gaps-ok)))

  (fact "`trie-find` on some words always returns the value"
    (trie-find wiki-trie "baby") => (contains [\y [3]] :gaps-ok)
    (trie-find wiki-trie "dad") => (contains [\d [6]] :gaps-ok)
    (trie-find wiki-trie "zip") => nil)

  (fact "`trie-insert-recursive` of 'baby' again in 'baby, bad' conjs the value"
    (trie-find (trie-insert-recursive baby-bad-trie "baby" 7) "baby") => (contains [\y [3 7]] :gaps-ok))

  (fact "`trie-insert-recursive` inserting song and songs should not conj values"
    (let [song-trie (trie-insert-recursive (map->Node {}) "song" 3)
          song-songs-trie (trie-insert-recursive song-trie "songs" 4)]
      (trie-find song-songs-trie "song") => (contains [\g [3]] :gaps-ok)
      (trie-find song-songs-trie "songs") => (contains [\s [4]] :gaps-ok)))

  (fact "`trie-insert-recursive` of 'dad' again in wiki-trie conjs the value"
    (trie-find (trie-insert-recursive wiki-trie "dad" 7) "dad") => (contains [[6 7]] :gaps-ok))

  (fact "`trie-insert-recursive` inserting twice should conj values"
    (let [foobar-trie (trie-insert-recursive (map->Node {}) "foobar" 3)
          foobar2-trie (trie-insert-recursive foobar-trie "foobar" 4)]
      (trie-find foobar2-trie "foobar") => (contains [\r [3 4]] :gaps-ok)))

  (fact "`trie-insert-recursive` inserting a shorter word after a longer should not break"
    (let [robot-trie (trie-insert-recursive (map->Node {}) "roberto" 3)
          robot-roberto-trie (trie-insert-recursive robot-trie "robot" 4)]
      (trie-find robot-roberto-trie "roberto") => (contains [\o [3]] :gaps-ok)))

  (fact "`trie-insert-recursive` inserting twice should conj values"
    (let [wiki2-trie (trie-insert-recursive wiki-trie "baby" 9)]
      (trie-find wiki2-trie "baby") => (contains [\y [3 9]] :gaps-ok)))

  (fact "`trie-insert-recursive` inserting twice should conj values"
    (let [wiki2-trie (trie-insert-recursive wiki-trie "bad" 9)]
      (trie-find wiki2-trie "bad") => (contains [\d [4 9]] :gaps-ok)))

  (fact "`trie-insert-recursive` inserting twice should conj values"
    (let [wiki2-trie (trie-insert-recursive wiki-trie "dad" 7)]
      (trie-find wiki2-trie "dad") => (contains [\d [6 7]] :gaps-ok)))

  (fact "`trie-insert-recursive` inserting twice should conj values"
    (let [wiki2-trie (trie-insert-recursive wiki-trie "bank" 9)]
      (trie-find wiki2-trie "bank") => (contains [\k [5 9]] :gaps-ok)))

  (fact "`trie-insert-recursive` inserting twice should conj values"
    (let [wiki2-trie (trie-insert-recursive wiki-trie "box" 9)]
      (trie-find wiki2-trie "box") => (contains [\x [2 9]] :gaps-ok)))

  (fact "`trie-insert-recursive` inserting twice should conj values"
    (let [wiki2-trie (trie-insert-recursive wiki-trie "dance" 9)]
      (trie-find wiki2-trie "dance") => (contains [\e [7 9]] :gaps-ok))))
