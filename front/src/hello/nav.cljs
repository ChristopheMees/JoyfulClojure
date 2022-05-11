(ns hello.nav
  (:require [hello.state :refer [state]]))

(defn nav-> [page]
  (swap! state assoc :active-page page))

(defn nav []
  [:div
   [:ul {:class "nav"}
    [:li {:class "nav-link"}
     [:a {:href "#" :on-click (fn [_] (nav-> :schedule))} "Home"]]
    [:li {:class "nav-link"}
     [:a {:href "#" :on-click (fn [_] (nav-> :customers))} "Customers"]]
    [:li {:class "nav-link"}
     [:a {:href "#" :on-click (fn [_] (nav-> :contracts))} "Contracts"]]
    [:li {:class "nav-link"}
     [:a {:href "#" :on-click (fn [_] (nav-> :users))} "Users"]]]])
