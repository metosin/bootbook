(ns bootbook.session
  (:require [schema.core :as s]
            [plumbing.core :as p :refer [defnk]]
            [ring.middleware.session :as ring-session]
            [ring.middleware.session.memory :refer [memory-store]]
            [ring.util.http-response :refer [ok] :as resp]
            [bootbook.users :as u]))

(def cookie-name "bootbook-session")
(defonce sessions (atom {}))
(defonce session-store (memory-store sessions))

(defn- get-apikey [authorization]
  (if authorization
    (if-let [[_ k v] (re-find #"(\w+)\s*[ :=]\s*(\w+)" authorization)]
      (if (= k "apikey") v))))

(defn- get-apikey-user [request]
  (some->> (get-in request [:headers "authorization"])
           (get-apikey)
           (u/find-user-with-apikey (get-in request [:system/ctx :db]))))

(defn wrap-user [handler]
  (fn [request]
    (handler (assoc request :user (or (get-in request [:session :user])
                                      (get-apikey-user request))))))

(defn wrap-session-middlewares [handler]
  (-> handler
      (wrap-user)
      (ring-session/wrap-session {:cookie-name cookie-name
                                  :store       session-store})))

(defn wrap-session-required [handler]
  (fn [request]
    (if (:user request)
      (handler request)
      (resp/forbidden {}))))

;;
;; REST:
;;

(defnk status [{user nil}]
  (if user
    (ok {:user user})
    (resp/unauthorized {})))

(defnk login [[:system/ctx db] [:params email password]]
  (if-let [user (u/find-user-with-password db email password)]
    (-> (ok {:user user})
        (assoc-in [:session :user] user))
    (resp/forbidden {})))

(defnk logout []
  (-> (resp/temporary-redirect "/")
      (assoc :session nil)))

;;
;; Routes:
;;

(def routes {:get status
             "/login" {:post login
                       :get  logout}})
