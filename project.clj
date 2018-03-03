(defproject wiki_search_xml "0.4.0-SNAPSHOT"
  :description "A text search app within wikipedia xml abstract files"
  :url "https://github.com/arichiardi/wiki-search-xml"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/tools.trace "0.7.8"]
                 [com.stuartsierra/component "0.2.3"]
                 [slingshot "0.12.2"]
                 [environ "1.0.0"]

                 ;; Data
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.zip "0.1.1"]
                 [clj-tuple "0.2.1"]
                 [danlentz/clj-uuid "0.1.5"]
                 [nio "1.0.3"]
                 ;; [com.ashafa/clutch "0.4.0"]

                 ;; Network
                 [ring/ring-core "1.4.0-RC1"]
                 [ring/ring-json "0.3.1"]
                 [http-kit "2.1.18"]
                 [compojure "1.3.4"]
                 [commons-daemon/commons-daemon "1.0.15"]

                 ;; Logging
                 [org.clojure/tools.logging "0.3.1"]
                 [org.apache.logging.log4j/log4j-api "2.3"]
                 [org.apache.logging.log4j/log4j-core "2.3"]
                 [org.apache.logging.log4j/log4j-1.2-api "2.3"]
                 [org.apache.logging.log4j/log4j-jul "2.3"]]
  :jvm-opts ^:replace ["-Dclojure.assert-if-lazy-seq=true"]
  :plugins [[lein-environ "1.0.0"]
            [lein-pprint "1.1.2"]]
  :min-lein-version "2.0.0"
  :aliases {"bg-repl" ["trampoline" "repl" ":headless" "> repl.out 2> repl.err < /dev/null &"]}
  :profiles {:uberjar {:aot :all
                       :main wiki_search_xml.daemon}
             :dev {:debug true
                   :dependencies [[org.clojure/tools.namespace "0.2.10"]
                                  [org.clojure/tools.trace "0.7.8"]
                                  [midje "1.7.0"]
                                  [midje-notifier "0.2.0"]
                                  [ring/ring-devel "1.4.0-RC1"]
                                  [criterium "0.4.3"]
                                  [com.taoensso/timbre "4.0.2"]]
                   :plugins [[lein-midje "3.1.4-SNAPSHOT"]]
                   :source-paths ["dev"]
                   :resource-paths ^:replace ["dev-resources"]
                   :env {:wsx-logger-name "wiki-search-xml-dev-logger"
                         :squiggly {:checkers [:eastwood :kibit]}}}
             :test {:env {:wsx-logger-name "wiki-search-xml-test-logger"}
                    :resource-paths ^:replace ["dev/resources"]}})
