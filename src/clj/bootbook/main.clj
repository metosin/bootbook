(ns bootbook.main
  (:gen-class))

(defn -main [& args]
  (require 'bootbook.system)
  ((resolve 'bootbook.system/start-base-system) {})
  (println "Server ready"))
