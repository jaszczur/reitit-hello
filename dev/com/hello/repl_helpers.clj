(ns com.hello.repl-helpers
  (:require [hato.client :as hc]
            [integrant.repl.state]
            [migratus.core :as migratus]
            [next.jdbc :as jdbc]
            [com.hello.adapters.database :as database]))

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

(defmacro install-reveal-tap
  "It requires Reveal dependency on the classpath (is not incleded in the project).

  If you're using [https://github.com/practicalli/clojure-deps-edn], you can add :inspect/reveal
  to a list of aliases you're using in your editor/repl."
  []
  `(do
     (require '[vlaaad.reveal])
     (vlaaad.reveal/tap-log :always-on-top true
                            :close-difficulty :easy)))

(defmacro with-datasource [ds-sym & body]
  `(let [~ds-sym (-> integrant.repl.state/system
                     :com.hello.adapters.database/database
                     :datasource)]
     ~@body))

(defn execute-raw-query [^String query]
  (with-datasource ds
    (jdbc/execute! ds [query])))

(defn execute-query [query]
  (with-datasource ds
    (database/execute! ds query)))
