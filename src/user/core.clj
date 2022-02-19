(ns user.core)

;; Core user greeting logic
;;

(defn greet [user]
  (str "Hello, " (:name user) "!"))
