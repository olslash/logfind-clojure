(ns logfind.core
  (:gen-class))

(require '[clojure.string :as string])
(def search-root "/Users/mrobb/test")

(defn- user-prop
  "Returns the system property for user.<key>"
  [key]
  (System/getProperty (str "user." key)))

(defn- read-config
  "reads and returns the .logfind file in the user's home directory"
  []
  (string/split-lines (slurp (str (user-prop "home") "/.logfind"))))

(defn files
  "returns a list of all the files in a directory (recursive)"
  [dir]
  (file-seq (clojure.java.io/file dir)))

(defn- find-files
  "searches /home for files matching the provided pattern"
  [pattern]
  (filter (fn [x]
            (re-find (re-pattern pattern) (str x)))
          (files search-root)))

(defn -main
  "lol"
  ([] (print "provide one or more string params to search"))
  ([&args]
    (print (find-files "log"))))
