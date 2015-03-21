# hello-cc

A small Clojure command line application using the Google Closure compiler to transform either CommonJS, AMD or ECMAScript6 modules into a Google Closure library.

## Usage

Pass the files you would like to transform and the type of the modules:

```
$ lein run -- --js example-js/hello-es6.js --js example-js/bye-es6.js -m es6 > out.js
```

or

```
$ lein uberjar
$ java -jar target/hello-cc-0.1.0-SNAPSHOT-standalone.jar --js example-js/hello-cjs.js -m commonjs
```
