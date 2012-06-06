(ns hackbrett.sound
  (:use overtone.live)
  (:use [clojure.tools.logging :only [error]])
  (:use [clojure.java.io :only [copy output-stream]]))

;(use 'overtone.live) ;; TODO only for repl
;(use 'clojure.repl 'clojure.pprint)

; http://dailywav.com/search/results.php
(def samples (load-samples "sounds/*.wav"))

(defonce sample-map
  (atom
    (into {} 
          (map (fn [x] [(:id x) x]) samples)))) 

;; TODO make all those locals, not defs
;;;;; begin should-be-locals
(def pad-keys
  [39 48 45 43 51 49 36 38 40 42 44 46 ; scene 1
   60 61 62 63 64 65 66 67 68 69 70 71 ; scene 2
   72 73 74 75 76 77 78 79 80 81 82 83 ; scene 3
   84 85 86 87 88 89 90 91 92 93 94 95]) ; scene 4 
(def midi-key-to-sample
  (remove nil?
          (map-indexed (fn [index y]
                         (let [nsamples (count samples)]
                           (when (< index nsamples)
                             [y (:id (nth samples index))])))
                       pad-keys)))
(def buttons
  (into {}
        (map
          (fn [x midi-key]
            [midi-key (assoc x :midi-key midi-key)])
          (for [scene (range 1 5)
                button (range 1 13)]
            {:scene scene :button button})
          pad-keys)))
(def samples-buttons-scenes
  (->> midi-key-to-sample 
    (map (fn [[k v]]
           (into
             (assoc (@sample-map v)
                    :key k)
             (buttons k))))))
;;;;; end should-be-locals

(def scene-button-id
  (atom
    (reduce (fn [agg {:keys [scene button] :as sample}]
              (assoc-in agg
                        [scene button]
                        (select-keys sample
                                     [:scene :button :name :id :midi-key]))) 
            {} 
            samples-buttons-scenes))) 

(def num-of-midi-channels 128)

(defonce ^:private silent-buffer (buffer 0))
;; a buffer for the instrument to index into
(defonce ^:private index-buffer
  (let [tab midi-key-to-sample
        buf (buffer 128)]
    (buffer-fill! buf (:id silent-buffer))
    (doseq [[idx val] tab]
      (buffer-set! buf idx val))
    buf))

(definst play-sample [note 36]
  (let [buf (index:kr (:id index-buffer) note)]
    (scaled-play-buf 2 buf :level 1 :loop 0 :action FREE :start-pos 0)))

;; handle the pad
(on-event [:midi :note-on] (fn [{note :note}]
                             (stop)
                             (play-sample note))
          ::play-samples)

;;;;;;; request handlers
(defn list-sounds [] ; TODO list unbound sounds too 
  @scene-button-id)

(defn list-samples []
  (->> @sample-map
    vals
    (sort-by :id)
    (map #(select-keys % [:id :name :duration :size]))))

(defn play-sound [scene button]
  (let [midi-key (get-in @scene-button-id [scene button :midi-key]) ]
    (stop)
    (play-sample midi-key) 
    ""))

(defn add-file [body filename]
  (let [path (str "sounds/uploaded/" filename)
        _ (with-open [s (output-stream path)]
            (copy body s))
        sample (load-sample path)] 
    (swap! sample-map
           assoc
           (:id sample) sample)
    (select-keys sample [:id])))

;; TODO persistence 
(defn bind-sample [scene button sample-id]
  (let [midi-key (get-in @scene-button-id
                         [scene button :midi-key])
        name (get-in @sample-map [sample-id :name])]
    (swap! scene-button-id
           update-in
           [scene button]
           (fn [map sample-id name]
             (assoc map
                    :id sample-id
                    :name name))
           sample-id
           name)
    (buffer-set! index-buffer midi-key sample-id) ;; TODO concurrency?
    ""))

; (sample-player (nth samples 0)) ; upload and play
