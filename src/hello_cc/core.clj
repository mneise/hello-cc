(ns hello-cc.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as s])
  (:import [java.util List]
           [com.google.javascript.jscomp ProcessCommonJSModules
            CompilerOptions SourceFile Result JSError])
  (:gen-class main true))

(def module-types ["commonjs", "amd"])

(defn report-failure [^Result result]
  (let [errors (.errors result)
        warnings (.warnings result)]
    (doseq [next (seq errors)]
      (println "ERROR:" (.toString ^JSError next)))
    (doseq [next (seq warnings)]
      (println "WARNING:" (.toString ^JSError next)))))

(defn process-commonjs
  [file]
  (let [^List externs '()
        ^List inputs (list (SourceFile/fromFile file))
        ^CompilerOptions options (doto (CompilerOptions.)
                                   (.setProcessCommonJSModules true))
        compiler (com.google.javascript.jscomp.Compiler.)
        ^Result result (.compile compiler externs inputs options)]
    (if (.success result)
      (println (.toSource compiler))
      (report-failure result))))

(def cli-options
  ;; An option with a required argument
  [[nil "--js JS_FILE" "JavaScript File"
    :validate [#(not (s/blank? %)) "Please pass a valid JavaScript filename"]]
   ["-m" "--module-type" "JavaScript module type"
    :validate [(fn [v] (some #(= % v) module-types))
               (str "Please use one of the following module types: "
                    (s/join ", " module-types))]]
   ["-h" "--help"]])

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 summary)
      errors (exit 1 (s/join errors)))
    (if (:js options)
      (process-commonjs (:js options))
      (exit 1 "Please pass a JavaScript filename"))))
