(ns webhookproxyweb.components.whitelist-editor
  (:require [re-frame.core :refer [dispatch subscribe]]
            [schema.core :as s]
            [ajax.core :refer [POST]]
            [cljs-uuid-utils.core :as uuid]
            [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields]]
            [webhookproxyweb.model :as model]))

(declare listing-component update-add-component)

(defn root-component [item]
  (let [screen-atom (subscribe [:screen-changed :whitelists])]
    (fn [item] 
      (println "re-rendering")
      (let [[active-screen & screen-args] @screen-atom]
        (println "Screen args" screen-args)
        (case active-screen
          :listing
          (apply conj [listing-component] screen-args)
          :update-add
          (apply conj [update-add-component] screen-args))))))


(defn listing-component [webhook-id]
  (let [whitelists (subscribe [:whitelists webhook-id])]
    (fn []
      [:div
       [:h2 "IP Filters"]
       [:button {:on-click #(dispatch [:change-screen 
                                       :whitelists 
                                       :update-add
                                       webhook-id]) } "Add IP Filter"]
       (when (zero? (:count @whitelists))
         [:p "There are currently no filters defined which is sad."])
       [:ul
        (for [{:keys [-scheme:chrome-extensionid description] :as whitelist} @whitelists]
          ^{:key id} [:li 
                      [:p.description description]
                      [:button {:on-click #(dispatch [:edit-whitelist id]) } "Edit"] ]) ]])))

(defn form-input [label input-attrs]
  [:div
   [:label label]
   [:input.form-control input-attrs]])

(defn update-add-component [webhook-id]
  (let [form-id (keyword (str (uuid/make-random-uuid)))
        sync-path (str "/api/webhooks/" webhook-id "/whitelists")
        form-sub (subscribe [:forms form-id])
        is-new (if item false true)
        staging (atom (or item {:id  (str (uuid/make-random-uuid))
                                :description "" :ip ""}))]
    (fn [webhook-id] 
      [:div
       [:p "Add whitelist"]
       (when (false? (:valid? @form-sub))
         [:div
          [:p "There were problems with your submission."]
          [:ul
           (for [verror (:errors @form-sub)]
             [:li  (:error verror)])]])
       [bind-fields 
        [:div
         (form-input "Description" { :field :text :id :description })
         (form-input "IP or IP maskk" { :field :text :id :ip })
         ] staging] 
       [:button {:on-click #(dispatch [:submitted {:data @staging 
                                                   :is-new is-new 
                                                   :form-id form-id 
                                                   :schema model/WhitelistEntry
                                                   :sync-path sync-path
                                                   }]) } 
        (if is-new "Add" "Update")]]
      )))


