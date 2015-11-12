(ns freeman.ospa.routing
  (:require [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [re-frame.core :refer [register-handler]])
  (:import goog.history.Html5History))

(defonce ^:export history (Html5History.))

; export on whp.routing.history for debugging
(doto history 
  (.setEnabled true)
  (.setPathPrefix "")
  (.setUseFragment false))

(defn on-popstate [e]
  (-> e .-token secretary/dispatch!))

(events/listen history EventType/NAVIGATE on-popstate)

(defn transition! [path]
  (.setToken history path))

(defn force-transition! [path]
  (if (= (.getToken history) path)
    (secretary/dispatch! path)
    (.setToken history path)))

(def route-map (atom {}))

(register-handler :redirect (fn [db [_ uri & args]]
                              (if (string? uri)
                                (transition! uri)
                                (let [route-fn (:fn (get @route-map uri))]
                                  (transition! (route-fn args))
                                  ))
                              db))
                              

(defn register-route [route-key route-path route-fn]
  (swap! route-map #(assoc % route-key { :path route-path
                                            :fn (fn [params] 
                                                  (secretary/render-route route-path params))
                                                  }))
  (secretary/add-route! route-path route-fn))

(defn resolve-route [route-url param-map]
  (secretary.core/render-route route-url (or param-map {})))
