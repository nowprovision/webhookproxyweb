(ns webhookproxyweb.components.webhook.view
  (:require [webhookproxyweb.components.webhook.handlers]
            [webhookproxyweb.components.webhook.subs]
            [webhookproxyweb.components.webhook.routes 
             :refer [add-webhook-path 
                     edit-webhook-path
                     ;list-whistlists-path
                     list-webhooks-path]]
            [webhookproxyweb.components.filters.routes 
             :refer [list-filters-path]]
            [reagent-forms.core :refer [bind-fields]]
            [reagent.core :refer [atom]]
            [webhookproxyweb.utils :as utils]
            [webhookproxyweb.components.shared :refer [form-input mask-loading]]
            [re-frame.core :refer [dispatch subscribe]]))

(declare listing-component update-add-component)

(defn root []
  (let [sub-screen (subscribe [:screen-changed :webhooks])
        webhooks-loaded (subscribe [:webhooks-loaded])]
    (fn [] 
      (mask-loading webhooks-loaded 
                    (fn [] 
                      (let [[active-screen & screen-args] @sub-screen]
                        [:div
                         (case active-screen
                           :listing
                           [:div
                            [:div
                             [:button {:on-click #(dispatch [:redirect (add-webhook-path)]) } "Add New Webhook"]]
                            [:br]
                            (apply conj [listing-component] screen-args)]
                           :update-add
                           [:div
                            [:div
                             [:button {:on-click #(dispatch [:redirect (list-webhooks-path)]) } "Show Webhooks"]]
                            (apply conj [update-add-component] screen-args)])]))
                    ))))

(defn listing-component []
  (let [webhooks (subscribe [:webhooks-changed])]
    (fn [] 
      [:div
       [:table.listing
        [:thead
         [:th "Name"]
         [:th "Description"]
         [:th "Subomain"]
         [:th ""]
         [:th ""]]
       (for [item (sort-by :name @webhooks)]
         ^{:key (:name item)} 
         [:tr 
          [:td (:name item)]
          [:td (:description item)]
          [:td (:subdomain item)]
          [:td 
           [:button {:on-click #(dispatch [:redirect 
                                           (edit-webhook-path 
                                             {:webhook-id (:id item) })]) } 
            "Edit"]]
          [:td
           [:button {:on-click #(dispatch [:redirect 
                                           (list-filters-path { :webhook-id (:id item) })]) } 
            "Edit IP Filters"]]]
          )]])))


(defn update-add-component [webhook-id]
  (let [form-id (utils/uuid-keyword)
        form-sub (subscribe [:form-changed form-id])
        is-new (if webhook-id false true)
        webhook-sub (subscribe [:webhook-changed webhook-id])
        staging (atom (if is-new {:id (utils/uuid-str)
                                    :description ""
                                    :subdomain "" }
                        @webhook-sub))]
    (fn [] 
      [:div
       [:h2 "Add new webhook"]
       (when (false? (:valid? @form-sub))
         [:div
          [:p "There were problems with your submission."]
          [:ul
           (for [verror (:errors @form-sub)]
             [:li  (:error verror)])]])
      [bind-fields 
        [:table
        (form-input "Name" {:field :text 
                            :id :name 
                            :placeholder "Name"})
        (form-input "Description" {:field :text 
                                  :id :description 
                                  :placeholder "Description"})
        (form-input "Subdomain" {:field :text 
                                :id :subdomain 
                                :placeholder "Subdomain"})
        ] staging]
       [:br]
       [:div
        [:button {:on-click #(dispatch [:webhook-change-submitted {:data @staging :is-new is-new :form-id form-id }]) } 
            (if is-new "Add Webhook" "Update Webhoook")]
        ]
       ])))

