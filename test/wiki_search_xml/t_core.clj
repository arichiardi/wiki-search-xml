(ns wiki-search-xml.t-core
  (:require [wiki-search-xml.core :refer :all]
            [midje.sweet :refer :all]
            [wiki-search-xml.common :as common]
            [clojure.core.async :refer [chan go <! <!! >! >!! timeout pub sub]]))

(facts "about `core`"

  (fact "`<t!!` return the right value or times out"
    (let [c1 (chan)]
      (do (go (>! c1 "test")) (<t!! c1 1000)) => "test"
      (do (go (do (<! (timeout 500)) (>! c1  "test"))) (<t!! c1 1000)) => "test"
      (do (go (do (<! (timeout 2000)) (>! c1  "test"))) (<t!! c1 1000)) => timeout-msg
      (<t!! c1 10) => timeout-msg))

  (fact "<!-do-loop! "
    (let [ch1 (chan 1)
          a (atom 0)]
      (<!-do-loop! ch1 (swap! a inc))
      @a => 1))

  (fact "<!-do-loop! correctly executes side effect more than once "
    (let [ch1 (chan 1)
          a (atom 0)]
      (<!-do-loop! ch1 (fn [_] (swap! a inc)))
      (>!! ch1 "1")
      (>!! ch1 "2")
      (>!! ch1 "3")
      (>!! ch1 "4")
      @a => 4))

  (future-fact "<!-do-loop! correctly skips msgs if channel is a sub"
    (let [ch1 (chan 6)
          p1 (pub ch1 :target)
          s1 (chan 6)
          _ (sub p1 :me s1)
          a (atom 0)]
      (<!-do-loop! s1 (fn [_] (swap! a inc)))
      (>!! ch1 {:target :me})
      (>!! ch1 {:target :you})
      (>!! ch1 {:target :her})
      (>!! ch1 {:target :me})
      (>!! ch1 {:target :them})
      (>!! ch1 {:target :me})
      @a => 3))

  (fact ">!-dispatch-<!-apply! correctly dispatches on the first chan"
    (let [ch1 (chan 1)
          ch2 (chan 1)]
      (>!-dispatch-<!-apply! ch1 ch2  identity identity {:what :test})
      (<t!! ch1 250) => {:what :test}))

  (fact ">!-dispatch-<!-apply! correctly receives on the second chan"
    (let [ch1 (chan 1)
          ch2 (chan 1)
          out (>!-dispatch-<!-apply! ch1 ch2
                                     #(= :test (:what %1))
                                     #(assoc % :success true)
                                     {:what :dispatch})]
      (go (>! ch2 {:what :test}))
      (<t!! out 250) => (just {:what :test :success true})))

  (fact ">!-dispatch-<!-apply! does not receive messages not conforming the predicate"
    (let [ch1 (chan 1)
          ch2 (chan 1)
          out (>!-dispatch-<!-apply! ch1 ch2
                                     #(= :test (:what %1))
                                     #(assoc % :success true)
                                     {:what :dispatch})]

      (go (>! ch2 {:what :not-for-me}))
      (<t!! out 250) => timeout-msg))

  (fact ">!-dispatch-<!-apply! skips messages not conforming the predicate"
    (let [ch1 (chan 1)
          ch2 (chan 2)
          out (>!-dispatch-<!-apply! ch1 ch2
                                     #(= :test (:what %1))
                                     #(assoc % :success true)
                                     {:what :dispatch})]

      (go (>! ch2 {:what :not-for-me})
          (<! (timeout 500))
          (>! ch2 {:what :test}))
      (<t!! out 750) => (just {:what :test :success true})))

  (fact "<t-shut! if receiving within timeout everything is fine"
    (let [ch1 (chan 1)
          out (<t-shut! ch1 500)]

      (go (<! (timeout 250))
          (>! ch1 {:what :test}))
      (<t!! out 550) => (just {:what :test})))

  (fact "<t-shut! if receiving after timeout is triggered channel is closed (returns nil)"
    (let [ch1 (chan 1)
          out (<t-shut! ch1 100)]
      (<!! (timeout 500))
      (>!! ch1 {:what :test}) => false
      (<t!! ch1 250) => nil
      (<t!! ch1 250) => nil
      (<t!! ch1 250) => nil)))



