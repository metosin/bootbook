(ns bootbook.handler
  (:require [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.util.http-response :as resp]
            [bootbook.session :refer [wrap-session-middlewares]]
            [bootbook.routes :as routes]))

(defn wrap-handle-defnk-exceptions [handler]
  (fn [request]
    (try
      (handler request)
      (catch clojure.lang.ExceptionInfo e
        (if (-> e .data :error (= :missing-key))
          (resp/bad-request (.data e))
          (throw e))))))

(defn wrap-middlewares [routes]
  (-> routes
      (wrap-session-middlewares)
      (wrap-handle-defnk-exceptions)
      (wrap-defaults {:params    {:urlencoded true
                                  :multipart  true
                                  :nested     true
                                  :keywordize true}
                      :cookies   true
                      :session   {:cookie-attrs {:http-only true}}
                      :security  {:xss-protection {:enable? true, :mode :block}
                                  :frame-options  :sameorigin
                                  :content-type-options :nosniff}
                      :static    {:resources "static"}
                      :responses {:not-modified-responses true
                                  :absolute-redirects     true
                                  :content-types          true}})
      (wrap-restful-format :formats [:edn :json-kw])
      (wrap-webjars)))

(def handler (wrap-middlewares #'routes/handler))
