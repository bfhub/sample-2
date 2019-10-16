(ns sample-1-2.app
  (:require [sample-1-2.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
