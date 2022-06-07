(ns acme.core-test
  (:require [clojure.test :as t :refer [deftest is]]))

(def expected (atom {}))

(deftest foo-test
  (swap! expected assoc :foo-test true)
  (is true))

(deftest bar-test
  (swap! expected assoc :bar-test true)
  (is true))
