(ns hackbrett.test.sound
  (:use [hackbrett.sound])
  (:use [midje.sweet]))

(fact "stupid test"
      (first (make-buttons {39 "blubber"}
                           buttons
                           (atom {"blubber" {:name :blubber}})
                           1)) 
      => {:midi-key 39, :scene 1, :button 1 :sample {:name :blubber}}
      )

(fact "stupider test"
      (add-sample {39 "blubber"}
                  (atom {"blubber" {:name :blubber}})
                  {:midi-key 39})
      => {:midi-key 39 :sample {:name :blubber}})
