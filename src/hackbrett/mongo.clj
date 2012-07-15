(ns hackbrett.mongo
  (:use somnium.congomongo))

(defn init-buttons []
  (let [pad-keys
        [39 48 45 43 51 49 36 38 40 42 44 46 ; scene 1
         60 61 62 63 64 65 66 67 68 69 70 71 ; scene 2
         72 73 74 75 76 77 78 79 80 81 82 83 ; scene 3
         84 85 86 87 88 89 90 91 92 93 94 95],  ; scene 4
        buttons (map (fn [hmap midi-key]
                       (assoc hmap
                              :midi-key midi-key
                              :controller :nano-pad))
                  (for [scene (range 1 5)
                        button (range 1 13)]
                    {:scene scene :button button})
                  pad-keys)]
    (when (zero? (fetch-count :buttons :where {:controller :nano-pad}))
      (mass-insert! :buttons
                    buttons))))

(defn init []
  (mongo! :host "127.0.0.1" :db "hackbrett")
  (init-buttons))
