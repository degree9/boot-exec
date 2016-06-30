(set-env!
 :dependencies  '[[org.clojure/clojure                 "1.7.0"]
                  [boot/core                           "2.6.0"]
                  [adzerk/bootlaces                    "0.1.13"]
                  [org.clojars.hozumi/clj-commons-exec "1.2.0"]
                  [cheshire                            "5.5.0"]
                  [degree9/boot-semver                 "1.2.0"]]
 :resource-paths   #{"src"})

(require
 '[adzerk.bootlaces :refer :all]
 '[boot-semver.core :refer :all]
 '[degree9.boot-exec :refer :all])

(task-options!
  pom {:project 'degree9/boot-exec
       :version (get-version)
       :description "Boot-clj external process execution using Apache Commons Exec"
       :url         "https://github.com/degree9/boot-exec"
       :scm         {:url "https://github.com/degree9/boot-exec"}})

(deftask dev
  "Build boot-exec for development."
  []
  (comp
   (watch)
   (version :no-update true
            :minor 'inc
            :patch 'zero
            :pre-release 'snapshot)
   (target  :dir #{"target"})
   (build-jar)))

(deftask deploy
  "Build boot-exec and deploy to clojars."
  []
  (comp
   (version :minor 'inc
            :patch 'zero)
   (target  :dir #{"target"})
   (build-jar)
   (push-release)))
