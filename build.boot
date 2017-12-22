(set-env!
 :dependencies  '[[org.clojure/clojure                 "1.8.0" :scope "provided"]
                  [boot/core                           "2.7.1"]
                  [cheshire                            "5.7.1"]
                  [org.clojars.hozumi/clj-commons-exec "1.2.0"]
                  [degree9/boot-semver                 "1.6.0" :scope "test"]]
 :resource-paths   #{"src"})

(require
 '[degree9.boot-semver :refer :all]
 '[degree9.boot-exec   :as ex])

(task-options!
  pom    {:project 'degree9/boot-exec
          :description "Boot-clj external process execution using Apache Commons Exec."
          :url         "https://github.com/degree9/boot-exec"
          :scm         {:url "https://github.com/degree9/boot-exec"}}
  target {:dir #{"target"}})

(deftask develop
  "Build boot-exec for development."
  []
  (comp
   (version :develop true
            :minor 'inc
            :patch 'zero
            :pre-release 'snapshot)
   (watch)
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
