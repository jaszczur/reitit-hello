(ns utils.http
  (:require [ring.util.response :as rur]))

;;;; Request helpers
;;;;

(defn from-request [ks]
  (fn [ctx] (get-in ctx (vec (concat [:request] ks)))))

(defn from-path [param]
  (fn [ctx] (get-in ctx [:request :parameters :path param])))

;;;; Response helpers
;;;;

(defmacro not-found
  ([message]
   `(rur/not-found {:error {:type "not-found" :message ~message}}))
  ([]
   `(not-found  "resource not found")))

(defmacro response [body]
  `(rur/response ~body))


(defn maybe-response [body]
  (if body
    (response body)
    (not-found)))

(defmacro status
  ([status]
   `(rur/status ~status))
  ([res status]
   `(rur/status ~res ~status)))

(defmacro created
  ([url body]
   `(rur/created ~url ~body))
  ([body]
   `{:status  201
     :body    ~body}))

(defn accepted
  ([body]
   (-> (rur/response body)
       (rur/status 202)))
  ([]
   (accepted nil)))

(defn unauthorized [& reason]
  {:status  403
   :headers {}
   :body    {:error {:type "permission-denied" :message (apply str reason)}}})
