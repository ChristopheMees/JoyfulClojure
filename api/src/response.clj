(ns response)

(defn ok
  ([] {:status 200 :body "Ok"})
  ([resp] (merge resp {:status 200})))

(defn unauthorized
  ([] {:status 401 :body "Unauthorized"})
  ([resp] (merge resp {:status 401 :body "Unauthorized"})))

(defn forbidden 
  ([] {:status 403 :body "Forbidden"})
  ([resp] (merge resp {:status 403 :body "Forbidden"})))
