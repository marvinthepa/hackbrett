(ns hackbrett.sound
  (:require [hackbrett.mapping :as mapping])
  (:require [overtone.core :as overtone])
  (:use [overtone.helpers.file :only [canonical-path]])
  (:use [clojure.tools.logging :only [error warn]]))

(defn init-samples [db-samples]
  (doseq [sample db-samples
          :let [filename (:real-filename sample)]]
    (overtone/load-sample filename)))

(def load-sample #'overtone/load-sample)

(defn play-file [filename]
  (if-let [sample (@overtone/loaded-samples*
                     [(canonical-path filename) nil])]
    (overtone/sample-player sample)
    (warn "no sample with filename " filename)))

(defn play-note [note]
  (overtone/stop)
  (if-let [filename (mapping/get-sample-filename note)]
    (play-file filename)
    (warn "no sample for midi note " note)))

(defn midi-handler [{note :note}]
  (play-note note))

(defn init []
  (defonce _auto-boot_ (overtone/boot-internal-server))

  (init-samples (mapping/fetch-samples))

  (overtone/on-event [:midi :note-on]
                     midi-handler
                     ::play-sample))
