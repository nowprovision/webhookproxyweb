(ns webhookproxyweb.components.add-edit-form
  (:require [re-frame.core :refer [dispatch subscribe]]
            [schema.core :as s]
            [cljs-uuid-utils.core :as uuid]
            [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields]]
            [webhookproxyweb.model :as model]))

(defn submitted [db [_ staging-payload]]
  (let [errors (->>  staging-payload (s/check model/WebHookProxyEntry))]
    (when-not errors
      (dispatch [:validated staging-payload]))
    (-> db
        (assoc :valid? (not errors))
        (assoc :errors errors))))
      

(defn validated [db [_ staging-payload]]
  "send payload to server, mocked for now"
  (let [server-payload staging-payload]
    (.setTimeout js/window #(dispatch [:confirmed server-payload]) 10)
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
        staging (atom (or item {:id  (uuid/make-random-uuid)
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
         [:button {:on-click #(dispatch [:submitted @staging]) } "Add Item"]]
        ))))

(defn form-input [label input-attrs]
  [:div
   [:label label]
   [:input.form-control input-attrs]])
