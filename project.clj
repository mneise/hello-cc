(defproject hello-cc "0.1.0-SNAPSHOT"
  :description "hello-cc: Simple command line app for processing JS modules."
  :url "https://github.com/MNeise/hello-cc"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/tools.cli "0.3.1"]
                 [com.google.javascript/closure-compiler "v20150729"]]
  :profiles {:uberjar {:aot :all}}
  :main hello-cc.core)
