(ns interceptors.coertion
  (:require [clojure.data.json :as json]))

(def supported-content-types ["application/json" "application/edn"])

(def coerce-body
  {:name ::coerce-body
   :leave
   (fn [context]
     (let [accepted         (get-in context [:request :accept :field] "application/json")
           response         (get context :response)
           body             (get response :body)
           coerced-body     (case accepted
                              "application/edn"  (pr-str body)
                              "application/json" (json/write-str body))
           updated-response (assoc response
                                   :headers {"Content-Type" accepted}
                                   :body    coerced-body)]
       (assoc context :response updated-response)))})
