(ns hackbrett.sound
  (:require :reload [hackbrett.mapping :as mapping])
  (:require [overtone.core :as overtone])
  (:use [overtone.helpers.file :only [canonical-path]])
  (:use [clojure.tools.logging :only [error warn]]))

(defn init-samples [db-samples]
  (doseq [sample db-samples
          :let [filename (:real-filename sample)]]
    (overtone/load-sample filename)))

(def load-sample overtone/load-sample) ;; TODO metadata

(defn midi-handler [{note :note}]
  (overtone/stop)
  (if-let [filename (canonical-path
                      (mapping/get-sample-filename note))] 
    (overtone/sample-player
      (@overtone/loaded-samples* [filename nil]))
    (warn "no sample for midi note " note)))

(defn init []
  (defonce _auto-boot_ (overtone/boot-internal-server))

  (init-samples (mapping/get-samples))

  (overtone/on-event [:midi :note-on]
                     midi-handler 
                     ::play-sample))
