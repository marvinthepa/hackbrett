(ns hackbrett.mongo
  (:use somnium.congomongo))

(defn init []
  (mongo! :host "127.0.0.1" :db "hackbrett"))

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
;;
;; buttons
;; {
;;  :scene
;;  :button
;;  :midi-key
;; }
