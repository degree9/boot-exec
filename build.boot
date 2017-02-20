(set-env!
 :dependencies  '[[org.clojure/clojure                 "1.8.0" :scope "provided"]
                  [boot/core                           "2.7.1"]
                  [adzerk/bootlaces                    "0.1.13" :scope "test"]
                  [cheshire                            "5.7.0"]
                  [org.clojars.hozumi/clj-commons-exec "1.2.0"]
                  [degree9/boot-semver                 "1.4.1"]]
 :resource-paths   #{"src"})

(require
 '[adzerk.bootlaces :refer :all]
 '[boot-semver.core :refer :all]
 '[degree9.boot-exec :refer :all])

(task-options!
  pom    {:project 'degree9/boot-exec
          :version (get-version)
          :description "Boot-clj external process execution using Apache Commons Exec"
          :url         "https://github.com/degree9/boot-exec"
          :scm         {:url "https://github.com/degree9/boot-exec"}}
  target {:dir #{"target"}})

(deftask develop
  "Build boot-exec for development."
  []
  (comp
   (watch)
   (version :develop true
            :minor 'inc
            :patch 'zero
            :pre-release 'snapshot)
   (target)
   (build-jar)))

(deftask deploy
  "Build boot-exec and deploy to clojars."
  []
  (comp
   (version)
   (target)
   (build-jar)
   (push-release)))
