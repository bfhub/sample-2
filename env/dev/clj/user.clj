(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
    [sample-1-2.config :refer [env]]
    [clojure.spec.alpha :as s]
    [expound.alpha :as expound]
    [mount.core :as mount]
    [sample-1-2.figwheel :refer [start-fw stop-fw cljs]]
    [sample-1-2.core :refer [start-app]]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn start 
  "Starts application.
  You'll usually want to run this on startup."
  []
  (mount/start-without #'sample-1-2.core/repl-server))

(defn stop 
  "Stops application."
  []
  (mount/stop-except #'sample-1-2.core/repl-server))

(defn restart 
  "Restarts application."
  []
  (stop)
  (start))


