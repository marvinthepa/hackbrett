(defproject hackbrett "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [overtone "0.7.0"]
                 [compojure "0.6.5"]
                 [cheshire "4.0.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [log4j "1.2.15" :exclusions  [javax.mail/mail
                                               javax.jms/jms
                                               com.sun.jdmk/jmxtools
                                               com.sun.jmx/jmxri]]
                 [congomongo "0.1.9"]
                 [com.eaio.uuid/uuid "3.3"]
                 ]
  :repositories {"eaio.com" "http://eaio.com/maven2"}
  :dev-dependencies [[org.clojars.scott/lein-nailgun "1.1.0"]
                     [vimclojure/server "2.3.1"]
                     [ring-mock "0.1.2"]
                     [ring-serve "0.1.2"]
                     [midje "1.4.0"]
                     [com.stuartsierra/lazytest "1.2.2"]]
  :plugins [[lein-ring "0.7.1"]]
  :ring {:handler hackbrett.core/app
         :init hackbrett.core/init})

