#!/usr/bin/env boot

(set-env!
  :source-paths #{}
  :resource-paths #{"html" "less" "resources" "src"}
  :dependencies '[; Boot setup
                  [adzerk/boot-cljs "1.7.170-1"]
                  [adzerk/boot-reload "0.4.1"]
                  [deraen/boot-less "0.4.2"]

                  ; Server dependencies
                  [compojure "1.4.0"]
                  [http-kit "2.1.19"]
                  [org.clojure/tools.reader "1.0.0-alpha1"]
                  [org.danielsz/system "0.1.9"]
                  [ring-middleware-format "0.6.0"]

                  ; App dependencies
                  [cljs-ajax "0.5.1"]
                  [org.clojure/clojurescript "1.7.170"]
                  [org.omcljs/om "1.0.0-alpha19-SNAPSHOT"]

                  ; Other dependencies
                  [devcards "0.2.0-8"]])

(task-options!
  pom {:project 'copaste
       :version "0.1.0-SNAPSHOT"})

(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-reload :refer [reload]]
         '[deraen.boot-less :refer [less]]
         '[reloaded.repl :refer [init start stop go reset]]
         '[system.boot :refer [system run]]
         '[server.systems :refer [development-system production-system]])

(deftask build-development
  []
  (comp
    (less)
    (cljs :source-map true
          :optimizations :none
          :compiler-options {:devcards true})))

(deftask run-development
  []
  (comp
    (watch)
    (system :sys #'development-system
            :auto-start true
            :hot-reload true
            :files ["handler.clj"])
    (reload :on-jsload 'app.app/run)
    (build-development)
    (repl :server true)))

(deftask build-production
  []
  (comp
    (less :compression true)
    (cljs :optimizations :advanced
          :compiler-options {:devcards true})))

(deftask run-production
  []
  (comp
    (watch)
    (reload :on-jsload 'app.app/run)
    (build-production)
    (run :main-namespace "server.core" :arguments [#'production-system])
    (wait)))
