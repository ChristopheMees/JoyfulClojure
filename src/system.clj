(ns system
  (:require [app :refer [app]]
            [ring.adapter.jetty :refer [run-jetty]]))

(defmulti init-key (fn [key value] key))
(defmulti halt-key! (fn [key value] key))

(defn init [cfg]
  (reduce (fn [map [k v]] (assoc map k (init-key k v))) {} cfg))

(defn halt! [system]
  (doseq [k (keys system)]
    (halt-key! k (system k))))

(def config
  {:adapter/jetty {:port 3000 :handler app}})

(defmethod init-key :adapter/jetty [_ {:keys [port handler]}]
  (run-jetty (fn [req] (@handler req)) {:port port :join? false}))

(defmethod halt-key! :adapter/jetty [_ server]
  (.stop server))

(def system
  (init config))

(comment
  (halt! system))
