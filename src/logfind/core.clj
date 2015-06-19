(ns logfind.core
  (:gen-class))

(require '[clojure.string :as string])
(defn- user-prop
  "Returns the system property for user.<key>"
  [key]
  (System/getProperty (str "user." key)))


(def search-root "/Users/mrobb/test")
(def config-file  (str (user-prop "home") "/.logfind"))


(defn- read-config
  "reads and returns the .logfind file in the user's home directory"
  []
  (string/split-lines (slurp config-file)))

(defn files
  "returns a fileseq of all the filenames in a directory"
  [dir]
  (file-seq (clojure.java.io/file dir)))

; fixme-  http://stackoverflow.com/questions/13063594/how-to-filter-a-directory-listing-with-a-regular-expression-in-clojure
(defn regex-file-seq
  "Lazily filter a directory based on a regex."
  [re dir]
  (filter #(re-find re (.getPath %)) dir))

(defn- join-and
  "joins the args into a single regex that will match them all (boolean AND)"
  [& args]
  (re-pattern
    (str ".*" (string/join ".*" args) ".*")))

(defn- join-or
  "joins the args into a single regex that will match them all (boolean OR)"
  [& args]
  (re-pattern
    (str (string/join "|" args))))

(defn- in-file
  "search a filereader for a pattern and return the filename if there was a match"
  [re file]
  (boolean (re-find re (slurp file))))

(defn- flags
  "get -flags from an input string"
  [inp]
  ; filter twice, one with a -* check and one for the rest
  (filterv #(= (first %) \-) inp))

(defn- arguments
  "get args (no -) from an input string"
  [inp]
  ; filter twice, one with a -* check and one for the rest
  (filterv #(not= (first %) \-) inp))

(defn -main
  "search all files matching the patterns specified in ~/.logfind for the
  strings given in args. Prints the filenames that contain matches.
  uses arg1 AND arg2 unless -o flag set, then uses arg1 OR arg2"
  ([] (print "provide one or more string params to search"))
  ([& args]
   (let [arg-flags (flags args)
         filemask (re-pattern (string/join "|" (read-config)))
         searchpattern (apply (if (some #{"-o"} arg-flags)
                                join-or
                                join-and)
                              (arguments args))]
     (doseq [file (regex-file-seq filemask (files search-root))]
       (if (in-file searchpattern file)
         (println (str file)))))))
