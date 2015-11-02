(ns webhookproxyweb.components.whitelist-editor
  (:require-macros [reagent.ratom :refer [reaction]]  
                   [secretary.core :refer [defroute]])
  (:require [re-frame.core :refer [dispatch 
                                   subscribe
                                   register-sub]]
            [schema.core :as s]
            [cljs-uuid-utils.core :as uuid]
            [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields]]
            [webhookproxyweb.components.shared :refer [form-input]]
            [webhookproxyweb.model :as model]
            [webhookproxyweb.routing :as routing]))

(declare listing-component update-add-component)

(register-sub :whitelists (fn [db [_ webhook-id & args]]
                            (reaction 
                              (let [whitelist-id (first args)
                                    whitelists (or (:whitelist 
                                                     (first 
                                                       (filter #(= (:id %) webhook-id) (:items @db)))) [])]
                                (if whitelist-id 
                                  (first (filter #(= (:id %) whitelist-id) whitelists))
                                  whitelists
                                  )))))


(defn root-component [item]
  (let [screen-atom (subscribe [:screen-changed :whitelists])
        webhook-sub (subscribe [:webhooks-changed])]
    (fn [item] 
      (if @webhook-sub
        (let [[active-screen & screen-args] @screen-atom]
          (case active-screen
            :listing
            [:div
             [:table
              [:tr
               [:td
                [:button { :on-click #(routing/transition! "/") }
                 "Show All Webhooks"]]
               [:td
                [:button { :on-click #(routing/transition! (str "/webhooks/" (first screen-args))) }
                 "Edit Webhook"]]] ]
             (apply conj [listing-component] screen-args)]
            :update-add
            (apply conj [update-add-component] screen-args)))
        [:div "Loading"] 
        ))))


(defn listing-component [webhook-id]
  (let [whitelists (subscribe [:whitelists webhook-id])]
    (fn []
      [:div
       [:h2 "IP Filters"]
       [:button {:on-click #(routing/transition! (str "/tasks/"
                                                      webhook-id
                                                      "/new-whitelist")) } "Add IP Filter"]
       (when (zero? (:count @whitelists))
         [:p "There are currently no filters defined which is sad."])
       [:table
        [:thead
         [:th "Description"]
         [:th "IP"]
         [:th ""]]
        (for [{:keys [id ip description] :as whitelist} @whitelists]
          ^{:key id} [:tr 
                      [:td description]
                      [:td ip]
                      [:td 
                      [:button {:on-click #(routing/transition! 
                                             (str "/webhooks/"
                                                  webhook-id
                                                  "/whitelists/"
                                                  id)) } "Edit"] ]
                      ]) ]])))

(defn update-add-component [webhook-id whitelist-id]
  (let [form-id (keyword (str (uuid/make-random-uuid)))
        sync-path (str "/api/webhooks/" webhook-id "/whitelists")
        form-sub (subscribe [:form-changed form-id])
        is-new (if whitelist-id false true)
        whitelist-sub (subscribe [:whitelists webhook-id whitelist-id])
        staging (atom (if is-new {:id  (str (uuid/make-random-uuid))
                                  :description "" :ip ""}
                        
                        @whitelist-sub))]
    (fn [webhook-id] 
      [:div
       [:h2 "Add allowed IP or IP range"]
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
       [:button {:on-click #(dispatch [:submitted {:data @staging 
                                                   :done-path (str "/webhooks/"
                                                                   webhook-id
                                                                   "/whitelists")
                                                   :is-new is-new 
                                                   :form-id form-id 
                                                   :schema model/WhitelistEntry
                                                   :sync-path sync-path
                                                   }]) } 
        (if is-new "Add Filter" "Update Filter")]]
      )))


