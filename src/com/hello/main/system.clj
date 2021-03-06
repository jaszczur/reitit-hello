;; Copyright © 2016, JUXT LTD.
(ns com.hello.main.system
  "Components and their dependency relationships"
  (:require
   [aero.core :as aero]
   [clojure.java.io :as io]
   [integrant.core :as ig]))

;; There will be integrant tags in our Aero configuration. We need to
;; let Aero know about them using this defmethod.
(defmethod aero/reader 'ig/ref [_ _ value]
  (ig/ref value))

(let [lock (Object.)]
  (defn- load-namespaces
    [system-config]
    (locking lock
      (ig/load-namespaces system-config))))

(defn read-config
  "Read EDN config, with the given aero options. See Aero docs at
  https://github.com/juxt/aero for details."
  [opts]
  (-> (io/resource "config.edn")
      (aero/read-config opts)))

(defn system-config
  "Construct a new system, configured with the given profile"
  [opts]
  (let [system-config (read-config opts)]
    (load-namespaces system-config)
    (ig/prep system-config)))
