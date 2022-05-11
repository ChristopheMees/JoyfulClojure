(ns auth
  (:require [buddy.auth :as auth]
            [buddy.auth.backends.token :refer [jwe-backend]]
            [buddy.core.nonce :as nonce]
            [buddy.sign.jwt :as jwt]
            [crypto :refer [pwd-hash]]
            [db :refer [qe]]
            [response :refer [forbidden ok unauthorized]]))

(def secret (nonce/random-bytes 32))
(def auth-backend (jwe-backend {:secret secret
                                :token-name "Bearer"
                                :options {:alg :a256kw
                                          :enc :a128gcm}}))

(defn user-claims [{:user/keys [name role]}]
  {:user/name name :user/role role})

(defn new-jwt [user]
  (jwt/encrypt (user-claims user) secret {:alg :a256kw :enc :a128gcm}))

(defn parse-identity [handler]
  (fn [req]
    (if (:identity req)
      (handler (update-in req [:identity :user/role] keyword))
      (handler req))))

(defn authenticated? [handler]
  (fn [req]
    (if-not (auth/authenticated? req)
      (unauthorized)
      (handler req))))

(defn login [db req]
  (let [{:user/keys [name password]} (req :body-params)
        user (qe db {:identity [:user/name name]
                     :query [:user/name :user/password :user/role]})]
    (if-not (or user
                (= (user :user/password)
                   (pwd-hash name password)))
      (unauthorized)
      (ok {:body (new-jwt user)}))))

(defn has-role? 
  [roles]
  {:pre [(set? roles)]}
  (fn [handler]
    (fn [req]
      (if (roles (-> req :identity :user/role))
        (handler req)
        (forbidden)))))
