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
            [webhookproxyweb.sync :as sync]
            [webhookproxyweb.screens :as screens]
            [webhookproxyweb.auth :as auth]
            [reagent.core :as reagent]
            [re-frame.db]
            [re-frame.core :refer [register-handler 
                                   register-sub 
                                   dispatch 
                                   dispatch-sync
                                   subscribe]]
            [webhookproxyweb.routing :as routing]))

(enable-console-print!)

(register-handler :fetch-webhooks sync/fetch-webhooks)
(register-handler :fetch-identity auth/fetch-identity)
(register-handler :reset-webhooks sync/reset-webhooks)
(register-handler :reset-identity auth/reset-identity)
(register-handler :start-auth-flow auth/start-auth-flow)


(defn root-template [] 
  (let [logged-in (subscribe [:logged-in])
        screen-atom (subscribe [:screen-changed])]
    (fn []
      [:div
       (if @logged-in
         (let [[active-screen & screen-args] (or @screen-atom :default)]
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
              :default 
              [:div]
              )
            ])
         [:div 
          [:h1 "Logging you in..."]]
         )])))

(defn ^:export rootrender [& args] 
  (reagent/render [root-template] (js/document.getElementById "app")))

(defn ^:export run []
  (dispatch [:fetch-identity])
  ; force the initial client side routing transitiion from page load
  (routing/force-transition! (-> js/window .-location .-pathname))
  (rootrender))

