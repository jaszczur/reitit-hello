(ns server
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.content-negotiation :as conneg]
            [io.pedestal.http.route.definition.table :as table]
            [interceptors.coertion :as ico]
            [integrant.core :as ig]))

(defn respond-hello [request]
  {:status 200 :body "Hello, world!"})

(defn respond-echo [request]
  {:status 200 :body {:message "wazzup"
                      :error false}})

(def common-interceptors [(conneg/negotiate-content ico/supported-content-types) ico/coerce-body])

(defn attach-common-interceptors [route]
  (let [chain (nth route 2)
        chain (if (seqable? chain)
                (into []
                      (concat common-interceptors chain))
                (conj common-interceptors chain))]
    (assoc route 2 chain)))

(def routes
  (->>
   #{["/greet" :get respond-hello :route-name :greet]
     ["/echo" :get  respond-echo :route-name :echo]}
   (map attach-common-interceptors)
   (into #{})))

(def service-map
  {::http/routes routes
   ::http/type   :jetty
   ::http/port   8890
   ::http/join?  false})

(defmethod ig/init-key :server/server [_ _]
  (http/start (http/create-server service-map)))

(defmethod ig/halt-key! :server/server [_ service-map]
  (http/stop service-map))
