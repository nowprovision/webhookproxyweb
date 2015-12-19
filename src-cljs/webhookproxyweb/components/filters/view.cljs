(ns webhookproxyweb.components.filters.view
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [freeman.ospa.core :refer [subscribe dispatch ratom]]
            [webhookproxyweb.components.filters.handlers]
            [webhookproxyweb.components.filters.subs]
             [webhookproxyweb.components.filters.routes]
            [webhookproxyweb.utils :as utils]
            [webhookproxyweb.components.shared :refer [bind-fields 
                                                       form-input 
                                                       table
                                                       button
                                                       action-button
                                                       mask-loading]]))

(declare listing-component update-add-component)

(defn root []
  (let [sub-screen (subscribe [:screen-changed :filters])
        webhooks-loaded (subscribe [:webhooks-loaded])]
    (fn [] 
      (mask-loading webhooks-loaded
                    (fn []
                      (let [[active-screen webhook-id & screen-args] @sub-screen]
                        (case active-screen
                          :listing
                          (apply conj [listing-component] webhook-id screen-args)
                          :update-add
                          (apply conj [update-add-component] webhook-id screen-args)))
                      )))))


(defn listing-component [webhook-id]
  (let [webhook (subscribe [:webhook-changed webhook-id])
        filters (subscribe [:filters-changed webhook-id])]
    (fn []
      (if (true? (:filtering-enabled @webhook))
        [:div
         [:h6 
          "IP allowed list for " 
          [:a.ajax-link {:on-click 
                         #(dispatch [:redirect :edit-webhook :webhook-id webhook-id]) }
           (:name @webhook) ]
          " webhook."]
         [table
          [{ :title "Description" :key :description }
           { :title "IP" :key :ip }]
          (sort-by :description @filters)
          (fn [item]
            [:td 
             [action-button {:on-click 
                             #(dispatch [:redirect :edit-filter 
                                         :webhook-id webhook-id
                                         :filter-id (:id item)
                                         ]) }
              [:i.material-icons "mode_edit"]] 
             [action-button {:on-click 
                             #(dispatch [:filter-removed { :id (:id item)
                                                          :context { :webhook-id webhook-id }
                                                          } ]) } 
              [:i.material-icons "delete"]]]
            )
          "filters"
          [:redirect :add-filter :webhook-id webhook-id]]
         ]
        [:div
              
         [:h6.warning
          [:i.material-icons "warning"]
          [:span
          "Please enable filtering for this webhook first." 
          [:a.ajax-link 
           { :on-click #(dispatch [:redirect :edit-webhook :webhook-id webhook-id]) }
           "Edit settings"]
          ]]]
        ))))
                      

(defn update-add-component [webhook-id filter-id]
  (let [form-id (utils/uuid-keyword)
        form-sub (subscribe [:form-changed form-id])
        is-new (if filter-id false true)
        action (if is-new :filter-spec-created :filter-spec-changed)
        existing-filter (subscribe [:filter-changed webhook-id filter-id])
        staging (reaction (if is-new {:id  (utils/uuid-str) :description "" :ip ""}
                        @existing-filter))]
    (fn [webhook-id] 
      [:div
       [:h6 (if is-new "Add an allowed IP" "Edit allowed IP filter")]
       (when (false? (:valid? @form-sub))
         [:div
          [:p "There were problems with your submission."]
          [:ul
           (for [verror (:errors @form-sub)]
             [:li  (:error verror)])]])
       [bind-fields 
        [:table.table
         (form-input "Description" { :field :text :id :description :placeholder "Branch office VPN gateway" })
         (form-input "IP address" { :field :text :id :ip :placeholder "1.2.3.4" })
         ] staging] 
       [:br]
       [button {:on-click #(dispatch [action {:data @staging 
                                               :id (:id @staging)
                                               :context { :webhook-id webhook-id }
                                               :form-id form-id }]) } 
        (if is-new "Add Filter" "Update Filter")]]
      )))
