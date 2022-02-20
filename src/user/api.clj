(ns user.api
  (:require [integrant.core :as ig]
            [utils.http :as http]
            [user.core :as user]
            [user.query :as q]
            [interceptors.database :as db]))

(defn hello-handler [req]
  (http/response
   (let [person (db/query-result req)]
     {:message (user/greet person)
      :error false})))

(defmethod ig/init-key ::routes [_ _]
  ["/user"
   ["/:id/greet" {:parameters {:path {:id [int?]}}
                  :interceptors [(db/query-one :entity
                                               (comp q/select-user-by-id
                                                     (http/from-path :id)))
                                 (db/ensure-found :entity)]
                  :get hello-handler
                  :head (constantly nil)}]])
