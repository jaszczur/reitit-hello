(ns adapters.database
  (:require
   [integrant.core :as ig]))

(def dummy-db (atom
               {69 {:name "Pachinko"
                    :date "2021-01-06"
                    :last-modified "Mon, 18 Jul 2016 02:36:04 GMT"}}))

(defmethod ig/init-key ::database [_ _]
  dummy-db)
