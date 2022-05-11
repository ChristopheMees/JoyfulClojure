(ns hello.pages.users
  (:require [hello.html :refer [->table ->form]]
            [hello.http :refer [http!]]
            [hello.state :as s]))

(defn load-users []
  (http! #(swap! s/state assoc :users (:body %))
         :get
         "/api/users"))

(defn users-table [state]
  [:div
   [:h1 "System Users"]
   (->table {:headers [:user/name :user/role]
             :rows (sort-by :user/name
                            (state :users))})])

(defn toggle-user-form []
  (swap! s/state update-in [:new-user-form :active] not))

(defn submit-user-form [state]
  (let [user-id (cljs.core/random-uuid)]
    (http! #(when (= 200 (:status %))
              (toggle-user-form)
              (load-users)
              (swap! s/state dissoc :new-user-form))
           :post
           (str "/api/user/" user-id)
           {:edn-params (state :new-user-form)})))

(defn new-user-form [state]
  [->form state {:id :new-user-form
                 :btn {:content "Save"
                       :on-click #(submit-user-form state)}
                 :inputs [{:key :user/name
                           :label "Username"
                           :type "text"}
                          {:key :user/password
                           :label "Password"
                           :type "password"}
                          {:key :user/role
                           :label "Role"
                           :type "text"}]}])

(defn new-user-btn []
  [:button.btn.btn-primary {:on-click #(toggle-user-form)} "New user"])

(defn users-page [_]
  (load-users)
  (fn [state]
    [:<>
     [users-table state]
     (if (get-in state [:new-user-form :active])
       [new-user-form state]
       [new-user-btn])]))
