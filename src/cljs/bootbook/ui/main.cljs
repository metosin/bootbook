(ns bootbook.ui.main
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]))

(defonce main-data (atom {:clicks 0}))

(defn main-view []
  [:h1 "Hello"])

(defn init! []
  (js/console.log "Here we go!")
  (reagent/render [main-view] (js/document.getElementById "app")))

(init!)
