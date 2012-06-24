(ns hackbrett.sound
  (:use overtone.core)
  (:use [clojure.tools.logging :only [error]])
  (:use [hackbrett.util :only [update-in-wildcard]])
  (:use [clojure.java.io :only [copy output-stream]]))

(defonce pad-keys
  [39 48 45 43 51 49 36 38 40 42 44 46 ; scene 1
   60 61 62 63 64 65 66 67 68 69 70 71 ; scene 2
   72 73 74 75 76 77 78 79 80 81 82 83 ; scene 3
   84 85 86 87 88 89 90 91 92 93 94 95]) ; scene 4
(defonce buttons
  (into {}
        (map
          (fn [x midi-key]
            [midi-key (assoc x :midi-key midi-key)])
          (for [scene (range 1 5)
                button (range 1 13)]
            {:scene scene :button button})
          pad-keys)))
(defonce num-of-midi-channels 128)

;; FIXME yikes!
(defn init []
  (defonce _auto-boot_ (boot-mixer))

  ; http://www.wayneandgarth.com/sound%20bites.htm
  ; http://dailywav.com/search/results.php
  ; http://www.moviewavs.com/Movies/Office_Space.html
  (defonce samples (load-samples "sounds/*.wav"))

  (defonce sample-map
    (atom
      (into {}
            (map (fn [x] [(:id x) x]) samples))))
  (defonce ^:private silent-buffer (buffer 0))

  (let [midi-key-to-sample (remove nil?
                                    (map-indexed (fn [index y]
                                                   (let [nsamples (count samples)]
                                                     (when (< index nsamples)
                                                       [y (:id (nth samples index))])))
                                                 pad-keys))
        samples-buttons-scenes (->> midi-key-to-sample
                                 (map (fn [[k v]]
                                        (into
                                          (assoc (@sample-map v)
                                                 :key k)
                                          (buttons k)))))]
    (defonce scene-button-id
      (atom
        (reduce (fn [agg {:keys [scene button] :as sample}]
                  (assoc-in agg
                            [scene button]
                            (select-keys sample
                                         [:scene :button :name :id :midi-key])))
                {}
                samples-buttons-scenes)))
    (defonce ^:private index-buffer ;; a buffer for the instrument to index into
      (let [tab midi-key-to-sample
            buf (buffer num-of-midi-channels)]
        (buffer-fill! buf (:id silent-buffer))
        (doseq [[idx val] tab]
          (buffer-set! buf idx val))
        buf))
    (definst play-sample [note 36]
      (let [buf (index:kr (:id index-buffer) note)]
        (scaled-play-buf 2 buf :level 1 :loop 0 :action FREE :start-pos 0)))
    ;; handle the pad
    (on-event [:midi :note-on] (fn [{note :note}]
                                 (error note)
                                 (stop)
                                 (play-sample note))
              ::play-samples)
    ))

;;;;;;;; helpers
(defn make-scene-button-uri [[button {:keys [scene button] :as m}]]
  (assoc m :uri
         (str "/pad/scene/" scene "/button/" button)))

(defn make-sample-uri [id]
  (str "/sample/" id))

(defn make-sample [{:keys [id name] :as m}]
  (assoc
    (dissoc m :id :name)
    :sample
    {:id id :name name :uri (make-sample-uri id)}))

(defn clean-sample [sample]
  (select-keys sample [:id :name :duration :size]))

(defn add-sample-uri [sample]
  (assoc sample :uri
         (make-sample-uri (get sample :id))))

(def format-button (comp make-sample make-scene-button-uri))

(defn format-scene [[scene buttons]]
      {:scene scene
       :uri (str "/pad/scene/" scene)
       :buttons (map format-button buttons)})


;;;;;;; request handlers
(defn list-sounds
  ([] (map format-scene @scene-button-id))
  ([scene] (format-scene [scene (@scene-button-id scene)]))
  ([scene button]
     (format-button [button (get-in @scene-button-id [scene button])])))

(defn get-sample [id] ;; TODO add download URI
  (->>
    (@sample-map id)
    clean-sample))

(defn list-samples []
  (->> @sample-map
    vals
    (sort-by :id)
    (map clean-sample)
    (map add-sample-uri)))

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

(defn update-id-and-name [map sample-id name]
  (assoc map
         :id sample-id
         :name name))

(defn update-scene-button-id!
  [scene button sample-id name]
  (swap! scene-button-id
         update-in
         [scene button]
         update-id-and-name
         sample-id name))

;; TODO persistence
(defn bind-sample [scene button sample-id]
  (let [midi-key (get-in @scene-button-id
                         [scene button :midi-key])
        name (get-in @sample-map [sample-id :name])]
    (update-scene-button-id! scene button sample-id name)
    (buffer-set! index-buffer midi-key sample-id) ;; TODO concurrency?
    ""))

; (sample-player (nth samples 0)) ; upload and play
