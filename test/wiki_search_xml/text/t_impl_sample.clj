(ns wiki-search-xml.text.t-impl-sample
  (:refer-clojure :exclude [next])
  (:require [wiki-search-xml.text.impl :refer :all]))

;; Print with (str ...) to see a better representation

(def baby-trie (map->Node {:sym \b :child (map->Node {:sym \a :child (map->Node {:sym \b :child (map->Node {:sym \y :values [3]})})})}))
(def baby-bad-trie (map->Node {:sym \b :child (map->Node {:sym \a :child (map->Node {:sym \b :child (map->Node {:sym \y :values [3]}) :next (map->Node {:sym \d :values [4]})})})}))
(def baby-dad-trie (map->Node {:sym \b :child (map->Node {:sym \a :child (map->Node {:sym \b :child (map->Node {:sym \y :values [3]})})})
                               :next (map->Node {:sym \d :child (map->Node {:sym \a :child (map->Node {:sym \d :values [6]})})})}))
(def bad-trie (map->Node {:sym \b :child (map->Node {:sym \a :child (map->Node {:sym \d :values [3]})})}))
(def bad-badly-trie (map->Node {:sym \b :child (map->Node {:sym \a :child (map->Node {:sym \d :values [3] :child (map->Node {:sym \l :child (map->Node {:sym \y :values [4]})})})})}))
(def baby-bad-bank-trie (map->Node {:sym \b :child (map->Node {:sym \a :child (map->Node {:sym \b
                                                                                          :child (map->Node {:sym \y
                                                                                                             :values [3]})
                                                                                          :next (map->Node {:sym \d
                                                                                                            :values [4]
                                                                                                            :next (map->Node {:sym \n
                                                                                                                              :child (map->Node {:sym \k
                                                                                                                                                 :values [5]})})})})})}))
(def baby-bad-bank-badly-trie (map->Node {:sym \b
                                    :child (map->Node {:sym \a
                                                       :child (map->Node {:sym \b
                                                                          :child (map->Node {:sym \y
                                                                                             :values [3]})
                                                                          :next (map->Node {:sym \d
                                                                                            :values [4]
                                                                                            :child (map->Node {:sym \l
                                                                                                               :child (map->Node {:sym \y
                                                                                                                                  :values [6]})})
                                                                                            :next (map->Node {:sym \n
                                                                                                              :child (map->Node {:sym \k
                                                                                                                                 :values [5]})})})})})}))
(def baby-bad-bank-box-trie (map->Node {:sym \b
                                        :child (map->Node {:sym \a
                                                           :next (map->Node {:sym \o
                                                                             :child (map->Node {:sym \x
                                                                                                :values [2]})})
                                                           :child (map->Node {:sym \b
                                                                              :child (map->Node {:sym \y
                                                                                                 :values [3]})
                                                                              :next (map->Node {:sym \d
                                                                                                :values [4]
                                                                                                :next (map->Node {:sym \n
                                                                                                                  :child (map->Node {:sym \k :values [5]})})})})})}))
(def baby-bad-bank-box-dad-dance-trie (map->Node {:sym \b
                                                  :child (map->Node {:sym \a
                                                                     :next (map->Node {:sym \o
                                                                                       :child (map->Node {:sym \x
                                                                                                          :values [2]})})
                                                                     :child (map->Node {:sym \b
                                                                                        :child (map->Node {:sym \y
                                                                                                           :values [3]})
                                                                                        :next (map->Node {:sym \d
                                                                                                          :values [4]
                                                                                                          :next (map->Node {:sym \n
                                                                                                                            :child (map->Node {:sym \k :values [5]})})})})})
                                                  :next (map->Node {:sym \d
                                                                    :child (map->Node {:sym \a
                                                                                       :child (map->Node {:sym \d
                                                                                                          :values [6]
                                                                                                          :next (map->Node {:sym \n
                                                                                                                            :child (map->Node {:sym \c
                                                                                                                                               :child (map->Node {:sym \e
                                                                                                                                                                   :values [7]})})})})})})}))
(def wiki-trie baby-bad-bank-box-dad-dance-trie)

