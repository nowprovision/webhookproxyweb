(ns webhookproxyweb.components.shared
  (:require [clojure.string :refer [join]]
            [freeman.ospa.core :refer [dispatch]]
            [reagent.core :refer [create-class]]
            [reagent-forms.core :as reagent-forms]))

(defn form-input [label input-attrs]
  [:tr
   [:td
   [:label label]]
   [:td.mdl-textfield
    [:input.form-control.mdl-textfield__input.mdl-textfield__large
     input-attrs]]])

(defn select-input [label input-attrs & children]
  [:tr
   [:td
   [:label label]]
   [:td.mdl-textfield
    (concat [:select.form-control.mdl-textfield__input.mdl-textfield__large
     input-attrs] children)]])

(defn mask-loading [loaded-atom component-fn]
  (if @loaded-atom
    (component-fn)
    [:div "Loading"]))

(def bind-fields reagent-forms/bind-fields)

(defn append-classes [el-attrs classes]
  (update el-attrs :class (fn [v]
                            (let [existing (if (nil? v) [] [v])]
                              (join " " (concat existing (map name classes)))))))

(defn button 
  ([el-attrs el-body]
   (button el-attrs el-body []))
  ([el-attrs el-body extra-classes]
   (let [default-classes (concat [:mdl-button
                                  :mdl-js-button
                                  :mdl-button--raised
                                  :mdl-js-ripple-effect
                                  :mdl-button--accent] extra-classes)]
     (create-class 
       { :reagent-render (fn [el-attrs el-body]
                           (let [el-attrs (append-classes el-attrs default-classes)]
                             (println (clj->js (:class el-attrs)))
                             [:button el-attrs el-body])) 
        :component-did-mount (fn [this] 
                               (let [chandler (goog.object.get js/window "componentHandler")]
                                 ((goog.object.get chandler "upgradeElement") (.getDOMNode this)))) }))))

(defn action-button  [el-attrs el-body]
  [button el-attrs el-body [:mdl-button--rspace :mdl-button--bspace]])

(defn table [headers items actions desc add-action]
  [:table.mdl-data-table.mdl-js-data-table.mdl-data-table--selectable.mdl-shadow--2dp
   [:thead
    (for [header headers]
      ^{:key (str "thead-" (:title header)) } [:th (:title header)])
      [:th]]
   [:tbody
    (if (empty? items)
      [:td { :col-span (+ (count headers) 1)} 
       [:div.empty-warning
        [:small (str "There are currently no " desc " defined. ") ]
        [:a.ajax-link {:alt "add new"
                       :on-click #(dispatch add-action) } [:i.material-icons "add" ] ]
        ]]
      (for [item items]
        ^{:key (:id item) } [:tr
                             (for [header headers :let [data (get item (:key header))]]
                              ^{:key (str (:id item) "-" (:key header)) } [:td data])
                             (actions item)
                             ])) ]])




  
