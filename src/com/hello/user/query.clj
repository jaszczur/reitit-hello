(ns com.hello.user.query)

(defn select-user-by-id [id]
  {:select :*
   :from :user
   :where [:= :user/id id]})
