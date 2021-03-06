(ns bootbook.components.handler
  (:require [com.stuartsierra.component :as component]
            [bootbook.components.context :as context]))

(defrecord Handler [sym context handler]
  component/Lifecycle

  (start [this]
    (require (symbol (namespace sym)) :reload)
    (assoc this :handler (context/wrap-context (resolve sym) context)))

  (stop [this]
    this))

(defn create [sym]
  (map->Handler {:sym sym}))
