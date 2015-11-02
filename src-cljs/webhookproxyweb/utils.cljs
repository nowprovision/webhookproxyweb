(ns webhookproxyweb.utils
  (:require [cljs-uuid-utils.core :as uuid]))

(defn uuid-str []
  (-> (uuid/make-random-uuid)
      str))

(defn uuid-keyword []
  (-> (uuid/make-random-uuid)
      str
      keyword))


