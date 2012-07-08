(ns hackbrett.mapping
  (:use clojure.pprint)
  (:require [somnium.congomongo :as mongo] ) ;; TODO consider moving to mongo ns
  (:use [clojure.tools.logging :only [error]])
 ; (:use [clojure.java.io :only [copy output-stream]])
  )

(defn get-samples [])

(defn get-sample-filename [note])
