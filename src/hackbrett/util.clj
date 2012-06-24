(ns hackbrett.util)

(defn update-in-wildcard
  "like update-in, but also accepts :_ as a wildcard key.
  If :_ is provided, ALL keys on that level are handled.
  Warning: might explode for big data structures"
  ([hmap [key & keys] f & args]
   (if (= key :_) ;; TODO better placeholder?
     (into {}
           (if keys
             (map
               (fn [[key val]]
                 [key
                  (apply update-in
                         val
                         keys f args)]) 
               hmap)
             (map
               (fn [[key val]]
                 [key (apply f val args)]) 
               hmap))) 
     (if keys
       (assoc hmap key
              (apply update-in-wildcard (get hmap key) keys f args))
       (assoc hmap key
              (apply f (get hmap key) args))))))
