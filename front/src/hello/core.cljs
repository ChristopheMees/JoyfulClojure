(ns hello.core
  (:require [reagent.dom :as rdom]
            [hello.nav :refer [nav]]
            [hello.pages.contract :refer [contract-page]]
            [hello.pages.customer :refer [customer-page]]
            [hello.pages.login :refer [login-page]]
            [hello.pages.users :refer [users-page]]
            [hello.state :refer [state]]))

(def pages {:users users-page
            :customers customer-page
            :contracts contract-page})

(defn page-content [{:keys [active-page] :as state}]
  [(pages active-page) state])

(defn app []
  (let [state @state]
    [:div
     [nav]
     (if (:identity state)
       [page-content state]
       [login-page state])]))

(rdom/render
 [app]
 (. js/document getElementById "app"))

