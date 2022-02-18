(ns user
  (:require
   [edge.system :as system]
   [integrant.repl :refer [clear go halt prep init reset reset-all]]
   [io.aviso.repl]
   [io.aviso.exception]
   [vlaaad.reveal :as reveal]))

(comment
  (reset)
  )

(io.aviso.repl/install-pretty-exceptions)
(integrant.repl/set-prep! #(system/system-config {:profile :dev}))


(defonce personal-settigns-applied (atom nil))
  (swap! personal-settigns-applied
         (fn [already-ran]
           (when-not already-ran
             (reveal/tap-log :always-on-top true
                             :close-difficulty :easy))
           true))

(comment
  (require '[reitit.core :as r])

  (let [sys integrant.repl.state/system
        router (:server/router sys)]
    router)

  )
