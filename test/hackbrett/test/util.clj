(ns hackbrett.test.util
  (:use [hackbrett.util])
  (:use [midje.sweet]))

(tabular
  (fact "update without wildcards behaves like update-in"
        (apply update-in-wildcard ?args) => (apply update-in ?args))
  ?args
  [{} [] identity]
  [{:a 1} [:a] identity]
  [{:a 1} [:a] (constantly 2)]
  [{:a 1 :b 2} [:a] (constantly 2)]
  [{:a {:b {:c 2}}} [:a :b :c] inc]
  )


(fact "should throw the same exceptions as update-in"
  (update-in-wildcard
    {:a {:b 1}} [:a :a] inc) => (throws NullPointerException))

(fact "incompatible levels throw (compatible with update-in)"
      (update-in-wildcard {:a {:b 1}} [:_ :a] inc)
      =>                  (throws NullPointerException))

(fact "when levels are missing, hashmaps are created"
      (update-in-wildcard {} [:a :b] (constantly 1)) => {:a {:b 1}}
      (update-in-wildcard {:a {}} [:_ :b] (constantly 1)) => {:a {:b 1}})

(fact "missing levels with wildcard expressions are not created (i.e. map is not updated)"
      (update-in-wildcard {} [:_ :a] (constantly 1)) => {})

(fact "when used with a wildcard, all the keys on that level are updated"
      (update-in-wildcard {:a 1 :b 2} [:_] inc)
      =>                  {:a 2 :b 3}
      (update-in-wildcard {:a {:a 1} :b {:a 1}} [:_ :a] inc)
      =>                  {:a {:a 2} :b {:a 2}}
      (update-in-wildcard {:a {:b 1} :b {:a 1}} [:_ :_] inc)
      =>                  {:a {:b 2} :b {:a 2}}
      (update-in-wildcard {:a {:b 1} :b {:a 1}} [:b :_] inc)
      =>                  {:a {:b 1} :b {:a 2}}
      (update-in-wildcard {:a {:b 1} :b {:a 1 :c 2}} [:b :_] inc)
      =>                  {:a {:b 1} :b {:a 2 :c 3}})
