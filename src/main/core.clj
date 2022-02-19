(ns main.core
  (:require
   [main.system :as sys]
   [integrant.core :as ig])
  (:gen-class))

(def system nil)

(defn -main [& args]
  (let [profile (keyword (or (System/getenv "PROFILE") "dev"))
        system-config (sys/system-config {:profile profile})
        sys           (ig/init system-config)]
    (.addShutdownHook
     (Runtime/getRuntime)
     (Thread.
      (fn []
        (ig/halt! sys))))
    (alter-var-root #'system (constantly sys)))
  @(promise))
