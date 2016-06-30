(ns degree9.boot-exec
  {:boot/export-tasks true}
  (:require [boot.core :as boot]
            [boot.tmpdir :as tmpd]
            [boot.util :as util]
            [clojure.java.io :as io]
            [clj-commons-exec :as exec]
            [boot.task.built-in :as tasks]
            [cheshire.core :refer :all]))

(boot/deftask properties
  "Generate config/property files for exteral dev tools."
  [c contents  VAL str "Contents of config/property file."
   d directory VAL str "Directory to output config/property file."
   f file      VAL str "Config/Property file name."
   k cache-key VAL kw  "Optional cache key for when property files are used in multiple filesets."]
   (let [directory (:directory *opts*)
         file      (:file *opts*)
         propstr   (:contents *opts*)
         tmp       (cond cache-key (boot/cache-dir! cache-key)
                         directory (io/file directory)
                         :else     (boot/tmp-dir!))
         propf     (io/file tmp file)]
     (boot/with-pre-wrap fileset
       (util/info (str "Writing property file " file "...\n"))
       (doto propf io/make-parents (spit propstr))
       fileset)))

(boot/deftask exec
  "Process execution via Apache Commons Exec"
  [p process     VAL     str      "Name of process to execute."
   a arguments   VAL     [str]    "A list of arguments to pass to the executable."
   k cache-key   VAL     kw       "Optional cache key for when exec is used for various filesets."
   d directory   VAL     str      "Optional target directory to execute the process within."
   g global      VAL     str      "Optional global path to search for the executable."
   l local       VAL     str      "Optional local path to search for the executable."]
  (let [cache-key    (:cache-key *opts*)
        directory    (:directory *opts*)
        proc         (:process *opts*)
        args         (:arguments *opts*)
        tmp          (cond cache-key (boot/cache-dir! cache-key)
                           directory (io/file directory)
                           :else     (boot/tmp-dir!))
        local-exec   (io/file "." (:local *opts*) proc)
        global-exec  (io/file (:global *opts* "/usr/local/bin") proc)
        process      (cond (.exists local-exec) (.getAbsolutePath local-exec)
                           (.exists global-exec) (.getPath global-exec)
                           :else proc)]
    (boot/with-pre-wrap fileset
        (util/info (clojure.string/join ["Executing Process: " process "\n"]))
        (let [cmdresult   @(exec/sh (into [process] args) {:dir (.getAbsolutePath tmp)})
              exitcode    (:exit cmdresult)
              errormsg    (:err cmdresult)]
          (assert (= 0 exitcode) (util/fail (clojure.string/join ["Process failed with...: \n" errormsg "\n"])))
          (util/info (str "Process completed successfully...\n"))
          (-> fileset (boot/add-resource tmp) boot/commit!)))))
