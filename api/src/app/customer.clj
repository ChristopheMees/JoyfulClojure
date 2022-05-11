(ns app.customer
  (:require [clojure.alpha.spec :as s]
            [domain.state :as state]
            [db :refer [put transact!]]))

(defn new-customer!
  [conn customer]
  {:pre [(s/valid? (s/select ::state/customer [*]) customer)]}
  (transact! conn
             (put (assoc (select-keys customer [:customer/number
                                                :customer/name
                                                :customer/contact])
                         :xt/id (:customer/number customer)))))

(comment
  (def customer {:type :customer
                 :customer/number "0412345678"
                 :customer/name "Cronos"
                 :customer/contact {:person/first-name "Christophe"
                                    :person/last-name "Mees"
                                    :person/email "christophe.mees@slingshot.company"}})

  (new-customer! (system/system :db/xtdb) customer)
  )
