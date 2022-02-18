(ns server
  (:require
   [camel-snake-kebab.core :as csk]
   [integrant.core :as ig]
   [interceptors.common :refer [common-interceptors]]
   [muuntaja.core]
   [reitit.dev.pretty :as pretty]
   [reitit.coercion.malli]
   [reitit.http :as http]
   [reitit.interceptor.sieppari :as sieppari]
   [reitit.ring :as ring]
   [reitit.spec :as rs]
   [ring.adapter.jetty :as jetty]))


(def muuntaja
  (muuntaja.core/create
   (-> muuntaja.core/default-options
       (assoc-in
        [:formats "application/json" :encoder-opts]
        {:encode-key-fn csk/->camelCaseString})
       (assoc-in
        [:formats "application/json" :decoder-opts]
        {:decode-key-fn csk/->kebab-case-keyword}))))

(defmethod ig/init-key :server/router [_ {:keys [routes]}]
  (http/router
   routes
   {:exception pretty/exception
    :validate  rs/validate
    :data {:coercion reitit.coercion.malli/coercion
           :muuntaja muuntaja
           :interceptors  (common-interceptors)}}))

(defmethod ig/init-key :server/server [_ {:keys [router]}]
  (jetty/run-jetty
   (http/ring-handler router
                      (ring/create-default-handler)
                      {:executor sieppari/executor})
   {:port 3000
    :join? false
    :async true}))

(defmethod ig/halt-key! :server/server [_ server]
  (.stop server))
