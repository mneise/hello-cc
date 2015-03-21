(ns hello-cc.core
  (:import [java.util List]
           [com.google.javascript.jscomp ProcessCommonJSModules
            CompilerOptions SourceFile Result JSError])
  (:gen-class main true))

(defn report-failure [^Result result]
  (let [errors (.errors result)
        warnings (.warnings result)]
    (doseq [next (seq errors)]
      (println "ERROR:" (.toString ^JSError next)))
    (doseq [next (seq warnings)]
      (println "WARNING:" (.toString ^JSError next)))))

(defn process-commonjs
  [files]
  (let [^List externs '()
        ^List inputs (map #(SourceFile/fromFile %) files)
        ^CompilerOptions options (doto (CompilerOptions.)
                                   (.setProcessCommonJSModules true))
        compiler (com.google.javascript.jscomp.Compiler.)
        ^Result result (.compile compiler externs inputs options)]
    (if (.success result)
      (println (.toSource compiler))
      (report-failure result))))

(defn -main
  [& args]
  (process-commonjs args))
