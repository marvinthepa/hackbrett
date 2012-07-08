(ns hackbrett.core
  (:use compojure.core
        [ring.middleware.multipart-params :only [wrap-multipart-params]]
        [ring.middleware.params :only [wrap-params]]
        [ring.util.response :only [redirect]]
       ; [ring.middleware.reload :only [wrap-reload]]
        )
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [hackbrett.sound :as sound]
            [hackbrett.mongo :as mongo]
            [hackbrett.mapping :as mapping]
            [cheshire.core :as json]))

(defn toi [x]
  (Integer/parseInt x)) ;; TODO error handling

(defroutes main-routes
  (POST ["/midi-key/:midi-key" :midi-key #"[0-9]+"]
        [midi-key]
         (sound/play-note (toi midi-key))
        "")

  (POST ["/midi-key/:midi-key/sample/:sample-name"
         :midi-key #"[0-9]+"
         :sample-name #"[^/]+"]
        [midi-key sample-name]
        (mapping/bind-sample (toi midi-key) sample-name))

;  (GET "/" [] (redirect "/pad"))
;  (GET "/pad" []
;       (json/generate-string (sound/list-bindings)))
;  (GET ["/pad/scene/:scene"
;        :scene #"[0-9+]"]
;       [scene]
;       (json/generate-string (sound/list-bindings (toi scene))))
;  (GET ["/pad/scene/:scene/button/:button"
;        :scene #"[0-9]+"
;        :button #"[0-9]+"]
;       [scene button]
;       (json/generate-string (sound/list-bindings (toi scene)
;                                                  (toi button))))
;
;  (GET "/sample" []
;       (json/generate-string (sound/list-samples)))
;
;  (POST ["/pad/scene/:scene/button/:button"
;         :scene #"[0-9]+"
;         :button #"[0-9]+"]
;        [scene button]
;        (sound/play-sound (toi scene)
;                          (toi button)))
;
;  (POST ["/pad/scene/:scene/button/:button/sample/:sampleid"
;         :scene #"[0-9]+"
;         :button #"[0-9]+"
;         :sampleid #"[0-9]+"]
;        [scene button sampleid]
;        (sound/bind-sample (toi scene)
;                           (toi button)
;                           (toi sampleid)))
;
  ; TODO check if this is a wav file, error or conversion (xuggle.com or mplayer)
  (wrap-multipart-params ;; TODO needed? get the filename from the multipart-params instead?
     (PUT ["/sample/:filename" :filename #"[^/]+"] ;; TODO GET on same url..
          {{filename :filename play :play} :params
           body :body
           :as request}
          (let [real-filename (mapping/add-file body filename)
                ;; TODO free-sample if the sample replaced another of the same name
                _ (sound/load-sample real-filename)]
            (if play
              (sound/play-file real-filename))
            (json/generate-string "OK")))) ;; TODO stupid, use locator for sample info instead

  (route/resources "/") ;; TODO
  (route/not-found "404 - Oh noes, there's nothing here!") ;; TODO something cooler
  )

;; TODO look at foreclojure.ring/wrap-json

(def app
  (->
    (handler/site main-routes)
    wrap-params
    ))

(defn init []
  (mongo/init)
  (sound/init))
