(ns webhookproxyweb.routing
  (:require [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
  (:import goog.history.Html5History))

(defonce history (Html5History.))

(defn on-popstate [e]
  (-> e .-token secretary/dispatch!))

(doto history 
  (.setEnabled true)
  (.setPathPrefix "")
  (.setUseFragment false))

(events/listen history EventType/NAVIGATE on-popstate)

(defn transition! [path]
  (println (str "Trans navigating to:" path))
  (.setToken history path))

(defn dispatch! [path]
  (println (str "Dispatch navigating to:" path))
  (secretary/dispatch! path))




