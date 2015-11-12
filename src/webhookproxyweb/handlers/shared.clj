(ns webhookproxyweb.handlers.shared)

(defn with-no-cache [headers]
  (merge {"Cache-Control" "no-cache, no-store, must-revalidate"
          "Pragma" "no-cache"
          "Expires" "0" } headers))

(defn with-static [headers]
  (merge { "Content-Type" "text/html" } headers))

(def error403 {:headers (with-no-cache {"Content-Type" "text/html"})
               :body "<html><body><h1>403. Not authorized</h1></body></html>" 
               :status 403 })

(defmacro with-security [roles & routes]
  ;; wrap handler with check of authenticated and session roles matches
  (let [wrapped-routes (map (fn [r] 
                              (concat (butlast r)
                                      [`(if (or (some #{:open} ~roles) ; public/open role
                                                (and (-> ~'req :session :authenticated?) 
                                                     (some (set ~roles) (-> ~'req :session :roles))))
                                          ~(last r)
                                          error403
                                          )])) routes)]
    `(list ~@wrapped-routes)))

