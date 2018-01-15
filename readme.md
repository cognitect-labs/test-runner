# test-runner

`test-runner` is a small library for discovering and running tests in
plain Clojure projects (i.e, those that use only Clojure's built-in
deps tooling, not Leiningen/boot/etc.)

## Rationale

Clojure's 1.9 release includes standalone tools for dependency
resolution, classpath construction, and launching processes. Clojure
also ships with a straightforward testing library, `clojure.test`.

Using these tools, however, there is currently no standard way to
discover and run unit tests. Including a heavyweight project tool such
as Leiningen or Boot just for the purpose of testing is
overkill. Projects can build their own ad-hoc test runners, but these
tend to lack features that will eventually be desired, and tend
towards the "quick and dirty," besides being nonstandard from project
to project.

This library aims to fill in the gap and provide a standardized,
easy-to-use entry entry point for discovering and running unit and
property-based tests while remaining a lightweight entry in Clojure's
suite of decomplected project management tools.

## Usage

Include a dependency on this project in your `deps.edn`. You will
probably wish to put it in a `dev` or `test` alias. For example:


```clojure
:aliases {:dev {:extra-paths ["test"]
                :extra-deps {net.vanderhart/test-runner {:git/url "git@github.com:levand/test-runner"
                                                         :sha "e3e4ce3d7e29349eeff44afd654bc2de6d5f3ae5"}}}}
```

Then, invoke Clojure via the command line, passing
`net.vanderhart.test-runner` as the main namespace to run using the
`-m` option.

```bash
clj -Cdev -Rdev -m net.vanderhart.test-runner
```

This will scan your project's `test` directory for any tests defined
using `clojure.test` and run them.

You may also supply any of the additional command line options:

```
  -d, --dir DIRNAME       Name of the directory containing tests. Defaults to "test".
  -n, --namespace SYMBOL  Symbol indicating a specific namespace to test.
  -v, --var SYMBOL        Symbol indicating the fully qualified name of a specific test.
  -i, --include KEYWORD   Run only tests that have this metadata keyword.
  -e, --exclude KEYWORD   Exclude tests with this metadata keyword.
  -h, --help              Display this help message
```

All options may be repeated multiple times, for a logical OR
effect. For example, the following invocation will run all tests in
the `foo.bar` and `foo.baz` namespaces, in the `test` and `src`
directories:

```
clj -Cdev -Rdev -m net.vanderhart.test-runner -d test -d src -n foo.bar -n foo.baz
```

### Using Inclusions and Exclusions

You can use inclusions and exclusions to run only a subset of your tests, identified by metadata on the test var.

For example, you could tag your integration tests like so:

```clojure
(deftest ^:integration test-live-system
  (is (= 200 (:status (http/get "http://example.com")))))
```

Then to run only integration tests, you could do:

```
clj -Cdev -Rdev -m net.vanderhart.test-runner -i :integration
```

Or to run all tests *except* for integration tests:

```
clj -Cdev -Rdev -m net.vanderhart.test-runner -e :integration
```

If both inclusions and exclusions are present, exclusions take priority over inclusions.
