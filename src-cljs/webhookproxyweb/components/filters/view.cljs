(ns webhookproxyweb.components.filters.view
  (:require [webhookproxyweb.components.filters.handlers]
            [webhookproxyweb.components.filters.subs]
            [webhookproxyweb.components.webhook.routes 
             :refer [list-webhooks-path
                     edit-webhook-path]]
            [webhookproxyweb.components.filters.routes 
             :refer [list-filters-path
                     add-filter-path
                     ;list-whistlists-path
                     edit-filter-path]]
            [reagent-forms.core :refer [bind-fields]]
            [webhookproxyweb.utils :as utils]
            [webhookproxyweb.components.shared :refer [form-input mask-loading]]
            [reagent.core :refer [atom]]
            [re-frame.core :refer [dispatch subscribe]]))

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
                          [:div
                           [:table
                            [:tr
                             [:td
                              [:button { :on-click #(dispatch [:redirect (list-webhooks-path)]) }
                               "Show All Webhooks"]]
                             [:td
                              [:button { :on-click #(dispatch [:redirect (edit-webhook-path { :webhook-id webhook-id })]) }
                               "Edit Webhook Settings"]]] ]
                           (apply conj [listing-component] webhook-id screen-args)]
                          :update-add
                          (apply conj [update-add-component] webhook-id screen-args)))
                      )))))


(defn listing-component [webhook-id]
  (let [filters (subscribe [:filters-changed webhook-id])]
    (fn []
      [:div
       [:h2 "IP Filters"]
       [:button {:on-click #(dispatch [:redirect (add-filter-path { :webhook-id webhook-id })]) } "Add IP Filter"]
       (when (zero? (:count @filters))
         [:p "There are currently no IP filters for this webhook defined."])
       [:table
        [:thead
         [:th "Description"]
         [:th "IP"]
         [:th ""]]
        (for [{:keys [id ip description] :as vfilter} 
              (sort-by :description @filters)]
          ^{:key id} [:tr 
                      [:td description]
                      [:td ip]
                      [:td 
                      [:button {:on-click #(dispatch [:redirect
                                                      (edit-filter-path 
                                                        {:webhook-id webhook-id 
                                                         :filter-id id })]) } "Edit"] ]
                      ]) ]])))

(defn update-add-component [webhook-id filter-id]
  (let [form-id (utils/uuid-keyword)
        sync-path (str "/api/webhooks/" webhook-id "/whitelists")
        form-sub (subscribe [:form-changed form-id])
        is-new (if filter-id false true)
        existing-filter (subscribe [:filter-changed webhook-id filter-id])
        staging (atom (if is-new {:id  (utils/uuid-str) :description "" :ip ""}
                        @existing-filter))]
    (fn [webhook-id] 
      [:div
       [:h2 (if is-new "Add an allowed IP or IP range"
                        "Edit allowed IP filter")]
       (when (false? (:valid? @form-sub))
         [:div
          [:p "There were problems with your submission."]
          [:ul
           (for [verror (:errors @form-sub)]
             [:li  (:error verror)])]])
       [bind-fields 
        [:table
         (form-input "Description" { :field :text :id :description :placeholder "Branch office VPN gateway" })
         (form-input "IP or IP mask" { :field :text :id :ip :placeholder "123.123.123.123/24" })
         ] staging] 
       [:br]
       [:button {:on-click #(dispatch [:redirect (list-filters-path { :webhook-id webhook-id}) ]) } "Cancel"]
       [:button {:on-click #(dispatch [:filter-change-submitted webhook-id {:data @staging 
                                                                            :is-new is-new 
                                                                            :form-id form-id }]) } 
        (if is-new "Add Filter" "Update Filter")]]
      )))
