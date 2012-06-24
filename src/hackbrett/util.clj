(ns hackbrett.util
  (:use clojure.pprint))

(defn update-in-wildcard
  "like update-in, but also accepts :_ as a wildcard key.
  If :_ is provided, ALL keys on that level are handled.
  Warning: might explode for big data structures"
  [hmap [key & ks] f & args]
  (if (= key :_) ;; TODO better placeholder?
    (into {}
          (if ks
            (map
              (fn [[k v]]
                [k (apply update-in-wildcard v ks f args)])
              hmap)
            (map
              (fn [[k v]]
                [k (apply f v args)])
              hmap)))
    (if ks
      (assoc hmap key (apply update-in-wildcard (get hmap key) ks f args))
      (do
        (pprint [hmap key args])
        (assoc hmap key (apply f (get hmap key) args))))))
