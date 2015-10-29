(ns webhookproxyweb.components.add-edit-form
  (:require [re-frame.core :refer [dispatch subscribe]]
            [schema.core :as s]
            [ajax.core :refer [POST]]
            [cljs-uuid-utils.core :as uuid]
            [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields]]
            [webhookproxyweb.model :as model]))

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
       [:button {:on-click #(dispatch [:submitted 
                                       {:data @staging 
                                        :schema model/WebHookProxyEntry
                                        :sync-path "/api/webhooks"
                                        :is-new is-new 
                                        :form-id form-id }]) } 
          (if is-new "Add" "Update")]]
      )))

(defn form-input [label input-attrs]
  [:div
   [:label label]
   [:input.form-control input-attrs]])
