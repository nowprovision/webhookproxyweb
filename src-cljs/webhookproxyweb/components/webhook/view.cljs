(ns webhookproxyweb.components.webhook.view
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [freeman.ospa.core :refer [dispatch subscribe ratom]]
            [webhookproxyweb.components.webhook.handlers]
            [webhookproxyweb.components.webhook.subs]
            [webhookproxyweb.components.webhook.routes]
            [webhookproxyweb.utils :as utils]
            [webhookproxyweb.components.shared :refer [bind-fields
                                                       form-input 
                                                       action-button
                                                       button
                                                       table
                                                       mask-loading]]))
            
(declare listing-component update-add-component)

(defn root []
  (let [sub-screen (subscribe [:screen-changed :webhooks])
        webhooks-loaded (subscribe [:webhooks-loaded])]
    (fn [] 
      (let [[active-screen & screen-args] @sub-screen]
        [:div
         (case active-screen
           :listing
            (apply conj [listing-component] screen-args)
           :update-add
            (apply conj [update-add-component] screen-args))]))))

(defn listing-component []
  (let [webhooks (subscribe [:webhooks-changed])]
    (fn [] 
      (table
        [{ :title "Name" :key :name }
         { :title "Description" :key :description }
         { :title "Subdomain" :key :subdomain }
         { :title "Secret" :key :secret }]
        (sort-by :name @webhooks)
        (fn [item]
          [:td 
           [action-button {:id "remove-webhook"
                           :on-click #(dispatch [:webhook-removed { :id (:id item) }]) }
            [:i.material-icons "delete"]]
           [action-button {:on-click #(dispatch [:redirect :edit-webhook :webhook-id (:id item)]) }
            [:i.material-icons "mode_edit"]]
           [action-button {:on-click #(dispatch [:redirect :list-filters :webhook-id (:id item)]) }
            [:i.material-icons "lock"]]])
        "webhooks"
        [:redirect :add-webhook]
        )
      )))
           ;[:button.mdl-button.mdl-js-button.mdl-button--fab.mdl-button--mini-fab.mdl-button--colored
            ;{:on-click #(dispatch [:redirect :edit-webhook :webhook-id (:id item)]) } 
            ;[:i.material-icons "mode_edit"]]
           ;[:button.mdl-button.mdl-js-button.mdl-button--fab.mdl-button--mini-fab.mdl-button--colored
            ;{:on-click #(dispatch [:webhook-removed {:id (:id item)}] ) } 
            ;[:i.material-icons "delete"]]
           ;[:button.mdl-button.mdl-js-button.mdl-button--fab.mdl-button--mini-fab.mdl-button--colored
            ;{:on-click #(dispatch [:redirect :list-filters :webhook-id (:id item)]) } 
            ;[:i.material-icons "lock"]]
           ;]]
          ;)]]])))


(defn update-add-component [webhook-id]
  (let [form-id (utils/uuid-keyword)
        form-sub (subscribe [:form-changed form-id])
        is-new (if webhook-id false true)
        form-event (if is-new :webhook-spec-created :webhook-spec-changed)
        webhook-sub (subscribe [:webhook-changed webhook-id])
        staging (reaction (if is-new {:id (utils/uuid-str)
                                    :description ""
                                    :filtering-enabled false
                                    :subdomain "" }
                        @webhook-sub))]
    (fn [] 
      [:div
       ;(let [_ @webhook-sub]
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
           (form-input "Filtering Enabled?" {:field :checkbox 
                                             :id :filtering-enabled })
           ] staging]
         [:br]
         [:div
          [button {:on-click #(dispatch [form-event
                                         {:data @staging 
                                          :id (or webhook-id (:id @staging))
                                          :form-id form-id }]) } 
           (if is-new "Add Webhook" "Update Webhoook")]
          ]
         ;)
       ])))

