Changelog
===========

* v0.5.0 - Sept 3, 2021
  * Fix #34 - remove custom exit logic, now handled by clj -X
* v0.4.0 - July 16, 2021
  * Fix #33 - don't test namespaces without active tests - thanks Mathieu Lirzin!
* v0.3.1 - May 24, 2021
  * Fix docstring typo
* v0.3.0 - May 21, 2021
  * Use default ns regex only when neither namespace or regex is supplied
* v0.2.1 - May 9, 2021
  * Remove debug printing
* v0.2.0 - May 7, 2021
  * Fix #12 - fix reflection warning
  * Fix #17 - mix of -n and -r should work
  * Add #26 - use exit code to 1 when args are invalid
  * Add #17 - skip fixtures if no vars from test ns are being run
  * Add #28 - add entry point for -X style invocation
  * Bump deps to latest + clojure 1.9.0

