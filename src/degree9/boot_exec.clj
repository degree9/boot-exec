(ns degree9.boot-exec
  {:boot/export-tasks true}
  (:require [boot.core :as boot]
            [boot.tmpdir :as tmpd]
            [boot.util :as util]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clj-commons-exec :as ex]
            [boot.task.built-in :as tasks]
            [cheshire.core :refer :all])
  (:import (org.apache.commons.exec OS)
           (java.io File)))

(defn get-directory [*opts*]
  (let [cache-key (:cache-key *opts*)
        directory (:directory *opts*)]
    (cond directory (io/file directory)
          cache-key (boot/cache-dir! cache-key)
          :else     (boot/tmp-dir!))))

(boot/deftask properties
  "Generate config/property files for exteral dev tools."
  [c contents  VAL str  "Contents of config/property file."
   d directory VAL str  "Directory to output config/property file."
   f file      VAL str  "Config/Property file name."
   k cache-key VAL kw   "Optional cache key for when property files are used in multiple filesets."
   i include       bool "Include property file in fileset."]
   (let [file      (:file *opts*)
         propstr   (:contents *opts*)
         include?  (:include *opts*)
         tmp       (get-directory *opts*)
         propf     (io/file tmp file)]
     (boot/with-pre-wrap fileset
       (util/info (str "Writing property file " file "...\n"))
       (doto propf io/make-parents (spit propstr))
       (if include?
         (let []
           (util/info (str "Adding property file to fileset...\n"))
           (-> fileset (boot/add-resource tmp) boot/commit!))
         fileset))))

(def ^:private os-windows?
  (OS/isFamilyWindows))

(def ^:private executable-extensions
  (cond-> [""]
          os-windows?
          (into (-> (System/getenv "PATHEXT")
                    (string/split #";")))))

(defn- get-executable
  [path name]
  (->> executable-extensions
       (map #(io/file path (str name %)))
       (filter #(.canExecute %))
       first))

(defn get-process [*opts*]
  (let [proc        (:process *opts*)
        local-path  (:local *opts* "./")
        global-path (:global *opts* "/usr/local/bin")
        local-exec  (get-executable local-path proc)
        global-exec (get-executable global-path proc)]
    (cond local-exec  (.getAbsolutePath local-exec)
          global-exec (.getPath global-exec)
          :else proc)))

(defn- sh
  [process args dir]
  (ex/sh (into (if-not os-windows?
                 [process]
                 ["cmd" "/c" process])
               args)
         {:dir dir}))

(defn exec-impl [fileset *opts*]
  (let [process (get-process *opts*)
        args    (:arguments *opts*)
        tmp     (get-directory *opts*)
        cmd     (sh process args (.getAbsolutePath tmp))
        show?   (:show *opts*)]
    (util/info (string/join ["Executing Process: " process "\n"]))
    (util/dbug (string/join ["Executing Process with arguments: " args "\n"]))
    (let [cmdresult   @cmd
          exitcode    (:exit cmdresult)
          errormsg    (:err cmdresult)
          stdout      (:out cmdresult)]
      (cond (not= 0 exitcode) (util/fail "Process failed with...: \n %s \n" errormsg)
            errormsg          (util/fail errormsg)
            show?             (util/info "%s" stdout)
            :else             (util/dbug "%s" stdout))
      (util/info "Process completed successfully...\n"))
    (if (:include *opts*) (-> fileset (boot/add-resource tmp) boot/commit!) fileset)))

(boot/deftask exec
  "Process execution via Apache Commons Exec"
  [p process     VAL     str      "Name of process to execute."
   a arguments   VAL     [str]    "A list of arguments to pass to the executable."
   k cache-key   VAL     kw       "Optional cache key for when exec is used for various filesets."
   d directory   VAL     str      "Optional target directory to execute the process within."
   g global      VAL     str      "Optional global path to search for the executable."
   l local       VAL     str      "Optional local path to search for the executable."
   i include             bool     "Include files added to the working directory."
   s show                bool     "Show executable output. By default output only with verbose (boot -vv)."]
  (boot/with-pre-wrap fileset
    (exec-impl fileset *opts*)))

(boot/deftask post-exec
  "Process execution via Apache Commons Exec (post-wrap)"
  [p process     VAL     str      "Name of process to execute."
   a arguments   VAL     [str]    "A list of arguments to pass to the executable."
   k cache-key   VAL     kw       "Optional cache key for when exec is used for various filesets."
   d directory   VAL     str      "Optional target directory to execute the process within."
   g global      VAL     str      "Optional global path to search for the executable."
   l local       VAL     str      "Optional local path to search for the executable."]
  (boot/with-post-wrap fileset
    (exec-impl fileset *opts*)))

(boot/deftask boot
  "Launch boot in boot."
  [a arguments VAL [str] "Cli arguments to pass to boot."
   d directory VAl str   "Optional target directory to execute boot within."]
  (let [args (:arguments *opts*)
        dir  (:directory *opts*)]
    (exec :process "boot" :arguments args :directory dir :show true)))
