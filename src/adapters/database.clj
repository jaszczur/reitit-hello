(ns adapters.database
  (:require
   [clojure.java.io :as io]
   [clojure.string :refer [split]]
   [honey.sql :as sql]
   [integrant.core :as ig]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as conn])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defmacro execute-one [ds query]
  `(jdbc/execute-one! ~ds (sql/format ~query) jdbc/snake-kebab-opts))

(defmacro execute [ds query]
  `(jdbc/execute! ~ds (sql/format ~query) jdbc/snake-kebab-opts))

(defn- slurp-statements [url]
  (-> url
      slurp
      (split #"\s*-- \*\*\*\n")))

(defn import-schema [ds url]
  (doseq [stmt (slurp-statements url)]
    (jdbc/execute-one! ds [stmt])))

(defmethod ig/init-key ::database [_ {:keys [db-spec
                                             schema-resource
                                             fixtures]}]
  (let [ds (conn/->pool HikariDataSource db-spec)]
    (import-schema ds (io/resource schema-resource))
    (doseq [stmt fixtures]
      (execute ds stmt))
    ds))

(defmethod ig/halt-key! ::database [_ datasource]
  (.close datasource))

(comment
  (def ds (::database integrant.repl.state/system))

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
