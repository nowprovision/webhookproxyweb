(ns webhookproxyweb.components.add-edit-form
  (:require [re-frame.core :refer [dispatch subscribe]]
            [schema.core :as s]
            [ajax.core :refer [POST]]
            [cljs-uuid-utils.core :as uuid]
            [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields]]
            [webhookproxyweb.model :as model]))

(defn submitted [db [_ payload]]
  (let [errors (->>  (:data payload) (s/check model/WebHookProxyEntry))]
    (when-not errors
      (dispatch [:validated payload]))
    (-> db
        (assoc-in [:forms (:form-id payload) :valid?] (not errors))
        (assoc-in [:forms (:form-id payload) :errors] 
                  (map model/error->friendly errors)))))
      

(defn validated [db [_ staging-payload]]
  (let [server-payload (select-keys staging-payload [:data :is-new])]
    (POST "/api/webhooks" {:format :json 
                           :params server-payload  
                           :response-format :json
                           :keywords? true
                           :handler (fn [server-payload]
                                      (dispatch [:confirmed server-payload]))
                           :error-handler (fn [error]
                                             (dispatch [:sync-errored (:form-id staging-payload) error]))
                           })
    db))

(defn sync-errored [db [_ form-id sync-error]]
  (-> db
      (assoc-in [:forms form-id :valid?] false)
      (update-in [:forms form-id :errors]  #(conj % (:response sync-error)))))

(defn- upsert [coll item]
  (conj (filter #(not= (:id %) (:id item)) coll) item))

(defn confirmed [db [_ payload]]
  "merge payload into main db atom"
  (let [items (:items db)]
    (-> db
      (assoc :items (upsert items payload))
      (assoc :active-screen [:listing]))))

(defn rejected [db [_ payload]]
  "if server returns an error or bad response"
  db)

(declare form-input) 

(defn component [item]
  (let [form-id (keyword (str (uuid/make-random-uuid)))
        form-sub (subscribe [:forms form-id])
        is-new (if item false true)
        staging (atom (or item {:id  (str (uuid/make-random-uuid))
                                :name ""
                                :description "" 
                                :subdomain ""}))]
    (fn [] 
      [:div
       [:p "Add item"]
       (when (false? (:valid? @form-sub))
         [:div
          [:p "There were problems with your submission."]
          [:ul
           (for [verror (:errors @form-sub)]
             [:li  (:error verror)])]])
       [bind-fields 
        [:div
         (form-input "Name" { :field :text :id :name })
         (form-input "Description" { :field :text :id :description })
         (form-input "Subdomain" { :field :text :id :subdomain })
         ] staging] 
       [:button {:on-click #(dispatch [:submitted { :data @staging :is-new is-new :form-id form-id }]) } 
          (if is-new "Add" "Update")]]
      )))

(defn form-input [label input-attrs]
  [:div
   [:label label]
   [:input.form-control input-attrs]])
