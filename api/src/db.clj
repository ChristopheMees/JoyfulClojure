(ns db
  (:require [xtdb.api :as xt]))

(defn pull [db fields eid]
  (if (coll? eid)
    (xt/pull-many db fields eid)
    (xt/pull db fields eid)))

(defn where [[k v]]
  (if v
    {:where [['?e k '?v]] :in ['?v]}
    {:where [['?e k '_]]}))

(comment
  (where [:user/name "admin"])
  (where [:user/name]))

(defn qe 
  "Query Entity"
  [db {:keys [identity query]}]
  (let [[_ idv] identity
        eid (->> (xt/q db (merge {:find '[?e]} (where identity)) idv)
                 (map first)
                 (#(if (= (count %) 1) (first %) %)))]
    (pull db query eid)))

(comment
  (def xtdb-node (system/system :db/xtdb))
  (defn db [] (xt/db xtdb-node))

  (qe (db) {:identity [:user/name "admin"]
            :query [:user/name :user/role]})

  (qe (db) {:identity [:user/name]
            :query [:user/name :user/role]})
  
  (qe (db) {:identity [:customer/number]
              :query [:customer/number :customer/name]})
  )

(defn tuser [conn {:user/keys [name password role]}]
  (xt/submit-tx
   conn
   [[::xt/put
     {:xt/id name
      :user/name name
      :user/password password
      :user/role role}]]))

(defn put [document]
  [::xt/put document])

(defn transact! [conn operation]
  (xt/submit-tx conn [operation]))

(comment
  (transact! xtdb-node [::xt/put
                        {:xt/id "0412345601"
                         :customer/number "0412345601"
                         :customer/name "Cronos"
                         :customer/contact {:person/first-name "Christophe"
                                            :person/last-name "Mees"
                                            :person/email "christophe.mees@slingshot.company"}}]))
