(ns hackbrett.core
  (:use compojure.core
        [ring.util.response :only [redirect]]
        [clojure.tools.logging :only [error]]
        )
  (:require :reload
            [compojure.route :as route]
            [compojure.handler :as handler]
            [hackbrett.sound :as sound]
            [hackbrett.mongo :as db]
            [hackbrett.mapping :as mapping]
            [cheshire.core :as cheshire]))

(def json cheshire/generate-string)

(defn toi [x]
  (Integer/parseInt x)) ;; TODO error handling

(defroutes main-routes
  (GET ["/midi-key/:midi-key" :midi-key #"[0-9]+"]
       [midi-key]
       (json (mapping/get-binding (toi midi-key))))

  (POST ["/midi-key/:midi-key" :midi-key #"[0-9]+"]
        [midi-key]
        (sound/play-note (toi midi-key))
        "")

  (GET "/midi-key" []
       (json (mapping/get-bindings)))


  (POST ["/midi-key/:midi-key/sample/:sample-name"
         :midi-key #"[0-9]+"
         :sample-name #"[^/]+"]
        [midi-key sample-name]
        (mapping/bind-sample (toi midi-key) sample-name)
        "")

  (GET "/" [] (redirect "/pad"))
  (GET "/pad" []
       (json (mapping/get-scenes))
       )
  (GET ["/pad/scene/:scene"
        :scene #"[0-9+]"]
       [scene]
       (json (mapping/get-scene (toi scene))))
  (GET ["/pad/scene/:scene/button/:button"
        :scene #"[0-9]+"
        :button #"[0-9]+"]
       [scene button]
       (json (mapping/get-button (toi scene) (toi button))))

  (GET "/sample" []
       (json (mapping/get-samples)))


;  (POST ["/pad/scene/:scene/button/:button"
;         :scene #"[0-9]+"
;         :button #"[0-9]+"]
;        [scene button]
;        (mapping/play-sound (toi scene)
;                          (toi button)))
;
;  (POST ["/pad/scene/:scene/button/:button/sample/:sampleid"
;         :scene #"[0-9]+"
;         :button #"[0-9]+"
;         :sampleid #"[0-9]+"]
;        [scene button sampleid]
;        (mapping/bind-sample (toi scene)
;                           (toi button)
;                           (toi sampleid)))

  ; TODO check if this is a wav file, error or conversion (xuggle.com or mplayer)
  (PUT ["/sample/:filename" :filename #"[^/]+"] ;; TODO GET on same url..
       {{filename :filename play :play} :params
        body :body
        :as request}
       (let [real-filename (mapping/add-file body filename)
             ;; TODO free-sample if the sample replaced another of the same name
             _ (sound/load-sample real-filename)]
         (if play
           (sound/play-file real-filename))
         "OK")) ;; TODO stupid, use locator for sample info instead

  (route/resources "/") ;; TODO
  (route/not-found "404 - Oh noes, there's nothing here!") ;; TODO something cooler
  )

(def app
  (-> main-routes
    handler/api
    ))

(defn init []
  (db/init)
  (sound/init))
