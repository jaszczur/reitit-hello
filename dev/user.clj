(ns user
  (:require
   [com.hello.main.system :as system]
   [com.hello.repl-helpers :as hr]
   [integrant.repl :refer [halt reset]]
   [io.aviso.repl]
   [io.aviso.exception]
   [io.aviso.logging]))

(alter-var-root #'io.aviso.exception/*app-frame-names* (constantly [#"com\.hello.*"]))
(io.aviso.repl/install-pretty-exceptions)
(io.aviso.logging/install-pretty-logging)
(integrant.repl/set-prep! #(system/system-config {:profile :dev}))


(comment
  ;; System
  (do (halt)                            ; restart
      (reset))
  (halt)                                ; stop
  (reset)                               ; refresh

  ;; Create migration script
  (hr/create-migration-script "fancy description")

  ;; Install reveal repl helper
  (hr/install-reveal-tap)

  ;; Do some manual JDBC stuff
  (hr/execute-jdbc-raw "SELECT * FROM migrations")

  ;; Test the API
  (hr/fetch :get "/user/1/greet")
  (hr/fetch :get "/user/2/greet" :accept :json :as :string)

  ,)
