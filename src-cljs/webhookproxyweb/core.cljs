(ns webhookproxyweb.core
  (:require [freeman.ospa.core 
             :refer [register-sub 
                     register-handler
                     render
                     subscribe 
                     dispatch 
                     force-transition!
                     resolve-route] 
             :refer-macros [reaction]]
            [webhookproxyweb.sync :as sync]
            [webhookproxyweb.auth :as auth]
            [webhookproxyweb.screens :as screens]
            [webhookproxyweb.components.webhook.view :as webhook-view]
            [webhookproxyweb.components.filters.view :as filter-view]))

(enable-console-print!)

(register-handler :fetch-webhooks sync/fetch-webhooks)
(register-handler :fetch-identity auth/fetch-identity)
(register-handler :reset-webhooks sync/reset-webhooks)
(register-handler :reset-identity auth/reset-identity)
(register-handler :start-auth-flow auth/start-auth-flow)


(defn root-template [] 
  (let [logged-in (subscribe [:logged-in])
        login-started (subscribe [:login-started])
        screen-atom (subscribe [:screen-changed])]
    (fn []
      [:div
       (if @logged-in
         (let [[active-screen & screen-args] (or @screen-atom :default)]
           [:div 
            [:h1 "Webhookproxy Control Panel"]
            [:div
             [:span "Logged in as " (:email @logged-in) " "]
            [:a {:on-click #(dispatch [:logout]) } "Logout" ]]
            [:br]
            (case active-screen
              :filters
              [:div
               [filter-view/root]
               ]
              :webhooks
              [:div
               [webhook-view/root]
               ]
              [:div] 
              :default 
              [:div]
              )
            ])
         [:div 
          (if @login-started
            [:h1 "Logging you in..." @login-started]
            [:div
             [:button.btn { :on-click #(dispatch [:start-auth-flow]) } "Login again"]]
            )])])))

(defn ^:export root-render [& args] 
  (render [root-template] (js/document.getElementById "app")))

(defn ^:export run []
  (dispatch [:fetch-identity])
  ; force the initial client side routing transitiion from page load
  (force-transition! (-> js/window .-location .-pathname))
  (root-render))

