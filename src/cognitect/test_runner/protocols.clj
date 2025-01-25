(ns cognitect.test-runner.protocols)

(defprotocol TestRunner :extend-via-metadata true
  (enable-filtering! [this opts nses])
  (contains-tests? [this opts ns])
  (run-tests [this opts nses])
  (disable-filtering! [this opts nses]))
