(ns bootbook.routes
  (:require [bidi.bidi]
            [bidi.ring]
            [bootbook.index :refer [index-response]]
            [bootbook.session :as session]))

(def routes ["" {"/"    {:get (constantly index-response)}
                 "/api" {"/session"  session/routes}}])

(def handler (-> routes
                 bidi.bidi/compile-route
                 bidi.ring/make-handler))
