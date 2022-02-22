(ns com.hello.interceptors.database
  (:require [sieppari.context :refer [terminate]]
            [com.hello.adapters.database :as jdbc]))

(defn inject-db [database]
  {:name ::inject-db
   :enter (fn [ctx]
            (assoc ctx :db (:datasource database)))})

(defn query-one
  "Queries database for "
  ([query-fn]
   (query-one :entity query-fn))
  ([k query-fn]
   {:enter (fn [ctx]
             (let [query  (query-fn ctx)
                   entity (and query (jdbc/execute-one (:db ctx) query))]
               (if entity
                 (-> ctx
                     (assoc-in [:request :query-results k] entity)
                     (assoc-in [:query-meta k :last-modified] (:last-modified entity)))
                 ctx)))
    :leave (fn [ctx]
             (if-let [last-modified (get-in ctx [:query-meta k :last-modified])]
               (assoc-in ctx [:response :headers "Last-Modified"] last-modified)
               ctx))}))

(defn ensure-found
  ([]
   (ensure-found :entity))
  ([k]
   {:enter (fn [ctx]
             (if (get-in ctx [:request :query-results k])
               ctx
               (terminate ctx {:status 404
                               :body {:error true
                                      :message "Entity not found"}})))}))

(defn query-result
  ([req] (query-result req :entity))
  ([req k] (get-in req [:query-results k])))
