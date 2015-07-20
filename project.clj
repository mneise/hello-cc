(defproject hello-cc "0.1.0-SNAPSHOT"
  :description "hello-cc: Simple command line app for processing JS modules."
  :url "https://github.com/MNeise/hello-cc"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/clojurescript "0.0-3126"]
                 [com.google.javascript/closure-compiler "v20150609-SNAPSHOT"]]
  :profiles {:uberjar {:aot :all}}
  :main hello-cc.core)
