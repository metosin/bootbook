(ns bootbook.ui.main
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.string :as string]
            [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [<!] :as a]
            [cljs-http.client :as http]))

(defonce books (atom nil))                                  ; FIXME!!!

(defn nav-bar []
  [:div.nav-bar
   [:ul
    [:li [:a.brand {:href "#"}
          [:i.fa.fa-book]
          " BootBook"]]]])

(defn search-books [search-value books]
  (go
    (->> {:query-params {:search search-value}}
         (http/get "/api/books")
         <!
         :body
         (reset! books))))

(defonce search-value (atom nil))                           ; FIXME

(defn search-form [books]
  (let [search-value-FIXME (atom nil)]
    (fn []
      [:form.search {:on-submit
                     (fn [e]
                       (.preventDefault e)
                       (search-books @search-value books))}
       [:input {:type        "text"
                :value       @search-value
                :on-change   (fn [e]
                               (reset! search-value (-> e .-target .-value)))
                :autofocus   true
                :placeholder "Search..."}]])))

(defn set-as-read [title read?]
  (js/console.log "change:" title read?)
  (go
    (->> {:form-params {:title title, :read? read?}}
         (http/post "/api/books/read")
         <!
         :body
         (reset! books))))

(defn book-view [{:keys [title langs authors read?]}]
  [:div.book {:key title}
   [:div
    [:span.title title]
    [:span.langs (for [lang langs]
                   [:span {:key lang} (-> lang name string/capitalize)])]]
   [:div.authos (string/join ", " (map (comp string/capitalize name) authors))]
   [:label
    [:input {:type      "checkbox"
             :checked   read?
             :on-change (fn [e]
                          (set-as-read title (-> e .-target .-checked)))}]
    "Read?"]])

(defn book-list [books]
  [:div.books
   (if-let [book-list (seq @books)]
     (map book-view book-list))])

(defn main-view []
  (let [books-FIXME (atom nil)]
    (fn []
      [:div.application
       [nav-bar]
       [search-form books]
       [book-list books]])))

(defn init! []
  (reagent/render [main-view] (js/document.getElementById "app")))

(init!)
