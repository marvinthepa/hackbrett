(ns hackbrett.mapping
  (:use hackbrett.util)
  (:use clojure.pprint)
  (:require [somnium.congomongo :as mongo]) ;; TODO consider moving to mongo ns
  (:use [clojure.tools.logging :only [error]])
 ; (:use [clojure.java.io :only [copy output-stream]])
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
