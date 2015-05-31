(defproject com.postspectacular/resonate-workshop-2015 "0.1.0-SNAPSHOT"
  :description  "Clojurescript, reagent & core.async workshop at Resonate festival 2015"
  :url          "https://github.com/learn-postspectacular/resonate-workshop-2015"
  :license      {:name "Apache Software License v2.0"
                 :url "https://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [org.clojure/clojurescript "0.0-3297"]
                 [thi.ng/geom "0.0.803"]
                 [thi.ng/domus "0.1.0"]
                 [cljsjs/react "0.12.2-5"]
                 [org.clojars.toxi/re-frame "0.2.0"]
                 [cljs-log "0.2.1"]
                 [reagent "0.5.0"]
                 [figwheel "0.2.5" :exclusions [org.clojure/clojure org.clojure/clojurescript]]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]

  :plugins      [[lein-cljsbuild "1.0.5"]
                 [lein-figwheel "0.2.5" :exclusions [org.clojure/clojure org.clojure/clojurescript]]]

  :source-paths ["src/clj" "src/cljs"]

  :clean-targets ^{:protect false} ["resources/public/js"]

  :cljsbuild    {:builds [{:id "dev"
                           :source-paths ["src/cljs" "dev_src"]
                           :compiler {:output-to "resources/public/js/app.js"
                                      :output-dir "resources/public/js/out"
                                      :optimizations :none
                                      :main resonate2015.dev
                                      :asset-path "js/out"
                                      :source-map true
                                      :source-map-timestamp true
                                      :cache-analysis true }}
                          {:id "min"
                           :source-paths ["src/cljs"]
                           :compiler {:output-to "resources/public/js/app.js"
                                      :main resonate2015.day2.core
                                      :optimizations :advanced
                                      :pretty-print false}}]}

  :figwheel     {:http-server-root "public" ;; default and assumes "resources"
                 :server-port 3449 ;; default
                 :css-dirs ["resources/public/css"] ;; watch and update CSS

                 ;; Start an nREPL server into the running figwheel process
                 ;; :nrepl-port 7888

                 ;; Server Ring Handler (optional)
                 ;; if you want to embed a ring handler into the figwheel http-kit
                 ;; server, this is simple ring servers, if this
                 ;; doesn't work for you just run your own server :)
                 ;; :ring-handler hello_world.server/handler

                 ;; To be able to open files in your editor from the heads up display
                 ;; you will need to put a script on your path.
                 ;; that script will have to take a file path and a line number
                 ;; ie. in  ~/bin/myfile-opener
                 ;; #! /bin/sh
                 ;; emacsclient -n +$2 $1
                 ;;
                 ;; :open-file-command "myfile-opener"

                 ;; if you want to disable the REPL
                 ;; :repl false

                 ;; to configure a different figwheel logfile path
                 ;; :server-logfile "tmp/logs/figwheel-logfile.log"
                 })
