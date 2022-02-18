(ns database
  (:require [sieppari.context :refer [terminate]]))

(def db
  {69 {:name "Pachinko"
       :date "2021-01-06"
       :last-modified "Mon, 18 Jul 2016 02:36:04 GMT"}})


(defn select-by-id
  ([query-fn]
   (select-by-id :entity query-fn))
  ([k query-fn]
   {:enter (fn [ctx]
             (if-let [entity (get db (query-fn ctx))]
               (-> ctx
                   (assoc-in [:request :query-results k] entity)
                   (assoc-in [:query-meta k :last-modified] (:last-modified entity)))
               ctx))
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
