(ns com.hello.user.core)

;; Core user greeting logic
;;

(defn greet [user]
  (str "Hello, " (:user/name user) "!"))
