(ns app
  (:require [reitit.ring :as r]
            [reitit.ring.middleware.muuntaja :refer [format-middleware]]
            [reitit.ring.middleware.parameters :refer [parameters-middleware]]
            [muuntaja.core :as m]
            [db :refer [users]]))

(defn handshake [_]
  {:status 200 :headers {"Content-Type" "text/plain"} :body "Hello World"})

(def app (atom nil))



(reset! app
        (r/ring-handler
         (r/router
          [["/" {:handler handshake}]
           ["/api"
            ["/user/:id" {:parameters {:path {:id int?}}
                          :get (fn [req] (let [id (get-in req [:path-params :id])] {:status 200 :body (@users id)}))
                          :post (fn [req] {:status 200 :body (let [id (get-in req [:path-params :id])
                                                                   {:user/keys [first-name last-name]} (req :body-params)]
                                                               (do (swap! users (fn [users] (assoc users id {:user/first-name first-name
                                                                                                             :user/last-name last-name})))
                                                                   {:user/id id}))})}]]]
          {:data {:muuntaja m/instance
                  :middleware [format-middleware
                               parameters-middleware]}})))

(comment
  (@app {:request-method :get
         :uri "/api/user/1"
         :headers {:accept "application/edn"}}))


