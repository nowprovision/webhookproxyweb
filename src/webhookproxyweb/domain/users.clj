(ns webhookproxyweb.domain.users
  "services for users"
  (:require [clj-uuid :as uuid]
            [korma.core :refer [insert limit select values where]]
            [webhookproxyweb.db :refer [user-entity with-db]]
            [webhookproxyweb.external.github :as github]))


(defrecord Users [db gh])

(declare find-by add)

(defn github-enrollment-and-identify [users code]
  (let [result (github/authenticate (:gh users) code)
        github-id (-> result :identity :id)
        github-email (-> result :identity :email)
        existing-user (find-by users :github-id github-id)
        user (or (first existing-user) 
                 (add users {:provider "github"
                             :uid github-id
                             :email github-email }))
        user-id (:id user)]
    user-id))

    
(defmulti find-by (fn [users & args] (first args)))

(defmethod find-by :github-id [{:keys [db]} _ uid]
  (with-db db
    (select user-entity
            (where {:provider "github" :uid (str uid)  })
            (limit 1))))

(defn- add [{:keys [db]} {:keys [provider uid email] }]
  (let [auto-id (str (uuid/v4))]
    (with-db db
      (insert user-entity
              (values {:id auto-id 
                       :provider provider
                       :uid (str uid)
                       :email email })))))
