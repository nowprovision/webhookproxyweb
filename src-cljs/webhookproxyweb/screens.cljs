(ns webhookproxyweb.screens
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.db :refer [app-db]]
            [re-frame.core :refer [register-sub register-handler]]))

(def active-screen (reaction (-> app-db deref :active-screen)))

(defn screen-changed 
  "when filters is [:webhooks] - e.g. (subscribe [:screen-changed :webhooks])
  and active-screen is a vector of full path [:webhooks :bar :foo]
  return a new reaction producing only [:bar :foo]"
  [db [_ & filters]]
  (reaction 
    (let [filter-len (count filters)]
      (when (= (seq (take filter-len @active-screen)) filters)
        (drop filter-len @active-screen)))))

(register-sub :screen-changed screen-changed)

(defn change-screen [db [_ & path]]
  (assoc db :active-screen path))

(register-handler :change-screen change-screen)

