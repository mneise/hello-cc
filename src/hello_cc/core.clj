(ns hello-cc.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [cljs.closure :as cl])
  (:import [java.util List]
           [java.util.logging Level]
           [com.google.javascript.jscomp CompilerOptions SourceFile
            Result JSError CompilerOptions$LanguageMode CompilerInput
            ProcessCommonJSModules ES6ModuleLoader ProcessEs6Modules
            CompilerInput]
           [com.google.javascript.rhino Node InputId])
  (:gen-class))

(def module-types [:commonjs :amd :es6])

(def cli-options
  [[nil "--js JS_FILE" "JavaScript File"
    :parse-fn str
    :assoc-fn (fn [m k v] (update-in m [k] #(conj % v)))
    :validate [#(not (string/blank? %)) "Please pass a valid JavaScript filename"]]
   ["-m" "--module-type TYPE" "JavaScript module type"
    :parse-fn keyword
    :validate [(fn [v] (some #(= % v) module-types))
               (str "Please use one of the following module types: "
                    (string/join ", " module-types))]]
   ["-h" "--help"]])

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn set-options [opts ^CompilerOptions compiler-options]
  (case (:type opts)
    :commonjs (.setProcessCommonJSModules compiler-options true)
    :amd (doto compiler-options
           (.setProcessCommonJSModules true)
           (.setTransformAMDToCJSModules true))
    :es6 (doto compiler-options
           (.setLanguageIn CompilerOptions$LanguageMode/ECMASCRIPT6)
           (.setLanguageOut CompilerOptions$LanguageMode/ECMASCRIPT5)))
  (.setPrettyPrint compiler-options true)
  compiler-options)

(defn process-js-module
  [files type]
  (let [^List externs '()
        ^List inputs (map #(SourceFile/fromFile %) files)
        ^CompilerOptions options (set-options {:type type} (CompilerOptions.))
        compiler (cl/make-closure-compiler)
        ^Result result (.compile compiler externs inputs options)]
    (if (.success result)
      (println (.toSource compiler))
      (cl/report-failure result))))

(defn process-js-module2
  [files type]
  (let [^List externs '()
        module-root "./"
        ^List source-files (map #(SourceFile/fromFile %) files)
        ^List inputs (map #(CompilerInput. %) source-files)
        ^CompilerOptions options (doto (CompilerOptions.)
                                   (.setPrettyPrint true))
        compiler (doto (cl/make-closure-compiler)
                   (.init externs source-files options))
        es6-loader (ES6ModuleLoader. (list module-root) inputs)
        cjs (ProcessCommonJSModules. compiler es6-loader true)]
    (doseq [file source-files
            :let [^Node root (.parse compiler file)]]
      (.process cjs nil root)
      (println (.toSource compiler root)))))

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
      errors (exit 1 (string/join "\n" errors)))
    (process-js-module2 (:js options) (:module-type options))))
