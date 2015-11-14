#!/usr/bin/env boot

(set-env!
  :source-paths #{}
  :resource-paths #{"html" "less" "resources" "src"}
  :dependencies '[; Boot setup
                  [adzerk/boot-cljs "1.7.170-1"]
                  [adzerk/boot-cljs-repl "0.3.0"]
                  [adzerk/boot-reload "0.4.1"]
                  [deraen/boot-less "0.4.2"]

                  ; boot-cljs-repl dependencies
                  [com.cemerick/piggieback "0.2.1"]
                  [weasel "0.7.0"]
                  [org.clojure/tools.nrepl "0.2.12"]

                  ; Server dependencies
                  [clj-consonant "0.1.0-SNAPSHOT"]
                  [com.cognitect/transit-clj "0.8.285"]
                  [compojure "1.4.0"]
                  [http-kit "2.1.19"]
                  [org.clojure/tools.reader "1.0.0-alpha1"]
                  [org.danielsz/system "0.1.9"]
                  [ring-middleware-format "0.6.0"]

                  ; App dependencies
                  [cljsjs/highlight "8.4-0"]
                  [com.cognitect/transit-cljs "0.8.232"]
                  [org.clojure/clojurescript "1.7.170"]
                  [org.omcljs/om "1.0.0-alpha22-SNAPSHOT"]

                  ; Other dependencies
                  [devcards "0.2.0-8"]])

(task-options!
  pom {:project 'copaste
       :version "0.1.0-SNAPSHOT"})

(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[adzerk.boot-reload :refer [reload]]
         '[deraen.boot-less :refer [less]]
         '[reloaded.repl :refer [init start stop go reset]]
         '[system.boot :refer [system run]]
         '[server.systems :refer [development-system production-system]])

(deftask build-development
  []
  (comp
    (less)
    (sift :add-jar {'cljsjs/highlight #"^cljsjs/common/highlight/github.min.css"})
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
    (cljs-repl)
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
    (cljs-repl)
    (build-production)
    (run :main-namespace "server.core" :arguments [#'production-system])
    (wait)))
