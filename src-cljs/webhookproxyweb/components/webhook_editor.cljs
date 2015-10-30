(ns webhookproxyweb.components.webhook-editor
  (:require-macros [reagent.ratom :refer [reaction]]  
                   [secretary.core :refer [defroute]])
  (:require [re-frame.core :refer [dispatch subscribe register-sub]]
            [cljs-uuid-utils.core :as uuid]
            [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields]]
            [webhookproxyweb.model :as model]
            [webhookproxyweb.components.shared :refer [form-input]]
            [webhookproxyweb.routing :as routing]))

(declare listing-component update-add-component)

(register-sub :webhooks (fn [db [_ & args]]
                            (reaction 
                                (if-let [webhook-id (first args)]
                                  (first (filter #(= (:id %) webhook-id) (:items @db))) 
                                  (seq (sort-by :name (:items @db))))
                               )))

(defroute "/" [] 
  (dispatch [:change-screen :webhooks :listing]))

(defroute "/tasks/new-webhook" []
  (dispatch [:change-screen :webhooks :update-add]))

(defroute "/webhooks/:webhook-id" [webhook-id] 
  (dispatch [:change-screen :webhooks :update-add webhook-id]))

(defn root-component [item]
  (let [screen-atom (subscribe [:screen-changed :webhooks])
        webhook-sub (subscribe [:webhooks])]
    (fn [item] 
      (if @webhook-sub
        (let [[active-screen & screen-args] @screen-atom]
          [:div
           (case active-screen
            :listing
            [:div
             [:table
              [:tr
               [:td
                [:button {:on-click #(routing/transition! "/tasks/new-webhook")} "Add New Webhook"]]]]
             [:br]
             (apply conj [listing-component] screen-args)]
            :update-add
            [:div
             [:table
              [:tr
               [:td
                [:button {:on-click #(routing/transition! "/")} "Show Webhooks"]]]]
             (apply conj [update-add-component] screen-args)])])
        [:div "Loading"] 
        ))))

(defn listing-component []
  (let [items (subscribe [:webhooks])]
    (fn [] 
      [:div
       [:table.listing
        [:thead
         [:th "Name"]
         [:th "Description"]
         [:th "Subomain"]
         [:th ""]
         [:th ""]]
       (for [item @items]
         ^{:key (:name item)} 
         [:tr 
          [:td (:name item)]
          [:td (:description item)]
          [:td (:subdomain item)]
          [:td 
           [:button {:on-click #(routing/transition! (str "/webhooks/" (:id item)))} "Edit"]]
          [:td
           [:button {:on-click #(routing/transition! (str "/webhooks/" (:id item) "/whitelists")) } "Edit IP Filters"]]]
          )]])))


(defn update-add-component [webhook-id]
  (let [form-id (keyword (str (uuid/make-random-uuid)))
        form-sub (subscribe [:forms form-id])
        is-new (if webhook-id false true)
        webhook-sub (subscribe [:webhooks webhook-id])
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
        [:table
         [:tr
          [:td
           [:button {:on-click #(dispatch [:submitted 
                                           {:data @staging 
                                            :schema model/WebHookProxyEntry
                                            :sync-path "/api/webhooks"
                                            :done-path "/"
                                            :is-new is-new 
                                            :form-id form-id }]) } 
            (if is-new "Add Webhook" "Update Webhoook")]]]]]]
        )))

