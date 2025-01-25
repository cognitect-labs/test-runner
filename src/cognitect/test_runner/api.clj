(ns cognitect.test-runner.api
  (:refer-clojure :exclude [test])
  (:require
    [cognitect.test-runner :as tr]))

(defn- do-test
  [{:keys [dirs nses patterns vars includes excludes test-runner-fn outputs]}]
  (let [adapted {:dir (when (seq dirs) (set dirs))
                 :namespace (when (seq nses) (set nses))
                 :namespace-regex (when (seq patterns) (map re-pattern patterns))
                 :var (when (seq vars) (set vars))
                 :include (when (seq includes) (set includes))
                 :exclude (when (seq excludes) (set excludes))
                 :test-runner-fn test-runner-fn
                 :output (when (seq outputs) (vec outputs))}]
    (tr/test adapted)))

(defn test
  "Invoke the test-runner with the following options:

  * :dirs - coll of directories containing tests, default= [\"test\"]
  * :nses - coll of namespace symbols to test
  * :patterns - coll of regex strings to match namespaces
  * :vars - coll of fully qualified symbols to run tests on
  * :includes - coll of test metadata keywords to include
  * :excludes - coll of test metadata keywords to exclude
  * :test-runner-fn - symbol identifying test runner creation function
  * :outputs - coll of output formats (symbols) to use

  If neither :nses nor :patterns is supplied, use `:patterns [\".*-test$\"]`.

  Not all output formats produce a fail/error summary."
  [opts]
  (let [{:keys [fail error] :or {fail 0, error 0}} (do-test opts)]
    (when (> (+ fail error) 0)
      (throw (ex-info "Test failures or errors occurred." {})))))
