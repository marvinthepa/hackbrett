(ns hackbrett.util)

(defn group-by-single [f coll]
  (reduce
    (fn [acc x]
      (let [k (f x)]
        (if (contains? acc k)
          (throw (RuntimeException. (format "key %s already in collection, only allowed to appear once" x))) 
          (assoc acc k x))))
    {}
    coll))
