(ns user
  (:require
   [hato.client :as hc]
   [integrant.repl :refer [clear go halt prep init reset reset-all]]
   [io.aviso.repl]
   [io.aviso.exception]
   [main.system :as system]
   [vlaaad.reveal :as reveal]))

(io.aviso.repl/install-pretty-exceptions)
(integrant.repl/set-prep! #(system/system-config {:profile :dev}))

(defmacro fetch [method url & opts]
  (let [full-url (str "http://localhost:3000" url)
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

(defonce personal-settigns-applied (atom nil))
  (swap! personal-settigns-applied
         (fn [already-ran]
           (when-not already-ran
             (reveal/tap-log :always-on-top true
                             :close-difficulty :easy))
           true))

(comment
  (require '[next.jdbc :as jdbc])

  (let [sys integrant.repl.state/system
        ds (:adapters.database/database sys)]
    (jdbc/execute! ds ["SELECT * FROM User"]))


  (fetch :get "/user/1/greet")
  (fetch :get "/user/2/greet" :accept :json :as :string)

  ,)
