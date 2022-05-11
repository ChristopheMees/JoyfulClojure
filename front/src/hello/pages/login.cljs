(ns hello.pages.login
  (:require [hello.html :refer [->form]]
            [hello.http :refer [http!]]
            [hello.state :as s]))

(defn login! [name password]
  (http! #(when (= 200 (:status %))
            (swap! s/state assoc :identity (:body %))
            (swap! s/state dissoc :login-form))
         :post
         "/login"
         {:edn-params {:user/name name :user/password password}}))

(defn login-page [state]
  (let [input (:login-form state)
        n (:user/name input)
        p (:user/password input)]
    [->form state {:id :login-form
                   :btn {:content "Login"
                         :on-click #(login! n p)}
                   :inputs [{:key :user/name
                             :label "Username"
                             :type "text"}
                            {:key :user/password
                             :label "Password"
                             :type "password"}]}]))
