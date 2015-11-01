(ns webhookproxyweb.forms
  (:require-macros [reagent.ratom :refer [reaction]]) 
  (:require [re-frame.core :refer [dispatch subscribe]]
            [schema.core :as s]
            [ajax.core :refer [POST]]
            [cljs-uuid-utils.core :as uuid]
            [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields]]
            [webhookproxyweb.model :as model]
            [webhookproxyweb.routing :as routing]
            [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch
                                   subscribe]]))

(declare validate sync-to-server add-validation-errors merge-sync-payload add-sync-errors)

(register-sub :forms-changed (fn [db _] (reaction (:forms @db))))

(register-sub :form-changed (fn [db [_ form-id]]
                              (let [forms (subscribe [:forms-changed])]
                                (reaction (get @forms form-id)))))



(defn validate [db [_ { :keys [data schema form-id] :as payload}]]
  (if-let [errors (s/check schema data)]
    (dispatch [:rejected form-id errors])
    (dispatch [:validated payload]))
  db)

(defn add-validation-errors [db [_ form-id errors]]
  (let [form-path [:forms form-id]]
    (-> db
        (assoc-in (conj form-path :valid?) false)
        (assoc-in (conj form-path :errors) (map model/error->friendly errors)))))

(defn sync-to-server [db [_ {:keys [form-id sync-path done-path] :as payload }]]
  (let [server-payload (select-keys payload [:data :is-new])
        confirmed-handler (fn [server-resp] (dispatch [:sync-confirmed form-id server-resp done-path]))
        err-handler (fn [error] (dispatch [:sync-errored form-id (:response error)]))]
    (->> {:format :json 
          :params server-payload  
          :response-format :json
          :keywords? true
          :handler confirmed-handler 
          :error-handler err-handler }
         (POST sync-path))
    db))

(defn add-sync-errors [db [_ form-id error]]
  (let [form-path [:forms form-id]]
    (-> db
        (assoc-in (conj form-path :valid?) false)
        (assoc-in (conj form-path :errors) [(if (:error error) error 
                                              { :error "Unexpected server error" })]))))


(defn- upsert [coll item]
  (conj (filter #(not= (:id %) (:id item)) coll) item))

(defn merge-sync-payload [db [_ form-id payload done-path]]
  (let [items (:items db)]
    (routing/transition! done-path)
    (assoc db :items (upsert items payload))
    ))

(register-handler :submitted validate) ; when submit validate
(register-handler :validated sync-to-server) ; once validated send to server
(register-handler :rejected add-validation-errors) ; handle validation rejection
(register-handler :sync-confirmed merge-sync-payload); once sync confirmed merge into ratom
(register-handler :sync-errored add-sync-errors) ; handle sync errors
