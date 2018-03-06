(ns cognitect.test-runner-test
  (:require [clojure.test :refer [deftest is testing]]
            [cognitect.test-runner :as sut]))

(deftest ns-filter-test
  (testing "specific namespaces only"
    (is (= ['foo]
         (filter (#'sut/ns-filter {:namespace #{'foo}})
                 ['foo 'foo-test 'foo-it-test]))))
  (testing "regex namespaces only"
    (is (= ['foo-it-test]
           (filter (#'sut/ns-filter {:namespace-regex #{#"foo-it-test"}})
                   ['foo 'foo-test 'foo-it-test]))))
  (testing "specific and regex namespaces"
    (is (= ['foo 'foo-it-test]
           (filter (#'sut/ns-filter {:namespace #{'foo}
                                     :namespace-regex #{#".*-it-test$"}})
                   ['foo 'foo-test 'foo-it-test])))))
