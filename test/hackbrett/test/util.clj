(ns hackbrett.test.util
  (:use [hackbrett.util])
  (:use [midje.sweet]))

(fact "group-by-single throws if keyfn returns the same value twice for a collection"
      (group-by-single identity [1 1]) => (throws RuntimeException))

(fact "group-by-single constructs a map with key defined by function"
      (group-by-single identity [1]) => {1 1}
      (group-by-single :a [{:a 1} {:a 2}]) => {1 {:a 1} 2 {:a 2}}
      )
