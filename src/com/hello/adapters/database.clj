(ns com.hello.adapters.database
  (:require
   [clojure.java.io :as io]
   [clojure.string :refer [split]]
   [honey.sql :as sql]
   [integrant.core :as ig]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as conn]
   [migratus.core :as migratus])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defmacro execute-one [ds query]
  `(jdbc/execute-one! ~ds (sql/format ~query) jdbc/snake-kebab-opts))

(defmacro execute [ds query]
  `(jdbc/execute! ~ds (sql/format ~query) jdbc/snake-kebab-opts))

(defn- slurp-statements [url]
  (-> url
      slurp
      (split #"\s*-- \*\*\*\n")))


(defmethod ig/init-key ::database [_ {:keys [db-spec
                                             migrations
                                             fixtures]}]
  (let [ds (conn/->pool HikariDataSource db-spec)
        migratus-opts (assoc migrations :db {:datasource ds})]
    (migratus/migrate migratus-opts)
    (doseq [stmt fixtures]
      (execute ds stmt))
    {:datasource ds
     :migratus-opts migratus-opts}))

(defmethod ig/halt-key! ::database [_ {:keys [datasource]}]
  (.close datasource))

(comment
  (def ds (-> integrant.repl.state/system
              ::database
              :datasource))

  (jdbc/execute! ds ["
insert into user (name, last_modified) values
  ('Pachinko', 'Mon, 18 Jul 2016 02:36:04 GMT'),
  ('Henio', 'Tue, 18 Jan 2022 12:21:55 GMT');
"])

  (execute ds {:select :*
               :from :user})
  ;; => [#:user{:id 1, :name "Zenon", :last-modified "Mon, 18 Jul 2016 02:36:04 GMT"}
  ;;     #:user{:id 2, :name "Henio", :last-modified "Tue, 18 Jan 2022 12:21:55 GMT"}]



  (count
   (slurp-statements (io/resource "adapters/schema.sql")))
  ;; => 2




  ,)
