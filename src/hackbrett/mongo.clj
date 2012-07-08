(ns hackbrett.mongo
  (:use somnium.congomongo))

(defn get-nano-pad []
  (fetch-one :controllers :where {:name :nano-pad}))

(defn init-nano-pad []
  (when (zero? (fetch-count
                 :controllers
                 :where {:name :nano-pad}))
    (insert! :controllers
             {:name :nano-pad
              :scenes
              [{:scene 1 :buttons [{:button 1, :midi-key 39}
                                   {:button 2, :midi-key 48}
                                   {:button 3, :midi-key 45}
                                   {:button 4, :midi-key 43}
                                   {:button 5, :midi-key 51}
                                   {:button 6, :midi-key 49}
                                   {:button 7, :midi-key 36}
                                   {:button 8, :midi-key 38}
                                   {:button 9, :midi-key 40}
                                   {:button 10, :midi-key 42}
                                   {:button 11, :midi-key 44}
                                   {:button 12, :midi-key 46}]}
               {:scene 2 :buttons [{:button 1, :midi-key 60}
                                   {:button 2, :midi-key 61}
                                   {:button 3, :midi-key 62}
                                   {:button 4, :midi-key 63}
                                   {:button 5, :midi-key 64}
                                   {:button 6, :midi-key 65}
                                   {:button 7, :midi-key 66}
                                   {:button 8, :midi-key 67}
                                   {:button 9, :midi-key 68}
                                   {:button 10, :midi-key 69}
                                   {:button 11, :midi-key 70}
                                   {:button 12, :midi-key 71}]}
               {:scene 3 :buttons [{:button 1, :midi-key 72}
                                   {:button 2, :midi-key 73}
                                   {:button 3, :midi-key 74}
                                   {:button 4, :midi-key 75}
                                   {:button 5, :midi-key 76}
                                   {:button 6, :midi-key 77}
                                   {:button 7, :midi-key 78}
                                   {:button 8, :midi-key 79}
                                   {:button 9, :midi-key 80}
                                   {:button 10, :midi-key 81}
                                   {:button 11, :midi-key 82}
                                   {:button 12, :midi-key 83}]}
               {:scene 4 :buttons [{:button 1, :midi-key 84}
                                   {:button 2, :midi-key 85}
                                   {:button 3, :midi-key 86}
                                   {:button 4, :midi-key 87}
                                   {:button 5, :midi-key 88}
                                   {:button 6, :midi-key 89}
                                   {:button 7, :midi-key 90}
                                   {:button 8, :midi-key 91}
                                   {:button 9, :midi-key 92}
                                   {:button 10, :midi-key 93}
                                   {:button 11, :midi-key 94}
                                   {:button 12, :midi-key 95}]}]})))

(defn init []
  (mongo! :host "127.0.0.1" :db "hackbrett")
  (init-nano-pad))

;; samples
;; {
;;  :id-filename
;;  :real-filename
;; }
;;
;; bindings
;; {
;;  :midi-key
;;  :id-filename
;; }
