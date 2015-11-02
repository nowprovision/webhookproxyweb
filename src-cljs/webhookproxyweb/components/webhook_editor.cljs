(ns webhookproxyweb.components.webhook-editor
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [dispatch subscribe register-sub register-handler]]
            [cljs-uuid-utils.core :as uuid]
            [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields]]
            [webhookproxyweb.model :as model]
            [webhookproxyweb.components.shared :refer [form-input 
                                                       mask-loading]]
            [webhookproxyweb.routes :as routes]))

(declare listing-component update-add-component)


(register-sub :webhooks-changed (fn [db _] 
                                  (reaction (:items @db))))

(register-sub :webhook-changed (fn [db [_ webhook-id]]
                                 (let [webhooks-changed (subscribe [:webhooks-changed])]
                                   (reaction 
                                     (first (filter #(= (:id %) webhook-id)  @webhooks-changed))))))

(register-sub :webhooks-loaded (fn [db _]
                                 (let [webhooks-changed (subscribe [:webhooks-changed])]
                                   (reaction (not (nil? @webhooks-changed))))))

(register-handler :webhook-change-submitted
                  (fn [db [_ payload]]
                       (dispatch [:submitted (merge {:schema model/WebHookProxyEntry
                                                     :sync-path "/api/webhooks"
                                                     :done-path "/" } payload)])))

(defn root-component [item]
  (let [sub-screen (subscribe [:screen-changed :webhooks])
        webhooks-loaded (subscribe [:webhooks-loaded])]
    (fn [item] 
      (mask-loading webhooks-loaded 
                    (fn [] 
                      (let [[active-screen & screen-args] @sub-screen]
                        [:div
                         (case active-screen
                           :listing
                           [:div
                            [:div
                               [:button {:on-click #(dispatch [:redirect (routes/add-webhook-path)]) } "Add New Webhook"]]
                            [:br]
                            (apply conj [listing-component] screen-args)]
                           :update-add
                           [:div
                            [:div
                               [:button {:on-click #(dispatch [:redirect (routes/list-webhooks-path)]) } "Show Webhooks"]]
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
                                           (routes/edit-webhook-path 
                                             {:webhook-id (:id item) })]) } 
            "Edit"]]
          [:td
           [:button {:on-click #(dispatch [:redirect 
                                           (routes/list-whitelists-path { :webhook-id (:id item) })]) } 
            "Edit IP Filters"]]]
          )]])))


(defn update-add-component [webhook-id]
  (let [form-id (keyword (str (uuid/make-random-uuid)))
        form-sub (subscribe [:form-changed form-id])
        is-new (if webhook-id false true)
        webhook-sub (subscribe [:webhook-changed webhook-id])
        staging (atom (if is-new {:id (str (uuid/make-random-uuid)) 
                                    :description ""
                                    :subdomain ""
                                    }
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
        [:div
           [:button {:on-click #(dispatch [:webhook-change-submitted {:data @staging :is-new is-new :form-id form-id }]) } 
            (if is-new "Add Webhook" "Update Webhoook")]]]]
        )))

