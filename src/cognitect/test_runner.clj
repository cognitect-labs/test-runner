(ns cognitect.test-runner
  (:require [cognitect.test-runner.protocols :as p]
            [clojure.tools.namespace.find :as find]
            [clojure.java.io :as io]
            [clojure.test :as test]
            [clojure.test.junit :as junit]
            [clojure.test.tap :as tap]
            [clojure.tools.cli :as cli])
  (:refer-clojure :exclude [test]))

(defn- ns-filter
  [{:keys [namespace namespace-regex]}]
  (let [[include-ns include-regexes]
        (if (or (seq namespace) (seq namespace-regex))
          [namespace namespace-regex]
          [nil [#".*\-test$"]])]
    (fn [ns]
      (or
        (get include-ns ns)
        (some #(re-matches % (name ns)) include-regexes)))))

(defn- var-filter
  [{:keys [var include exclude]}]
  (let [test-specific (if var
                        (set (map #(or (resolve %)
                                       (throw (ex-info (str "Could not resolve var: " %)
                                                       {:symbol %})))
                                  var))
                        (constantly true))
        test-inclusion (if include
                         #((apply some-fn include) (meta %))
                        (constantly true))
        test-exclusion (if exclude
                         #((complement (apply some-fn exclude)) (meta %))
                         (constantly true))]
    #(and (test-specific %)
          (test-inclusion %)
          (test-exclusion %))))

(defn- filter-vars!
  [nses filter-fn]
  (doseq [ns nses]
    (doseq [[_name var] (ns-publics ns)]
      (when (:test (meta var))
        (when (not (filter-fn var))
          (alter-meta! var #(-> %
                                (assoc ::test (:test %))
                                (dissoc :test))))))))

(defn- restore-vars!
  [nses]
  (doseq [ns nses]
    (doseq [[_name var] (ns-publics ns)]
      (when (::test (meta var))
        (alter-meta! var #(-> %
                              (assoc :test (::test %))
                              (dissoc ::test)))))))

(defn- ns-contains-tests?
  "Check if a namespace contains some tests to be executed."
  [ns]
  (some (comp :test meta)
        (-> ns ns-publics vals)))

(defrecord ClojureTestRunner []
  p/TestRunner
  (enable-filtering! [_ options nses]
    (filter-vars! nses (var-filter options)))
  (contains-tests? [_ _ ns] (ns-contains-tests? ns))
  (run-tests [this options nses]
    ;; we only care about junit and tap (other test runners might care about
    ;; other values)
    (if-let [outputs (seq (filter #{'junit 'tap} (:output options)))]
      (doseq [output outputs]
        (case output
          junit
          (junit/with-junit-output
            (apply test/run-tests (filter #(p/contains-tests? this options %) nses)))
          tap
          (tap/with-tap-output
            (apply test/run-tests (filter #(p/contains-tests? this options %) nses)))))
      (apply test/run-tests (filter #(p/contains-tests? this options %) nses))))
  (disable-filtering! [_ _ nses]
    (restore-vars! nses)))

(defn create-clojure-test-runner []
  (->ClojureTestRunner))

(defn test
  [options]
  (let [dirs (or (:dir options)
                 #{"test"})
        nses (->> dirs
                  (map io/file)
                  (mapcat find/find-namespaces-in-dir))
        nses (filter (ns-filter options) nses)
        tsym (or (:test-runner-fn options)
                 'cognitect.test-runner/create-clojure-test-runner)
        t-fn (try
               (require (symbol (namespace tsym)))
               (resolve tsym)
               (catch Throwable t
                 (println "Unable to find test runner function:" tsym)
                 (throw t)))
        ;; create the test runner:
        trun (t-fn)]
    (println (format "\nRunning tests in %s" dirs))
    (dorun (map require nses))
    (try
      (p/enable-filtering! trun options nses)
      (p/run-tests trun options nses)
      (finally
        (p/disable-filtering! trun options nses)))))

(defn- parse-kw
  [^String s]
  (if (.startsWith s ":") (read-string s) (keyword s)))


(defn- accumulate [m k v]
  (update-in m [k] (fnil conj #{}) v))

(def cli-options
  [["-d" "--dir DIRNAME" "Name of the directory containing tests. Defaults to \"test\"."
    :parse-fn str
    :assoc-fn accumulate]
   ["-n" "--namespace SYMBOL" "Symbol indicating a specific namespace to test."
    :parse-fn symbol
    :assoc-fn accumulate]
   ["-r" "--namespace-regex REGEX" "Regex for namespaces to test."
    :parse-fn re-pattern
    :assoc-fn accumulate]
   ["-v" "--var SYMBOL" "Symbol indicating the fully qualified name of a specific test."
    :parse-fn symbol
    :assoc-fn accumulate]
   ["-i" "--include KEYWORD" "Run only tests that have this metadata keyword."
    :parse-fn parse-kw
    :assoc-fn accumulate]
   ["-e" "--exclude KEYWORD" "Exclude tests with this metadata keyword."
    :parse-fn parse-kw
    :assoc-fn accumulate]
   ["-t" "--test-runner-fn SYMBOL" "Symbol indicating the test runner function to use."
    :parse-fn symbol]
   [nil "--output SYMBOL" "Output format, specific to the selected test runner."
    :parse-fn symbol
    :assoc-fn (fn [m k v] (update m k (fnil conj []) v))]
   ["-H" "--test-help" "Display this help message"]])

(defn- help
  [args]
  (println "\nUSAGE:\n")
  (println "clj -m" (namespace `help) "<options>\n")
  (println (:summary args))
  (println "\nAll options may be repeated multiple times for a logical OR effect.")
  (println "If neither -n nor -r is supplied, use -r #\".*-test$\" (ns'es ending in '-test')"))

(defn -main
  "Entry point for the test runner"
  [& args]
  (let [args (cli/parse-opts args cli-options)]
    (if (:errors args)
      (do (doseq [e (:errors args)]
            (println e))
          (help args)
          (System/exit 1))
      (if (-> args :options :test-help)
        (help args)
        (try
          (let [{:keys [fail error]} (test (:options args))]
            (System/exit (if (zero? (+ fail error)) 0 1)))
          (finally
            ;; Only called if `test` raises an exception
            (shutdown-agents)))))))
