(ns freeman.ospa.core
  (:require [reagent.core]
            [re-frame.core]
            [freeman.ospa.routing :as routing]))

(def ratom reagent.core/atom)

(def dispatch re-frame.core/dispatch)

(def subscribe re-frame.core/subscribe)

(def register-handler re-frame.core/register-handler)

(def register-sub re-frame.core/register-sub)

(def register-route routing/register-route)

(def resolve-route routing/resolve-route)

(def render reagent.core/render)

(def force-transition! routing/force-transition!)



