(ns user.api
  (:require [integrant.core :as ig]
            [utils.http :as http]
            [user.core :as user]
            [interceptors.database :as db]))

(defn hello-handler [req]
  (http/response
   (let [person (db/query-result req)]
     {:message (user/greet person)
      :error false})))

(defmethod ig/init-key ::routes [_ _]
  ["/hello"
   ["/greet/:id" {:parameters {:path {:id [int?]}}
                  :interceptors [(db/select-by-id :entity (http/from-path :id))
                                 (db/ensure-found :entity)]
                  :get hello-handler
                  :head (constantly nil)}]])
