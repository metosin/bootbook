(ns bootbook.books
  (:require [clojure.string :as string]
            [ring.util.http-response :refer [ok]]))

(defn with-index [{:keys [title langs authors] :as book}]
  (assoc book :index (set (concat
                            (map string/lower-case (string/split title #"\s+"))
                            (map name langs)
                            (map name authors)))))

(def books (atom (map with-index [{:title   "The Joy of Clojure"
                                   :langs   #{:clojure}
                                   :pages   328
                                   :authors [:fogus :houser]
                                   :read?   true}
                                  {:title   "Erlang Programming"
                                   :langs   #{:erlang}
                                   :pages   470
                                   :authors [:cesarini :thompson]}
                                  {:title   "Clojure Data Analysis Cookbook"
                                   :langs   #{:clojure}
                                   :pages   326
                                   :authors [:rochester]}
                                  {:title   "Programming Concurrency on the JVM"
                                   :langs   #{:java :ruby :groovy :scala :clojure}
                                   :pages   270
                                   :authors [:venkat]
                                   :read?   true}
                                  {:title   "The Little Schemer"
                                   :langs   #{:scheme}
                                   :pages   196
                                   :authors [:friedman :felleisen :sussman]}
                                  {:title   "Types and Programming Languages"
                                   :langs   #{:haskel :java :fortran}
                                   :pages   623
                                   :authors [:abelson :sussman :jsussman]}])))

(defn ->finder [search]
  (let [pattern (re-pattern (str "^" search))
        match?  (partial re-find pattern)]
    (fn [{index :index}]
      (some match? index))))

(defn search [request]
  (ok (some-> request
              :params
              :search
              string/lower-case
              ->finder
              (filter @books))))


(defn set-when-title [target-title read?]
  (fn [{:keys [title] :as book}]
    (if (= title target-title)
      (assoc book :read? read?)
      book)))

(defn set-as-read [request]
  (let [title (-> request :params :title)
        read? (-> request :params :read? Boolean/parseBoolean)]
    (ok (swap! books (fn [books]
                       (map (set-when-title title read?) books))))))

(def routes {:get    search
             "/read" {:post set-as-read}})
