(ns com.hello.user.core-test
  (:require [com.hello.user.core :as sut]
            [clojure.test :as t]))

(t/deftest greeting
  (t/is (= "Hello, Henio!"
           (sut/greet {:name "Henio"}))))
