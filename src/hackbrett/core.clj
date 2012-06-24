(ns hackbrett.core
  (:use compojure.core)
  (:use ring.middleware.multipart-params)
  (:use [ring.util.response :only [redirect]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [hackbrett.sound :as sound]
            [cheshire.core :as json]))

(def initialized (atom false)) ;; TODO initialize only once

(defn toi [x]
  (Integer/parseInt x)) ;; TODO error handling

(defroutes main-routes
  (GET "/" [] (redirect "/pad"))
  (GET "/pad" []
       (json/generate-string (sound/list-sounds)))
  (GET ["/pad/scene/:scene"
        :scene #"[0-9+]"]
       [scene]
       (json/generate-string (sound/list-sounds (toi scene))))
  (GET ["/pad/scene/:scene/button/:button"
        :scene #"[0-9]+"
        :button #"[0-9]+"]
       [scene button]
       (json/generate-string (sound/list-sounds (toi scene)
                                                (toi button))))

  (GET "/sample" []
       (json/generate-string (sound/list-samples)))

  (GET "/sample/:id" [id] ;; TODO add download uri
       (json/generate-string (sound/get-sample (toi id))))

  (POST "/pad/scene/:scene/button/:button" [scene button]
        (sound/play-sound (toi scene)
                          (toi button)))

  (POST ["/pad/scene/:scene/button/:button/sample/:sampleid"
         :scene #"[0-9]+"
         :button #"[0-9]+"
         :sampleid #"[0-9]+"]
        [scene button sampleid]
        (sound/bind-sample (toi scene)
                           (toi button)
                           (toi sampleid)))

  (wrap-multipart-params ;; TODO needed?
    (PUT ["/sample/:filename" :filename #"[^/]+"]
         {{filename :filename} :params
          body :body
          :as request}
         (json/generate-string (sound/add-file body filename))))

  (route/resources "/") ;; TODO
  (route/not-found "404 - Oh noes, there's nothing here!") ;; TODO something cooler
  )

;; TODO look at foreclojure.ring/wrap-json

(def app
  (->
    (handler/site main-routes)
    ))

(defn init []
  (sound/init))
