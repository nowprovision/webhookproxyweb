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
        (assoc :valid? (not errors))
        (assoc :errors errors))))
      

(defn validated [db [_ staging-payload]]
  (let [server-payload staging-payload]
    (POST "/api/webhooks" {:format :json 
                           :params staging-payload  
                           :response-format :json
                           :keywords? true
                           :handler (fn [server-payload]
                                      (dispatch [:confirmed server-payload]))
                           :server-handler (fn [server-payload]
                                             (dispatch [:rejected server-payload]))
                           })
    db))


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
  (let [valid-tuple (subscribe [:form-valid])
        is-new (if item false true)
        staging (atom (or item {:id  (str (uuid/make-random-uuid))
                                :name ""
                                :description "" 
                                :subdomain ""}))]
    (fn [] 
      (let [[valid validation-errors] @valid-tuple]
        [:div
         [:p "Add item"]
         (when-not valid
           [:p "There were problems with your submission"]
           [:ul
            (for [verror (model/errors->friendly validation-errors)]
              [:li  verror])])
         [bind-fields 
          [:div
           (form-input "Name" { :field :text :id :name })
           (form-input "Description" { :field :text :id :description })
           (form-input "Subdomain" { :field :text :id :subdomain })
           ] staging] 
         [:button {:on-click #(dispatch [:submitted { :data @staging :is-new is-new }]) } "Add Item"]]
        ))))

(defn form-input [label input-attrs]
  [:div
   [:label label]
   [:input.form-control input-attrs]])
