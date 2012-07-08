(ns hackbrett.mapping
  (:use hackbrett.util)
  (:use clojure.pprint)
  (:require [somnium.congomongo :as mongo]) ;; TODO consider moving to mongo ns
  (:use [clojure.tools.logging :only [error]])
  (:use [clojure.java.io :only [copy output-stream delete-file]])
  (:import com.eaio.uuid.UUID)
  )

(defn get-samples []
  (mongo/fetch :samples))

(defn get-sample-filename [note]
  (let [id-filename (:id-filename
                      (mongo/fetch-one :bindings
                                       :where {:midi-key note}))
        sample (mongo/fetch-one :samples
                                :where {:id-filename id-filename})]
    (:real-filename sample)))

(defn add-file [body filename]
  (let [path (str "sounds/uploaded/" (UUID.) ".wav")
        _ (with-open [s (output-stream path)]
            (copy body s))
        {old-path :real-filename} (mongo/fetch-and-modify
                                    :samples
                                    {:id-filename filename}
                                    {:id-filename filename, :real-filename path})]
    (when (not= old-path path)
      (delete-file old-path true))
    path))

(defn bind-sample [midi-key sample-name]
  ;; TODO check if there is a sample with that id..
  (if (zero? (mongo/fetch-count
                :samples
                :where {:id-filename sample-name}))
    (str "no sample with name " sample-name)
    (mongo/fetch-and-modify :bindings
                            {:midi-key midi-key}
                            {:id-filename sample-name :midi-key midi-key} :upsert? true)))
