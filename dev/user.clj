(ns user
  (:require
   [hato.client :as hc]
   [integrant.repl :refer [clear go halt prep init reset reset-all]]
   [io.aviso.repl]
   [io.aviso.exception]
   [io.aviso.logging]
   [migratus.core :as migratus]
   [com.hello.main.system :as system]))

(alter-var-root #'io.aviso.exception/*app-frame-names* (constantly [#"com\.hello.*"]))
(io.aviso.repl/install-pretty-exceptions)
(io.aviso.logging/install-pretty-logging)
(integrant.repl/set-prep! #(system/system-config {:profile :dev}))

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

(comment
  ;; System up/down
  (halt)
  (reset)

  ;; Create migration script
  (let [migratus-opts (-> integrant.repl.state/system
                          :com.hello.adapters/database
                          :migratus-opts)]
    (migratus/create migratus-opts "the-migration-description"))

  ;; Install reveal repl helper
  (do
    (require '[vlaaad.reveal :as reveal])
    (reveal/tap-log :always-on-top true
                    :close-difficulty :easy))

  ;; Do some manual JDBC stuff
  (do
    (require '[next.jdbc :as jdbc])
    (let [sys integrant.repl.state/system
          ds (-> sys :com.hello.adapters.database/database :datasource)]
      (jdbc/execute! ds ["SELECT * FROM migrations"])))


  ;; Test the API
  (fetch :get "/user/1/greet")
  (fetch :get "/user/2/greet" :accept :json :as :string)

  ,)
