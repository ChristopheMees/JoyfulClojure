(ns domain.state
  (:require [clojure.alpha.spec :as spec]
            [clojure.alpha.spec.gen :as gen]))

(spec/def :user/name (spec/and string? #(< (count %) 21)))
(spec/def :user/password (spec/and string? #(> (count %) 4)))
(spec/def :user/role #{:admin :read :write})
(spec/def :user/active boolean?)

(spec/def ::user (spec/schema [:user/name :user/password :user/role :user/active]))

(comment
  (spec/valid? (spec/select ::user [:user/name :user/password :user/role :user/active])
               {:user/name "admin"
                :user/password "admin"
                :user/role :admin
                :user/active true})

  (gen/sample (spec/gen (spec/select ::user [*])) 5))

(spec/def :person/first-name string?)
(spec/def :person/last-name string?)

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(spec/def :person/email (spec/and string? #(re-matches email-regex %)))

(spec/def ::person (spec/schema [:person/first-name :person/last-name :person/email]))

(comment
  (spec/valid? (spec/select ::person [*])
               {:person/first-name "Christophe"
                :person/last-name "Mees"
                :person/email "christophe.mees@slingshot.be"}))

(spec/def :customer/number string?)
(spec/def :customer/name string?)
(spec/def :customer/contact (spec/union ::person))

(spec/def ::customer (spec/schema [:customer/number :customer/name :customer/contact]))

(comment
  (spec/valid? (spec/select ::customer [* {:customer/contact [:person/email]}])
               {:customer/number "0443807959"
                :customer/name "Cronos"
                :customer/contact {:person/email "christophe.mees@slingshot.be"}}))

(spec/def :car/license-plate string?)
(spec/def :car/built inst?)
(spec/def :car/driver (spec/union ::person))

(spec/def ::car (spec/schema [:car/license-plate :car/built :car/driver]))

(comment
  (spec/valid? (spec/select ::car [* {:car/driver [:person/email]}])
               {:car/license-plate "1-DKA039"
                :car/built #inst "2020-03-12"
                :car/driver {:person/email "christophe.mees@slingshot.be"}}))

(spec/def :contract/start inst?)
(spec/def :contract/end inst?)
(spec/def :contract/frequency #{:monthly :yearly})
(spec/def :contract/price number?)
(spec/def :contract/customer (spec/union ::customer))
(spec/def :contract/car (spec/union ::car))

(spec/def ::contract (spec/schema [:contract/start
                                   :contract/end
                                   :contract/frequency
                                   :contract/price
                                   :contract/customer
                                   :contract/car]))

 (comment
   (spec/valid? (spec/select ::contract [* {:contract/customer [:customer/name]
                                            :contract/car [:car/driver {:car/driver [:person/email]}]}])
                {:contract/start #inst "2021-01"
                 :contract/end #inst "2022-01"
                 :contract/frequency :monthly
                 :contract/price 120.23
                 :contract/customer {:customer/name "Cronos"}
                 :contract/car {:car/driver {:person/email "christophe.mees@slingshot.be"}}})
   )
