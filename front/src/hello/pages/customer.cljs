(ns hello.pages.customer
  (:require [hello.html :refer [->table 
                                ->smart-form 
                                close-form]]
            [hello.http :refer [http!]]
            [hello.state :as s]))

(defn load-customers []
  (http! #(swap! s/state assoc :customers (:body %))
         :post
         "/api/query"
         {:edn-params {:identity [:customer/number]
                       :query [:customer/number 
                               :customer/name
                               :customer/contact]}}))

(defn format-new-customer-command 
  [{:customer/keys [number name]
    :person/keys [first-name last-name email]}]
  {:type :new-customer
   :customer/number number
   :customer/name name
   :customer/contact {:person/first-name first-name
                      :person/last-name last-name
                      :person/email email}})

(defn submit-customer-form [state]
  (http! #(when (= 200 (:status %))
            (load-customers)
            (close-form :new-customer-form))
         :post
         "/api/command"
         {:edn-params (format-new-customer-command
                       (state :new-customer-form))}))

(defn customer-table [state]
  [->table {:headers [:customer/number
                      :customer/name
                      :person/first-name
                      :person/last-name
                      :person/email]
            :rows (sort-by :customer/number
                           (map #(merge % (:customer/contact %))
                                (state :customers)))}])

(defn new-customer-form [state]
  [->smart-form state {:id :new-customer-form
                       :btn {:content "Save"
                             :on-click #(submit-customer-form state)}
                       :inputs [{:key :customer/number
                                 :label "Company Number"
                                 :type "text"}
                                {:key :customer/name
                                 :label "Company name"
                                 :type "text"}
                                {:key :person/first-name
                                 :label "Contact first name"
                                 :type "text"}
                                {:key :person/last-name
                                 :label "Contact last name"
                                 :type "text"}
                                {:key :person/email
                                 :label "Contact email"
                                 :type "email"}]
                       :toggle {:btn-text "New customer"}}])

(defn customer-page [_state]
  (load-customers)
  (fn [state] 
    [:<>
     [:h1 "Customers"]
     [customer-table state]
     [new-customer-form state]]))
