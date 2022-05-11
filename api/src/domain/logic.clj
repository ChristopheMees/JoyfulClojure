(ns domain.logic
  (:require [clojure.alpha.spec :as s]
            [domain.state :as st]))

(defn contract-active? [month]
  (let[mm (.getTime month)]
   (fn [{:contract/keys [start end]}] (and (< (.getTime start) mm) (> (.getTime end) mm)))))

(defn active-contracts-monthly-price [month]
  (comp (filter #(= :monthly (:contract/frequency %)))
        (filter (contract-active? month))
        (map :contract/price)))

(defn sum-contracts-for-month
  [contracts month]
  (transduce (active-contracts-monthly-price month) + contracts))

(defn customers-amount-to-pay-for-month
  [input month]
  {:pre [(->> input 
              vals 
              flatten 
              (map #(s/valid? (s/select ::st/contract [:contract/start :contract/end :contract/frequency :contract/price]) %))
              (reduce #(and %1 %2)))]}
  (reduce (fn [a [k v]] (assoc a k (sum-contracts-for-month v month))) {} input))

(comment
  (def cronos {:customer/name "Cronos"})
  (def contracts [{:contract/customer {:customer/name "Cronos"}
                   :contract/start #inst "2021-01-01"
                   :contract/end #inst "2022-01-01"
                   :contract/frequency :monthly
                   :contract/price 120}
                  {:contract/customer {:customer/name "Cronos"}
                   :contract/start #inst "2021-01-01"
                   :contract/end #inst "2022-01-01"
                   :contract/frequency :monthly
                   :contract/price 210}
                  {:contract/customer {:customer/name "Cronos"}
                   :contract/start #inst "2021-01-01"
                   :contract/end #inst "2022-01-01"
                   :contract/frequency :yearly
                   :contract/price 3500}])

  (def cheops {:customer/name "Cheops"})
  (def contracts2 [{:contract/customer cheops
                    :contract/start #inst "2021-01-01"
                    :contract/end #inst "2022-01-01"
                    :contract/frequency :monthly
                    :contract/price 223.6}
                   {:contract/customer cheops
                    :contract/start #inst "2021-01-01"
                    :contract/end #inst "2022-01-01"
                    :contract/frequency :monthly
                    :contract/price 330.5}
                   {:contract/customer cheops
                    :contract/start #inst "2020-01-01"
                    :contract/end #inst "2021-01-01"
                    :contract/frequency :monthly
                    :contract/price 240}])

  (sum-contracts-for-month contracts #inst "2021-05")
  (customers-amount-to-pay-for-month {cronos contracts
                                      cheops contracts2}
                                     #inst "2021-05")
  )
