(ns webhookproxyweb.core
  (:require-macros [reagent.ratom :refer [reaction]]  
                   [webhookproxyweb.config :refer [from-config]]
                   [secretary.core :refer [defroute]])
  (:require [cljs-uuid-utils.core :as uuid]
            [ajax.core :refer [GET POST]]
            [webhookproxyweb.model :as model]
            [webhookproxyweb.components.webhook-editor :as webhook-editor]
            [webhookproxyweb.components.whitelist-editor :as whitelist-editor]
            [webhookproxyweb.forms :as forms]
            [reagent.core :as reagent]
            [re-frame.db]
            [re-frame.core :refer [register-handler 
                                   register-sub 
                                   dispatch 
                                   dispatch-sync
                                   subscribe]]
            [webhookproxyweb.routing :as routing]))

(enable-console-print!)

                                                    

(defn fetch-webhooks [db _]
  (GET "/api/webhooks" {:response-format :json
                        :keywords? true 
                        :handler (fn [webhooks] 
                                   (dispatch [:reset-webhooks webhooks]))
                        :error-handler (fn [] nil)
                        })
  db) 

(defn fetch-identity [db _]
  (GET "/whoami" {:response-format :json
                  :keywords? true 
                  :handler (fn [user]
                             (when (:authenticated? user)
                               (dispatch [:reset-identity user])
                               ))
                  :error-handler (fn [] nil)
                  })
  db) 

(defn reset-webhooks [db [_ payload]]
  (assoc db :items payload))

(defn reset-identity [db [_ payload]]
  (assoc db :logged-in-user payload))

(register-handler :fetch-webhooks fetch-webhooks)
(register-handler :fetch-identity  fetch-identity)
(register-handler :reset-webhooks  reset-webhooks)
(register-handler :reset-identity  reset-identity)


(register-handler :log-out (fn [db [_ payload]]
                             (POST "/logout" {:format :json
                                              :params {}
                                              :response-format :json})
                               (dissoc db :active-user)))

(register-sub :form-valid (fn [db _] (reaction [(:valid? @db) (:errors @db)])))

(register-sub :screen-changed (fn [db [_ & filters]]
                                (reaction
                                   (let [active-screen (:active-screen @db)
                                         interested-in 
                                         (reduce 
                                           #(when (= (first %1) %2) (rest %1))
                                         (or (:active-screen @db) [:listing])
                                         filters
                                         )]
                                     interested-in))))

(register-sub :logged-in (fn [db _] (reaction (:logged-in-user @db))))
(register-sub :forms (fn [db [_ form-id & args]] (reaction (-> @db :forms form-id))))


(defn change-screen [db path ]
  (let [path-components (rest path)
        resolved-path-components (vec (map (fn [p]
                                        (cond 
                                          (fn? p)
                                          (p db)
                                          (vector? p)
                                          (get-in db p)
                                          :else
                                          p)) path-components))]
  (assoc db :active-screen resolved-path-components)))

(register-handler :change-screen change-screen)

(def github-login-url (str "https://github.com/login/oauth/authorize?"
                           "scope=user:email&client_id=" (from-config [:github-auth :client-id])))


(defn root-template [] 
  (let [logged-in (subscribe [:logged-in])
        screen-atom (subscribe [:screen-changed])]
    (fn []
      [:div
       (if @logged-in
         (let [[active-screen & screen-args] @screen-atom]
           [:div 
            [:h1 "Webhookproxy"] 
            (case active-screen
              :whitelists
              [:div
               [whitelist-editor/root-component]
               ]
              :webhooks
              [:div
               [webhook-editor/root-component]
               ]
              [:div] 
              )
            ])
         [:div 
          [:h1 "Please Login"]
          [:a { :target "_new" :href github-login-url } "Auth to github"]]
         )])))

(defn ^:export rootrender [& args] 
  (reagent/render [root-template] (js/document.getElementById "app")))

(defn ^:export login [& args]
  (dispatch [:fetch-identity]))

(defn ^:export run
  []
  (forms/init)
  (dispatch-sync [:fetch-identity])
  (dispatch-sync [:fetch-webhooks])
  (println "COUNT: " (count (:items @re-frame.db/app-db)))
  (routing/dispatch! (-> js/window .-location .-pathname))
  (rootrender))

