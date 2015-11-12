(ns webhookproxyweb.screens
  (:require [freeman.ospa.core 
             :refer [register-sub 
                     register-handler
                     subscribe 
                     dispatch 
                     resolve-route] 
             :refer-macros [reaction]]))

(register-sub :active-screen-changed (fn [db _] (reaction (:active-screen @db))))

(defn screen-changed 
  "when filters is [:webhooks] - e.g. (subscribe [:screen-changed :webhooks])
  and active-screen is a vector of full path [:webhooks :bar :foo]
  return a new reaction producing only [:bar :foo]"
  [db [_ & filters]]
  (reaction 
    (let [active-screen (subscribe [:active-screen-changed])
          filter-len (count filters)]
      (when (= (seq (take filter-len @active-screen)) filters)
        (drop filter-len @active-screen)))))

(register-sub :screen-changed screen-changed)

(defn change-screen [db [_ & path]]
  (assoc db :active-screen path))

(register-handler :change-screen change-screen)

