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
      [:div
       (table
         [{ :title "Name" :key :name }
          { :title "Description" :key :description }
          { :title "Subdomain" :key :subdomain }
          { :title "Secret" :key :secret }]
         (sort-by :name @webhooks)
         (fn [item]
           [:td 
            [action-button {:title "Edit Webhook"
                            :on-click #(dispatch [:redirect :edit-webhook :webhook-id (:id item)]) }
             [:i.material-icons "mode_edit"]]
            [action-button {:title "Edit IP Filters"
                            :on-click #(dispatch [:redirect :list-filters :webhook-id (:id item)]) }
             [:i.material-icons "lock"]]
            [action-button {:title "Remove Webhook"
                            :on-click #(do (when (.confirm js/window "Are you sure you want to delete this webhook?")
                                             (dispatch [:webhook-removed { :id (:id item) }]))) }
             [:i.material-icons "delete"]]])
         "webhooks"
         [:redirect :add-webhook]
         )
       [:div.table-footer-link
        [:a.ajax-link {:alt "add new"
                       :on-click #(dispatch [:redirect :add-webhook]) } 
         [:i.material-icons "add"]  
         [:span "Add new webhook"]
         ]]
       [:div.instructions
        [:h6 "Usage instructions:"]
        [:p "1) Webhook URL is https://<subdomain>.webhookproxy.com/webhook/<secret>"]
        [:small "This is the post URL you provide to the third party as your webhook endpoint / postback URL"]
        [:p "2) Long Poll URL is https://<subdomain>.webhookproxy.com/poll/<secret>"]
        [:small "This is the get URL you long poll with your http client lib to pick up new payloads. 
                If no payload is available after 30 seconds it returns a 204 empty response, otherwise
                provides the payload in the body with a 200 response. You can run any number of
                concurrent clients, or simple loop with curl/wget if using a script."]
        [:p "3) Reply URL is https://<subdomain>.webhookproxy.com/reply/<secret>"]
        [:small "This is the reply URL you post to in order to return a repsonse to original web hook callee,
                using a X-InReplyTo request header which matches the X-ReplyId header found in payload response of 2"]
        ]
       ]
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
           (form-input "Auto Reply? (empty 200)" {:field :checkbox 
                                             :id :autoreply })
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

