(ns cognitect.test-runner.api
  (:refer-clojure :exclude [test])
  (:require
    [cognitect.test-runner :as tr]))

(defn- do-test
  [{:keys [dirs nses patterns vars includes excludes]}]
  (let [adapted {:dir (when (seq dirs) (set dirs))
                 :namespace (when (seq nses) (set nses))
                 :namespace-regex (when (seq patterns) (map re-pattern patterns))
                 :var (when (seq vars) (set vars))
                 :include (when (seq includes) (set includes))
                 :exclude (when (seq excludes) (set excludes))}]
    (tr/test adapted)))

(defn test
  "Invoke the test-runner with the following options:

  * :dirs - coll of directories containing tests, default= [\"test\"]
  * :nses - coll of namespace symbols to test
  * :patterns - coll of regex strings to match namespaces, default= [\".*-test$\"]
  * :vars - coll of fully qualified symbols to run tests on
  * :includes - coll of test metadata keywords to include
  * :excludes - coll of test metadata keywords to exclude"
  [opts]
  (try
    (let [{:keys [fail error]}
          (do-test opts)]
      (System/exit (if (zero? (+ fail error)) 0 1)))
    (finally
      (shutdown-agents))))
