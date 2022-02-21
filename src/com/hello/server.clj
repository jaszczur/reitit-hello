(ns com.hello.server
  (:require
   [clojure.tools.logging :as log]
   [camel-snake-kebab.core :as csk]
   [integrant.core :as ig]
   [com.hello.interceptors.common :refer [common-interceptors]]
   [com.hello.interceptors.database :as db]
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

(defmethod ig/init-key ::router [_ {:keys [routes database]}]
  (http/router
   routes
   {:exception pretty/exception
    :validate  rs/validate
    :data {:coercion reitit.coercion.malli/coercion
           :muuntaja muuntaja
           :interceptors  (->  (common-interceptors)
                               (conj (db/inject-db database)))}}))

(defmethod ig/init-key ::server [_ {:keys [server-opts router]}]
  (let [srv
        (jetty/run-jetty
         (http/ring-handler router
                            (ring/create-default-handler)
                            {:executor sieppari/executor})
         server-opts)]
    (log/info "Server listening at port" (:port server-opts))
    srv))

(defmethod ig/halt-key! ::server [_ server]
  (.stop server))
