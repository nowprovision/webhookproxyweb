(ns webhookproxyweb.components.layout
  (:require [clojure.string :refer [join]]
            [freeman.ospa.core :refer [dispatch
                                       resolve-route]]
            [reagent.core :refer [create-class]]))


(defn hide-drawer []
  ; this hacky - find a better way to handle material integration
  (-> 
    (.querySelector js/document ".mdl-layout__drawer")
    (.-classList)
    (.remove "is-visible"))
  (-> 
    (.querySelector js/document ".mdl-layout__obfuscator")
    (.-classList)
    (.remove "is-visible")))

(defn layout [content]
  (create-class
    {:reagent-render (fn [content]
                       [:div.mdl-layout.mdl-js-layout.mdl-layout--fixed-header
                        [:header.mdl-layout__header
                         [:div.mdl-layout__header-row
                          [:a.mdl-layout-title.ajax-link
                           {:alt "Listings"
                            :on-click #(do (dispatch [:redirect :list-webhooks]) false) }
                           [:img {:src "/img/webhooks.svg"
                                  :style {:width "50px"
                                          :height "50px" }} ]
                           "WebHookProxy"]
                          [:div.mdl-layout-spacer]
                          [:nav.mdl-navigation.mdl-layout--large-screen-only.right-actions
                           [:a.mdl-navigation__link.ajax-link {:style {:font-weight "bold" } :on-click #(do (dispatch [:redirect :add-webhook]) false) }
                            [:i.material-icons "add"]  
                            [:span "Add webhook"]]
                           [:a.mdl-navigation__link.ajax-link {:style {:font-weight "bold" } :on-click #(do (dispatch [:logout]) false) } 
                            [:i.material-icons "exit_to_app"]  
                            [:span "Logout"]]
                           ]]
                         ]
                        [:div.mdl-layout__drawer
                         [:span.mdl-layout-title "WebHookProxy"]
                         [:nav.mdl-navigation
                          [:a.mdl-navigation__link.ajax-link 
                           {:href "#" :on-click #(do 
                                                   (dispatch [:redirect :list-webhooks]) 
                                                   (hide-drawer)
                                                   false) }
                           "Edit Webhooks"]
                          [:a.mdl-navigation__link.ajax-link
                          {:on-click #(do 
                                        (dispatch [:redirect :add-webhook]) 
                                        (hide-drawer)
                                        false) }
                           "Add Webhook"]
                          [:a.mdl-navigation__link.ajax-link
                           {:on-click #(do (dispatch [:logout]) 
                                           (hide-drawer)
                                           false) }
                           "Logout"]]]
                        [:main.mdl-layout__content
                         [:div.page-content
                          content]]]) 
     :component-did-mount (fn [this] 
                            (let [chandler (goog.object.get js/window "componentHandler")]
                              ((goog.object.get chandler "upgradeElement") (.getDOMNode this)))
                            ) 
     }))

