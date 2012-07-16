(ns hackbrett.core
  (:use compojure.core
        [ring.util.response :only [redirect]]
        [clojure.tools.logging :only [error]])
  (:require :reload
            [compojure.route :as route]
            [compojure.handler :as handler]
            [hackbrett.sound :as sound]
            [hackbrett.mongo :as db]
            [hackbrett.mapping :as mapping]
            [cheshire.core :as cheshire]))

(def json cheshire/generate-string)

(defn toi [x]
  (Integer/parseInt x))

(defroutes main-routes
  (GET "/" [] (redirect "/help"))

  (GET ["/midi-key/:midi-key" :midi-key #"[0-9]{2,3}"]
       [midi-key]
       (json (mapping/get-binding (toi midi-key))))

  (POST ["/midi-key/:midi-key" :midi-key #"[0-9]{2,3}"]
        [midi-key]
        (sound/play-note (toi midi-key))
        "")

  (GET "/midi-key" []
       (json (mapping/get-bindings)))

  (POST ["/midi-key/:midi-key/sample/:sample-name"
         :midi-key #"[0-9]{2,3}"
         :sample-name #"[^/]+"]
        [midi-key sample-name]
        (mapping/bind-sample (toi midi-key) sample-name)
        "")

  (GET "/pad" []
       (json (mapping/get-scenes)))
  (GET ["/pad/scene/:scene"
        :scene #"[1-4]"]
       [scene]
       (json (mapping/get-scene (toi scene))))
  (GET ["/pad/scene/:scene/button/:button"
        :scene #"[1-4]"
        :button #"[0-9]+"]
       [scene button]
       (json (mapping/get-button (toi scene) (toi button))))
  (POST ["/pad/scene/:scene/button/:button"
         :scene #"[0-9]+"
         :button #"[0-9]+"]
        [scene button]
        (let [midi-key (:midi-key (mapping/get-button
                                    (toi scene)
                                    (toi button)))]
          (sound/play-note midi-key)
          ""))
  (POST ["/pad/scene/:scene/button/:button/sample/:sample-name"
         :scene #"[0-9]+"
         :button #"[0-9]+"
         :sample-name #"[^/]+"]
        [scene button sample-name]
        (let [midi-key (:midi-key (mapping/get-button
                                    (toi scene)
                                    (toi button)))]
          (mapping/bind-sample midi-key sample-name)
          ""))

  (GET "/sample" []
       (json (mapping/get-samples)))

  (GET "/help" []
       repeat
       {
        :content-type "text/plain; charset=utf-8"
        :body
        (clojure.string/replace
       "Welcome to Hackbrett.

list all samples:
   curl 'http://%hostname%/sample'
upload a new sample (only wav is supported):
   curl -T baby.wav 'http://%hostname%/sample/baby.wav'
upload a new sample and play it immediately:
   curl -T baby.wav 'http://%hostname%/sample/baby.wav?play=true'
play an existing sample:
   curl -X POST 'http://%hostname%/sample/baby.wav'

show nano-pad and bound samples:
   curl 'http://%hostname%/pad'
details:
   curl 'http://%hostname%/pad/scene/1'
   curl 'http://%hostname%/pad/scene/1/button/1'

bind a sample to a button on nano pad:
   curl -X POST 'http://%hostname%/pad/scene/1/button/1/sample/baby.wav'
play a sample by binding on nano-pad:
   curl -X POST 'http://%hostname%/pad/scene/1/button/1'

list existing samples bound to midi-keys:
   curl 'http://%hostname%/midi-key'
play an existing sample by midi-key:
   curl -X POST 'http://%hostname%/midi-key/39'
"
          "%hostname%"
          (str (.getHostName (java.net.InetAddress/getLocalHost)) ":3000") ;; TODO get portname from environment
          )})

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

  (POST ["/sample/:filename" :filename #"[^/]+"]
        [filename]
        (let [real-filename (mapping/get-real-filename filename)]
          (sound/play-file real-filename)
          ""))

  (route/resources "/") ;; TODO
  (route/not-found "404 - Oh noes, there's nothing here!") ;; TODO something cooler
  )

(def app
  (-> main-routes
    handler/api
    ))

(defn init []
  (db/init)
  (sound/init)
  (mapping/init))
