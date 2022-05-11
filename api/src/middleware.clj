(ns middleware)

(defn wrap-cors [handler]
  (fn [req] (if (= (req :request-method) :options)
              {:status 200 :headers {"Access-Control-Allow-Origin" "*"
                                     "Access-Control-Allow-Headers" "*"}}
              (update (handler req) :headers (fn [h] (assoc h "Access-Control-Allow-Origin" "*"))))))
