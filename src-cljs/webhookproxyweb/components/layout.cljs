(ns webhookproxyweb.components.layout
  (:require [clojure.string :refer [join]]
            [freeman.ospa.core :refer [dispatch
                                       resolve-route]]
            [reagent.core :refer [create-class]]))

(defn layout [content]
  (create-class
    {:reagent-render (fn [content]
                       [:div.mdl-layout.mdl-js-layout.mdl-layout--fixed-header
                        [:header.mdl-layout__header
                         [:div.mdl-layout__header-row
                          [:a.mdl-layout-title.ajax-link
                           {:alt "Listings"
                            :on-click #(do (dispatch [:redirect :list-webhooks]) false) }
                           "WebHookProxy"]
                          [:div.mdl-layout-spacer]
                          [:nav.mdl-navigation.mdl-layout--large-screen-only
                           [:a.mdl-navigation__link.ajax-link {:on-click #(do (dispatch [:redirect :add-webhook]) false) }
                            "Add Webhook"]
                           [:a.mdl-navigation__link.ajax-link {:on-click #(do (dispatch [:logout]) false) } "Logout"]
                           ]]
                         ]
                        [:div.mdl-layout__drawer
                         [:span.mdl-layout-title "Title"]
                         [:nav.mdl-navigation
                          [:a.mdl-navigation__link "Link"]
                          [:a.mdl-navigation__link "Link"]
                          [:a.mdl-navigation__link "Link"]
                          [:a.mdl-navigation__link "Link"]]]
                        [:main.mdl-layout__content
                         [:div.page-content
                          content]]]) 
     :component-did-mount (fn [this] 
                            (let [chandler (goog.object.get js/window "componentHandler")]
                              ((goog.object.get chandler "upgradeElement") (.getDOMNode this)))
                            ) 
     }))

