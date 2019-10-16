(ns sample-1-2.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [sample-1-2.core-test]))

(doo-tests 'sample-1-2.core-test)

