(ns hello.pages.contract
  (:require [hello.html :refer [->table
                                ->smart-form
                                close-form]]
            [hello.http :refer [http!]]
            [hello.state :as s]))

(defn load-contracts []
  (http! #(swap! s/state assoc :contracts (:body %))
         :post
         "/api/query"
         {:edn-params {:identity [:contract/number]
                       :query [:contract/number
                               :contract/start
                               :contract/end
                               :contract/customer]}}))

(defn format-new-contract-command 
  [{:contract/keys [number customer start end]
    :car/keys [built license-plate]
    :person/keys [first-name last-name email]}]
  {:type :new-contract
   :contract/number number
   :contract/start start
   :contract/end end
   :contract/customer customer
   :contract/car {:car/license-plate license-plate
                  :car/built built
                  :car/driver {:person/first-name first-name
                               :person/last-name last-name
                               :person/email email}}})

(defn submit-contract-form [state]
  (http! #(when (= 200 (:status %))
            (load-contracts)
            (close-form :new-contract-form))
         :post
         "/api/command"
         {:edn-params (format-new-contract-command
                       (state :new-contract-form))}))

(defn contract-table [state]
  [->table {:headers [:contract/number
                      :contract/customer
                      :contract/start
                      :contract/end]
            :rows (sort-by :contract/number
                           (state :contracts))}])

(comment
  (swap! s/state assoc :contracts [{:contract/number "1000"
                                    :contract/customer "Cronos"
                                    :contract/start #inst "2022-01-01"
                                    :contract/end #inst "2023-01-01"}]))

(defn new-contract-form [state]
  [->smart-form state {:id :new-contract-form
                       :btn {:content "Save"
                             :on-click #(submit-contract-form state)}
                       :inputs [{:key :contract/number
                                 :label "Number"
                                 :type "text"}
                                {:key :contract/customer
                                 :label "Customer"
                                 :type "text"}
                                {:key :contract/start
                                 :label "Start date"
                                 :type "date"}
                                {:key :contract/end
                                 :label "End date"
                                 :type "date"}
                                {:key :contract/frequency
                                 :label "Billable interval"
                                 :type "select"
                                 :default-option "Choose an interval"
                                 :options [{:label "Monthly" :value "monthly"}
                                           {:label "Yearly" :value "yearly"}]}
                                {:key :contract/price
                                 :label "Cost per interval"
                                 :type "text"}
                                {:key :car/license-plate
                                 :label "Car license plate"
                                 :type "text"}
                                {:key :car/built
                                 :label "Car built date"
                                 :type "date"}
                                {:key :person/first-name
                                 :label "Driver first name"
                                 :type "text"}
                                {:key :person/last-name
                                 :label "Driver last name"
                                 :type "text"}
                                {:key :person/email
                                 :label "Driver email"
                                 :type "text"}]
                       :toggle {:btn-text "New customer"}}])

(defn contract-page [_state]
  #_(load-contracts)
  (fn [state] 
    [:<>
     [:h1 "Contracts"]
     [contract-table state]
     [new-contract-form state]]))
