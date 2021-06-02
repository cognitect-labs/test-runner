# test-runner

`test-runner` is a small library for discovering and running tests in
projects using native Clojure deps (i.e, those that use only Clojure's
built-in dependency tooling, not Leiningen/boot/etc.)

## Rationale

Clojure's 1.9 release included standalone tools for dependency
resolution, classpath construction, and launching processes. Clojure
also ships with a straightforward testing library, `clojure.test`.

Using these tools, however, there was no standard way to
discover and run unit tests. Including a heavyweight project tool such
as Leiningen or Boot just for the purpose of testing is
overkill. Projects can build their own ad-hoc test runners, but these
tend to lack features that will eventually be desired, and tend
towards the "quick and dirty," besides being nonstandard from project
to project.

This library aims to fill in the gap and provide a standardized,
easy-to-use entry point for discovering and running unit and
property-based tests while remaining a lightweight entry in Clojure's
suite of decomplected project management tools.

## Configuration 

Include a dependency on this project in your `deps.edn`. You will
probably wish to put it in the `test` alias:

```clojure
;; v0.3.1
:aliases {:test {:extra-paths ["test"]
                 :extra-deps {io.github.cognitect-labs/test-runner 
                              {:git/url "https://github.com/cognitect-labs/test-runner.git"
                               :sha "705ad25bbf0228b1c38d0244a36001c2987d7337"}}
                 :main-opts ["-m" "cognitect.test-runner"]
                 :exec-fn cognitect.test-runner.api/test}}
```

### Invoke with `clojure -X` (exec style)

Invoking the test-runner with `clojure -X` will call the test function with a map of arguments,
which can be supplied either in the alias (via `:exec-args`) or on the command-line, or both.

Create the alias with `:exec-fn` to simplify the call:

```clojure
:aliases {:test {:extra-paths ["test"]
                 :extra-deps {io.github.cognitect-labs/test-runner 
                              {:git/url "https://github.com/cognitect-labs/test-runner.git"
                               :sha "705ad25bbf0228b1c38d0244a36001c2987d7337"}}
                 :exec-fn cognitect.test-runner.api/test}}
```

Invoke it with:

```bash
clj -X:test ...args...
```

This will scan your project's `test` directory for any tests defined
using `clojure.test` and run them.

You may also supply any of the additional command line options:

```
  :dirs - coll of directories containing tests, default= ["test"]
  :nses - coll of namespace symbols to test
  :patterns - coll of regex strings to match namespaces
  :vars - coll of fully qualified symbols to run tests on
  :includes - coll of test metadata keywords to include
  :excludes - coll of test metadata keywords to exclude"
```

If neither :dirs or :nses is supplied, will use:

```
  :nses [".*-test$"]
```

### Invoke with `clojure -M` (clojure.main)

To use the older clojure.main command line style:

```bash
clj -M:test ...args...
```

Use any of the additional command line options:

```
  -d, --dir DIRNAME            Name of the directory containing tests. Defaults to "test".
  -n, --namespace SYMBOL       Symbol indicating a specific namespace to test.
  -r, --namespace-regex REGEX  Regex for namespaces to test. Defaults to #".*-test$"
                               (i.e, only namespaces ending in '-test' are evaluated)
  -v, --var SYMBOL             Symbol indicating the fully qualified name of a specific test.
  -i, --include KEYWORD        Run only tests that have this metadata keyword.
  -e, --exclude KEYWORD        Exclude tests with this metadata keyword.
  -H, --test-help              Display this help message
```

## Operation

There are three main steps to test execution:

* Find dirs to scan - by default "test"
* Find namespaces in those dirs (either by specific name or regex pattern or both) - by default all ending in "-test"
* Find vars to invoke in those namespaces - by default all, unless specific vars are listed, further filtered by include and exclude metadata

### Using Inclusions and Exclusions

You can use inclusions and exclusions to run only a subset of your tests, identified by metadata on the test var.

For example, you could tag your integration tests like so:

```clojure
(deftest ^:integration test-live-system
  (is (= 200 (:status (http/get "http://example.com")))))
```

Then to run only integration tests, you could do one of:

```
clj -X:test :includes '[:integration]'
clj -M:test -i :integration
```

Or to run all tests *except* for integration tests, one of:

```
clj -X:test :excludes '[:integration]'
clj -M:test -e :integration
```

If both inclusions and exclusions are present, exclusions take priority over inclusions.

## Copyright and License

Copyright © 2018-2021 Cognitect

Licensed under the Eclipse Public License, Version 2.0
