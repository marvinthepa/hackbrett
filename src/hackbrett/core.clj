(ns hackbrett.core
  (:use compojure.core)
  (:use ring.middleware.multipart-params)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [hackbrett.sound :as sound]
            [cheshire.core :as json]))

(defn toi [x]
  (Integer/parseInt x)) ;; TODO error handling

(defroutes main-routes
  (wrap-multipart-params
    (PUT ["/addsample/:filename" :filename #"[^/]+"]
         {{filename :filename} :params
          body :body
          :as request}
         (json/generate-string (sound/add-file body filename))))
  (POST ["/bind/scene/:scene/button/:button"
         :scene #"[0-9]+"
         :button #"[0-9]+"]
        [scene button sampleid]
        (sound/bind-sample (toi scene)
                           (toi button)
                           (toi sampleid)))
  (POST "/play/scene/:scene/button/:button" [scene button]
        (sound/play-sound (toi scene)
                          (toi button)))
  (GET "/binding/list" []
       (json/generate-string (sound/list-sounds)))
  (GET "/sample/list" []
       (json/generate-string (sound/list-samples)))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (->
    (handler/site main-routes) 
    ))
