(set-env!
  :source-paths #{"src"}
  :dependencies '[
                  ; dev
                  [adzerk/bootlaces "0.1.13" :scope "test"]
                  [adzerk/boot-test "1.0.4"  :scope "test"]
                  [str-to-argv      "0.1.0"  :score "test"]

                  ; silence slf4j logging dammit
                  [org.slf4j/slf4j-nop        "1.7.21"]

                  ; alda server / worker
                  [alda/server-clj  "0.0.1"]

                  ; client
                  [com.beust/jcommander                 "1.48"]
                  [commons-io/commons-io                "2.5"]
                  [org.apache.commons/commons-lang3     "3.4"]
                  [com.google.code.gson/gson            "2.6.1"]
                  [org.fusesource.jansi/jansi           "1.11"]
                  [us.bpsm/edn-java                     "0.4.6"]
                  [com.jcabi/jcabi-manifests            "1.1"]
                  [org.zeromq/jeromq                    "0.3.5"]])

(require '[adzerk.bootlaces :refer :all])

(def ^:const +version+ "0.0.1")

(bootlaces! +version+)

(task-options!
  javac   {:options (let [jdk7-bootclasspath (System/getenv "JDK7_BOOTCLASSPATH")]
                      (concat
                        []
                        (when-not (empty? jdk7-bootclasspath)
                          ["-source" "1.7"
                           "-target" "1.7"
                           "-bootclasspath" jdk7-bootclasspath])))}

  pom     {:project 'alda/client-java
           :version +version+
           :description "A music programming language for musicians"
           :url "https://github.com/alda-lang/alda"
           :scm {:url "https://github.com/alda-lang/alda"}
           :license {"name" "Eclipse Public License"
                     "url" "http://www.eclipse.org/legal/epl-v10.html"}}

  jar     {:file "alda-client.jar"
           :main 'alda.Main}

  install {:pom "alda/client-java"}

  target  {:dir #{"target"}})

(deftask dev
  "Runs the Alda client for development.

   To test changes to the Alda client, run `boot dev -a client -x \"args here\"`.

   For example:

      boot dev -a client -x \"play --file /path/to/file.alda\"

   The arguments must be a single command-line string to be passed to the
   command-line client as if entering them on the command line. The example
   above is equivalent to running `alda play --file /path/to/file.alda` on the
   command line.

   One caveat to running the client this way (as opposed to building it and
   running the resulting executable) is that the client does not have the
   necessary permissions to start a new process, e.g. to start an Alda server
   via the client."
  [a app              APP     str  "The Alda application to run (server, repl or client)."
   x args             ARGS    str  "The string of CLI args to pass to the client."
   p port             PORT    int  "The port on which to start the server/worker."
   w workers          WORKERS int  "The number of workers for the server to start."
   F alda-fingerprint         bool "Allow the Alda client to identify this as an Alda process."]
  (comp
    (javac)
    (with-pass-thru fs
      (require '[str-to-argv])
      (import 'alda.Main)
      (eval `(alda.Main/main
               (into-array String
                 (str-to-argv/split-args (or ~args ""))))))))

(deftask assert-jdk7-bootclasspath
  "Ensures that the JDK7_BOOTCLASSPATH environment variable is set, as required
   to compile a final jar file that supports Java 1.7+."
  []
  (with-pre-wrap fileset
    (assert (not (empty? (System/getenv "JDK7_BOOTCLASSPATH")))
            (str "Alda requires JDK7 in order to build its uberjar, in order "
                 "to provide out-of-the-box support for users who may have "
                 "older versions of Java. Please install JDK7 and set the "
                 "environment variable JDK7_BOOTCLASSPATH to the path to your "
                 "JDK7 classpath jar, e.g. (OS X example) "
                 "/Library/Java/JavaVirtualMachines/jdk1.7.0_71.jdk/Contents/"
                 "Home/jre/lib/rt.jar"))
    fileset))

(deftask package
  "Builds jar file."
  []
  (comp (assert-jdk7-bootclasspath)
        (javac)
        (pom)
        (jar)))

(deftask deploy
  "Builds jar file, installs it to local Maven repo, and deploys it to Clojars."
  []
  (comp (package) (install) (push-release)))

