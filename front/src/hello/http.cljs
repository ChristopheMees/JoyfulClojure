(ns hello.http
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [hello.state :as s]))

(def mm {:get http/get
         :post http/post})

(defn add-auth [state headers]
  (if-let [identity (:identity state)]
    (assoc headers "Authorization" (str "Bearer " identity))
    headers))

(defn http!
  ([update-fn method url] (http! update-fn method url {}))
  ([update-fn method url params]
   (go (let [mf (mm method)
             response (<! (mf (str "http://localhost:3000" url)
                              (merge {:with-credentials? false
                                      :headers (add-auth 
                                                @s/state 
                                                {"Accept" 
                                                 "application/edn"})}
                                     params)))]
         (update-fn response)))))
