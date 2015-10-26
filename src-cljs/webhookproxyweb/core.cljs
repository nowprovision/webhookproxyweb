(ns webhookproxyweb.core
  (:require-macros [reagent.ratom :refer [reaction]])  
  (:require [cljs-uuid-utils.core :as uuid]
            [ajax.core :refer [GET POST]]
            [webhookproxyweb.model :as model]
            [webhookproxyweb.components.add-edit-form :as add-edit-form]
            [webhookproxyweb.components.listing :as listing]
            [reagent.core :as reagent]
            [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch
                                   subscribe]]))

(enable-console-print!)

(def seed-data  
  [{:id (uuid/make-random-uuid) :name "github" :description "github webhook" :subdomain "gh" }
   {:id (uuid/make-random-uuid) :name "google drive" :description "google drive sdk" :subdomain "google" }
   {:id (uuid/make-random-uuid) :name "dropbox" :description "dropbox webhook" :subdomain "dropbox"}])

(defn fake-initialize [db _]
  (GET "/api/webhooks" {:response-format :json
                        :keywords? true 
                        :handler (fn [seed-data] 
                                   (dispatch [:logged-in])
                                   (dispatch [:reset-db seed-data]))
                        :error-handler (fn [] nil)
                        })
  db) 

(defn reset-db [db [_ payload]]
  (assoc db :items payload))

(register-handler :initialize fake-initialize)
(register-handler :reset-db  reset-db)

(register-handler :submitted add-edit-form/submitted)
(register-handler :validated add-edit-form/validated)
(register-handler :confirmed add-edit-form/confirmed)
(register-handler :rejected add-edit-form/rejected)

(register-handler :logged-in (fn [db [_ payload]]
                               (assoc db :active-user true)))

(register-handler :log-out (fn [db [_ payload]]
                             (POST "/logout" {:format :json
                                              :params {}
                                              :response-format :json})
                               (dissoc db :active-user)))

(register-sub :form-valid (fn [db _] (reaction [(:valid? @db) (:errors @db)])))
(register-sub :items-changed (fn [db _] (reaction (:items @db))))
(register-sub :screen-changed (fn [db _] (reaction (or (:active-screen @db) [:listing]))))

(register-sub :logged-in (fn [db _] (reaction (or (:active-user @db) false))))

(defn change-screen [db sargs]
  (println sargs)
  (assoc db :active-screen (rest sargs)))

(register-handler :change-screen change-screen)

;(register-handler :show-edit add-edit-form/show-edit)

(defn root-template [] 
  (let [logged-in (subscribe [:logged-in])
        screen-atom (subscribe [:screen-changed])]
    (fn []
      (if @logged-in
        (let [[active-screen & screen-args] @screen-atom]
          [:div 
           [:h1 "Webhookproxy"] 
           [:button {:on-click #(dispatch [:log-out]) } "Logout"]
           (case active-screen
             :add-form
             [:div
              [:button  {:on-click #(dispatch [:change-screen :listing]) } "Show listing"]
              (apply conj [add-edit-form/component] screen-args)]
             :listing
             [:div
              [:button  {:on-click #(dispatch [:change-screen :add-form]) } "Show add form"]
              [listing/component]]
             )
           ])
        [:div 
         [:h1 "Please Login"]
         [:a { :target "_new" :href "https://github.com/login/oauth/authorize?scope=user:email&client_id=db15f5f3cf7a6e1168a1" } "Auth to github"]]
        ))))

(defn ^:export rootrender [& args] 
  (reagent/render [root-template] (js/document.getElementById "app")))

(defn ^:export login [& args]
  (dispatch [:logged-in]))


(defn ^:export run
  []
  (dispatch [:initialize])
  (rootrender))




