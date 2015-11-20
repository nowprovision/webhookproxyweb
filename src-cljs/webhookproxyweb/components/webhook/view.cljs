(ns webhookproxyweb.components.webhook.view
  (:require [freeman.ospa.core :refer [dispatch subscribe ratom]]
            [webhookproxyweb.components.webhook.handlers]
            [webhookproxyweb.components.webhook.subs]
            [webhookproxyweb.components.webhook.routes]
            [webhookproxyweb.utils :as utils]
            [webhookproxyweb.components.shared :refer [bind-fields
                                                       form-input 
                                                       mask-loading]]))
            
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
                             [:button.btn {:on-click #(dispatch [:redirect :add-webhook]) } "Add New Webhook"]]
                            [:br]
                            (apply conj [listing-component] screen-args)]
                           :update-add
                           [:div
                            [:div
                             [:button.btn {:on-click #(dispatch [:redirect :list-webhooks]) } "Show Webhooks"]]
                            (apply conj [update-add-component] screen-args)])]))
                    ))))

(defn listing-component []
  (let [webhooks (subscribe [:webhooks-changed])]
    (fn [] 
      [:div
       [:table.listing.table
        [:thead
         [:th "Name"]
         [:th "Description"]
         [:th "Subdomain"]
         [:th "Secret"]
         [:th ""]]
       [:tbody
       (for [item (sort-by :name @webhooks)]
         ^{:key (:name item)} 
         [:tr 
          [:td (:name item)]
          [:td (:description item)]
          [:td (:subdomain item)]
          [:td (:secret item)]
          [:td 
           [:button.btn {:on-click #(dispatch [:webhook-removed {:id (:id item)}] ) } 
            "Delete"]
           [:button.btn {:on-click #(dispatch [:redirect :edit-webhook :webhook-id (:id item)]) } 
            "Edit"]
           [:button.btn {:on-click #(dispatch [:redirect :list-filters :webhook-id (:id item)]) } 
            "Edit IP Filters"]]]
          )]]])))


(defn update-add-component [webhook-id]
  (let [form-id (utils/uuid-keyword)
        form-sub (subscribe [:form-changed form-id])
        is-new (if webhook-id false true)
        form-event (if is-new :webhook-spec-created :webhook-spec-changed)
        webhook-sub (subscribe [:webhook-changed webhook-id])
        staging (ratom (if is-new {:id (utils/uuid-str)
                                    :description ""
                                    :subdomain "" }
                        @webhook-sub))]
    (fn [] 
      [:div
       [:h3 "Add new webhook"]
       (when (false? (:valid? @form-sub))
         [:div
          [:p "There were problems with your submission."]
          [:ul
           (for [verror (:errors @form-sub)]
             [:li  (:error verror)])]])
      [bind-fields 
        [:table.table
        (form-input "Name" {:field :text 
                            :id :name 
                            :placeholder "Name"})
        (form-input "Description" {:field :text 
                                  :id :description 
                                  :placeholder "Description"})
        (form-input "Subdomain" {:field :text 
                                 :id :subdomain 
                                 :placeholder "Subdomain"})
        (form-input "Secret" {:field :text 
                              :id :secret 
                              :placeholder "Secret url suffix"})
        ] staging]
       [:br]
       [:div
        [:button.btn {:on-click #(dispatch [form-event
                                        {:data @staging 
                                         :id (or webhook-id (:id @staging))
                                         :form-id form-id }]) } 
            (if is-new "Add Webhook" "Update Webhoook")]
        ]
       ])))

