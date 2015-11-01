(ns webhookproxyweb.screens
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub register-handler]]))

(register-sub :screen-changed (fn [db [_ & filters]]
                                  (reaction 
                                    (let [filter-len (count filters)
                                          active-screen (:active-screen @db)]
                                      (when (= (seq (take filter-len active-screen)) filters)
                                        (drop filter-len active-screen))))))

(defn change-screen [db path ]
  (let [path-components (rest path)]
    (assoc db :active-screen path-components)))

(register-handler :change-screen change-screen)

