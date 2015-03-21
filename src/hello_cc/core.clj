(ns hello-cc.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as s])
  (:import [java.util List]
           [java.util.logging Level]
           [com.google.javascript.jscomp ProcessCommonJSModules
            CompilerOptions SourceFile Result JSError])
  (:gen-class main true))

(def module-types [:commonjs :amd])


(def cli-options
  [[nil "--js JS_FILE" "JavaScript File"
    :parse-fn str
    :validate [#(not (s/blank? %)) "Please pass a valid JavaScript filename"]]
   ["-m" "--module-type TYPE" "JavaScript module type"
    :parse-fn keyword
    :validate [(fn [v] (some #(= % v) module-types))
               (str "Please use one of the following module types: "
                    (s/join ", " module-types))]]
   ["-h" "--help"]])

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn report-failure [^Result result]
  (let [errors (.errors result)
        warnings (.warnings result)]
    (doseq [next (seq errors)]
      (println "ERROR:" (.toString ^JSError next)))
    (doseq [next (seq warnings)]
      (println "WARNING:" (.toString ^JSError next)))))

(defn ^com.google.javascript.jscomp.Compiler make-closure-compiler []
  (let [compiler (com.google.javascript.jscomp.Compiler.)]
    (com.google.javascript.jscomp.Compiler/setLoggingLevel Level/WARNING)
    compiler))

(defn set-options [opts ^CompilerOptions compiler-options]
  (case (:type opts)
    :commonjs (.setProcessCommonJSModules compiler-options true)
    :amd (doto compiler-options
           (.setProcessCommonJSModules true)
           (.setTransformAMDToCJSModules true)))
  compiler-options)

(defn process-js-module
  [file type]
  (let [^List externs '()
        ^List inputs (list (SourceFile/fromFile file))
        ^CompilerOptions options (set-options {:type type} (CompilerOptions.))
        compiler (make-closure-compiler)
        ^Result result (.compile compiler externs inputs options)]
    (if (.success result)
      (println (.toSource compiler))
      (report-failure result))))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)
        errors (if-not (:js options)
                 (conj errors "Please pass a JavaScript filename")
                 errors)
        errors (if-not (:module-type options)
                 (conj errors "Please specify a module type")
                 errors)]
    (cond
      (:help options) (exit 0 summary)
      errors (exit 1 (s/join "\n" errors)))
    (process-js-module (:js options) (:module-type options))))
