(ns system
  (:require [clojure.java.io :refer [file]]
            [ring.adapter.jetty :refer [run-jetty]]
            [xtdb.api :refer [db start-node]]
            [app :refer [handler]]))

(def system)

(defmulti init-key (fn [key _value] key))
(defmulti halt-key! (fn [key _value] key))

(defn init [cfg]
  (reduce (fn [map [k v]] (assoc map k (init-key k v))) {} cfg))

(defn halt! [system]
  (doseq [k (keys system)]
    (halt-key! k (system k))))

(def config
  {:adapter/jetty {:port 3000 :handler handler}
   :db/xtdb {:module 'xtdb.rocksdb/->kv-store
             :sync? true
             :tx-log "data/dev/tx-log"
             :document-store "data/dev/doc-store"
             :index-store "data/dev/index-store"}})

(defmethod init-key :db/xtdb 
  [_ {:keys [module sync? tx-log document-store index-store]}]
  (letfn [(kv-store [dir]
            {:kv-store {:xtdb/module module
                        :db-dir (file dir)
                        :sync? sync?}})]
    (start-node
     {:xtdb/tx-log (kv-store tx-log)
      :xtdb/document-store (kv-store document-store)
      :xtdb/index-store (kv-store index-store)})))

(defmethod halt-key! :db/xtdb [_ node]
  (.close node))

(defmethod init-key :adapter/jetty [_ {:keys [port handler]}]
  (run-jetty (fn [req] (let [dbconn (system :db/xtdb)
                             dbfn (fn [] (db dbconn))]
                         (@handler (merge req {:dbconn dbconn :dbfn dbfn}))))
             {:port port :join? false}))

(defmethod halt-key! :adapter/jetty [_ server]
  (.stop server))

(def system
  (init config))

(comment
  (halt! system))
