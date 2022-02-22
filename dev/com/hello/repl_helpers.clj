(ns com.hello.repl-helpers
  (:require [hato.client :as hc]
            [integrant.repl.state]
            [migratus.core :as migratus]
            [next.jdbc :as jdbc]))

(defmacro fetch [method url & opts]
  (let [full-url (str "http://localhost:3030" url)
        full-opts {:method method
                   :url full-url
                   :throw-exceptions? false
                   :as :auto
                   :accept :edn}
        full-opts (if (empty? opts)
                    full-opts
                    (apply assoc full-opts opts))]
    `(-> ~full-opts
         hc/request
         (select-keys [:status :headers :body]))))

(defn create-migration-script
  "Creates new migration script with given description"
  [name]
  (let [migratus-opts (-> integrant.repl.state/system
                          :com.hello.adapters/database
                          :migratus-opts)]
    (migratus/create migratus-opts name)))

(defmacro install-reveal-tap []
  `(do
     (require '[vlaaad.reveal :as reveal])
     (reveal/tap-log :always-on-top true
                     :close-difficulty :easy)))

(defn execute-jdbc-raw [^String query]
  (let [sys integrant.repl.state/system
        ds (-> sys :com.hello.adapters.database/database :datasource)]
    (jdbc/execute! ds [query])))
