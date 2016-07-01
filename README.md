# boot-exec
[![Clojars Project](https://img.shields.io/clojars/v/degree9/boot-exec.svg)](https://clojars.org/degree9/boot-exec)

Boot-clj external process execution using Apache Commons Exec

(Sub)Process Execution for [boot-clj][1].

* Provides `exec` task for process execution
* Provides `properties` task for property/config file generation.

> The following outlines basic usage of the task, extensive testing has not been done.
> Please submit issues and pull requests!

## Usage

Add `boot-exec` to your `build.boot` dependencies and `require` the namespace:

```clj
(set-env! :dependencies '[[degree9/boot-exec "X.Y.Z" :scope "test"]])
(require '[degree9.boot-exec :refer :all])
```

Execute an external build tool:

```bash
boot exec -p bower -a "install"
```

Create a properties file:

```bash
boot properties -f bower.json -d <some path> -c <file contents> 
```

Use in a wrapper task:

```clojure
(boot/deftask bower
  "boot-clj wrapper for bower"
  [...]
  (let [...]
    (comp
      (exec/properties :contents bwrjson :directory tmp-path :file "bower.json")
      (exec/properties :contents bwrrc :directory tmp-path :file ".bowerrc")
      (exec/exec :process "bower" :arguments ["install" "--allow-root"] :directory tmp-path :local "node_modules/bower/bin"))))
```

##Task Options

The `exec` task exposes options for specifying where to look for an executable and where to execute once it is found.

```clojure
p process     VAL     str      "Name of process to execute."
a arguments   VAL     [str]    "A list of arguments to pass to the executable."
k cache-key   VAL     kw       "Optional cache key for when exec is used for various filesets."
d directory   VAL     str      "Optional target directory to execute the process within."
g global      VAL     str      "Optional global path to search for the executable."
l local       VAL     str      "Optional local path to search for the executable."```
```

The `:cache-key` and `directory` options are mutually exclusive, the task first checks for a cache-key if found this is the location where the process will execute. If a directory is provided instead the directory location will be used for execution. If neither are provided the task will generate and use a temporary directory.

[1]: https://github.com/boot-clj/boot
[2]: https://docs.oracle.com/middleware/1212/core/MAVEN/maven_version.htm
