(ns webhookproxyweb.routing
  (:require [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType])
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

