(ns hackbrett.mapping
  (:use clojure.pprint
        [clojure.tools.logging :only [error]]
        [clojure.java.io :only [copy output-stream delete-file as-file]]
        [somnium.congomongo :as mongo]
        [hackbrett.util :as util]
        )
  (:import com.eaio.uuid.UUID))

;; TODO split  pad-related and midi-related stuff in their own namespaces
;;
(def uploaded-path "sounds/uploaded/")

(defn fetch-samples []
  (mongo/fetch :samples))

(defn get-sample-filename [note]
  (let [id-filename (:id-filename
                      (mongo/fetch-one :bindings
                                       :where {:midi-key note}))
        sample (mongo/fetch-one :samples
                                :where {:id-filename id-filename})]
    (:real-filename sample)))

(defn find-first [coll key val]
  (first (filter #(= (% key) val) coll)))

(defn clean-binding [binding]
  (select-keys binding [:id-filename :midi-key]))

(defn clean-button [button]
  (select-keys button [:midi-key :scene :button]))

(defn add-sample [bindings button]
  (assoc button :sample
         (:id-filename
           (bindings (:midi-key button)))))

(defn get-bindings []
  (map clean-binding
    (mongo/fetch :bindings)))

(defn get-scenes-where [where]
  (let [bindings (util/group-by-single :midi-key
                   (get-bindings))]
    (->>
      (mongo/fetch :buttons :where where)
      (map clean-button)
      (map (partial add-sample bindings)))))

(defn get-binding [midi-key]
  (clean-binding (mongo/fetch-one :bindings :where {:midi-key midi-key})))

(defn get-samples []
  (map :id-filename
    (mongo/fetch :samples)))

;; TODO could be improved using destructuring and map keys
(defn get-scenes []
  (get-scenes-where {:controller :nano-pad}))
(defn get-scene [scene]
  (get-scenes-where {:controller :nano-pad
                     :scene scene}))
(defn get-button [scene button]
  (when-let [scenes (get-scenes-where {:controller :nano-pad
                                       :scene scene
                                       :button button})]
    (first scenes)))

(defn add-file [body filename]
  (let [path (str uploaded-path (UUID.) ".wav")
        _ (with-open [s (output-stream path)]
            (copy body s))
        {old-path :real-filename} (mongo/fetch-and-modify
                                    :samples
                                    {:id-filename filename}
                                    {:id-filename filename, :real-filename path} :upsert? true)]
    (when (and old-path (not= old-path path))
      (delete-file old-path true))
    path))

(defn bind-sample [midi-key sample-name]
  (if (zero? (mongo/fetch-count
                :samples
                :where {:id-filename sample-name}))
    (str "no sample with name " sample-name)
    (mongo/fetch-and-modify :bindings
                            {:midi-key midi-key}
                            {:id-filename sample-name :midi-key midi-key} :upsert? true)))

(defn init []
  (.mkdirs
    (as-file
      uploaded-path)))
