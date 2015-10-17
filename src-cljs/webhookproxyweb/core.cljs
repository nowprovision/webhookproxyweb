(ns webhookproxyweb.core
  (:require-macros [reagent.ratom :refer [reaction]])  
  (:require [webhookproxyweb.model :as model]
            [webhookproxyweb.components.add-edit-form :as add-edit-form]
            [webhookproxyweb.components.listing :as listing]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch
                                   subscribe]]))

(enable-console-print!)

(def seed-data  
  [{:id 101 :name "github" :description "github webhook" :subdomain "gh" }
   {:id 102 :name "google drive" :description "google drive sdk" :subdomain "google" }
   {:id 103 :name "dropbox" :description "dropbox webhook" :subdomain "dropbox"}])

(defn fake-initialize [db _]
  (js/setTimeout (fn [] (dispatch [:reset-db seed-data])) 250)
  db) 

(defn reset-db [db [_ payload]]
  (assoc db :items payload))

(register-handler :initialize fake-initialize)
(register-handler :reset-db  reset-db)

(register-handler :submitted add-edit-form/submitted)
(register-handler :validated add-edit-form/validated)
(register-handler :confirmed add-edit-form/confirmed)
(register-handler :rejected add-edit-form/rejected)

(register-sub :form-valid (fn [db _] (reaction [(:valid? @db) (:errors @db)])))
(register-sub :items-changed (fn [db _] (reaction (:items @db))))
(register-sub :screen-changed (fn [db _] (reaction (or (:active-screen @db) :listing))))

(defn change-screen [db [_ screen]]
  (assoc db :active-screen screen))

(register-handler :change-screen change-screen)

(defn root-template [] 
  (let [active-screen (subscribe [:screen-changed])]
    (fn []
      [:div 
       [:h1 "Webhookproxy"] 
       (case @active-screen
         :add-form
         [:div
          [:button  {:on-click #(dispatch [:change-screen :listing]) } "Show listing"]
          [add-edit-form/component]]
         :listing
         [:div
          [:button  {:on-click #(dispatch [:change-screen :add-form]) } "Show add form"]
          [listing/component]]
         )
       ])))

(defn root-render [& args] 
  (reagent/render [root-template] (js/document.getElementById "app")))

(defn ^:export run
  []
  (dispatch [:initialize])
  (root-render))


