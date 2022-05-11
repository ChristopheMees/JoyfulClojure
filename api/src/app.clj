(ns app
  (:require [clojure.pprint :refer [pprint]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [reitit.ring :refer [ring-handler router] :as r]
            [reitit.ring.middleware.muuntaja :refer [format-middleware]]
            [reitit.ring.middleware.parameters :refer [parameters-middleware]]
            [muuntaja.core :as m]
            [xtdb.api :refer [q]]
            [auth :refer [auth-backend authenticated? has-role? login parse-identity]]
            [app.customer :refer [new-customer!]]
            [db :refer [pull tuser qe]]
            [middleware :refer [wrap-cors]]
            [response :refer [ok]]))

(defn handshake [_]
  {:status 200 :headers {"Content-Type" "text/plain"} :body "Hello World"})

(defn get-user [req db]
  (let [id (get-in req [:path-params :id])]
    (ok {:body (pull db [:user/name :user/role] id)})))

(defn get-all-users [db]
  (let [eids (map first (q db '{:find [?u] :where [[?u :user/name _]]}))]
    (ok {:body (pull db [:user/name :user/role] eids)})))

(defn post-user [req]
  (ok {:body (let [name (get-in req [:path-params :id])
                   {:user/keys [password role]} (req :body-params)]
               (tuser (:dbconn req)
                      {:user/name name
                       :user/password password
                       :user/role role})
               nil)}))

(defn query [dbfn q]
  (ok {:body (qe (dbfn) q)}))

(defn command! [conn {type :type :as command}]
  (when (= type :new-customer) (new-customer! conn command))
  (ok))

(defn logging-mw [handler]
  (fn [req]
    (pprint (select-keys req [:body :body-params :request-method :headers]))
    (handler req)))

(def handler (atom nil))

(def app (ring-handler
          (router
           [["/" {:handler handshake}]
            ["/login" {:post #(login ((:dbfn %)) %)}]
            ["/api" {:middleware [#(wrap-authentication % auth-backend)
                                  parse-identity
                                  authenticated?]}
             ["/user/:id" {:middleware [(has-role? #{:admin})]
                           :parameters {:path {:id int?}}
                           :get #(get-user % ((:dbfn %)))
                           :post post-user}]
             ["/users" {:middleware [(has-role? #{:admin})]
                        :get (fn [req] (get-all-users ((:dbfn req))))}]
             ["/command" {:middleware [(has-role? #{:write})]
                          :post (fn [{:keys [body-params dbconn]}] (command! dbconn body-params))}]
             ["/query" {:middleware [(has-role? #{:read :write})]
                          :post (fn [{:keys [body-params dbfn]}] (query dbfn body-params))}]]]
           {:data {:muuntaja m/instance
                   :middleware [wrap-cors
                                format-middleware
                                parameters-middleware
                                logging-mw]}})))

(reset! handler app)

(comment
  (def dbconn (system/system :db/xtdb))
  (defn dbfn [] (xtdb.api/db (system/system :db/xtdb)))
  (def request {:request-method :post
                :uri "/login"
                :headers {"content-type" "application/edn"}
                :body (str {:user/name "admin" :user/password "admin"})
                :dbfn dbfn})
  (-> (@handler request)
      :body)

  (defn get-token [] (auth/new-jwt #:user{:name "admin" :role :write}))
  (def request {:request-method :post
                :uri "/api/command"
                :headers {"Authorization" (str "Bearer " (get-token))
                          "content-type" "application/edn"}
                :dbconn dbconn
                :body (str
                       {:type :new-customer
                        :customer/number "0412345678"
                        :customer/name "Cronos"
                        :customer/contact {:person/first-name "Christophe"
                                           :person/last-name "Mees"
                                           :person/email "christophe.mees@slingshot.company"}})})

  (-> (@handler request)
      :status)

  (def request {:request-method :post
                :uri "/api/query"
                :headers {"Authorization" (str "Bearer " (get-token))
                          "content-type" "application/edn"}
                :dbfn dbfn
                :body (str {:identity [:customer/number]
                            :query [:customer/number :customer/name]})})

  (-> (@handler request)
      :body
      slurp)

  (qe (dbfn) {:identity [:customer/number]
              :query [:customer/number :customer/name]})



  (def date1 (.parse (java.text.SimpleDateFormat. "ddMMyyyy") "08082013"))
  (def date2 (.parse (java.text.SimpleDateFormat. "ddMMyyyy") "12082013"))

  (let [m1 (.getTime date1)
        m2 (.getTime date2)]
    (-> (- m1 m2)
        java.lang.Math/abs
        (/ (* 1000 60 60 24))))

  )
