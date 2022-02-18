(ns interceptors.common
  (:require
   [reitit.http.coercion :as coercion]
   [reitit.http.interceptors.parameters :as parameters]
   [reitit.http.interceptors.muuntaja :as muuntaja]
   [reitit.http.interceptors.exception :as exception]
   [reitit.http.interceptors.multipart :as multipart]))

(defn common-interceptors []
  [ ;; query-params & form-params
   (parameters/parameters-interceptor)
   ;; content-negotiation
   (muuntaja/format-negotiate-interceptor)
   ;; encoding response body
   (muuntaja/format-response-interceptor)
   ;; exception handling
   (exception/exception-interceptor
    (merge exception/default-handlers
           {::exception/wrap (fn [handler e request]
                              (.printStackTrace e)
                              (handler e request))}))
   ;; decoding request body
   (muuntaja/format-request-interceptor)
   ;; coercing response bodys
   (coercion/coerce-response-interceptor)
   ;; coercing request parameters
   (coercion/coerce-request-interceptor)
   ;; multipart
   (multipart/multipart-interceptor)])
