(defproject wiki_search_xml "0.1.0-SNAPSHOT"
  :description "A text search app within wikipedia xml abstract files"
  :url "https://github.com/arichiardi/wiki-search-xml"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.zip "0.1.1"]
                 [com.stuartsierra/component "0.2.3"]
                 [environ "1.0.0"]
                 [http-kit "2.1.18"]
                 [cheshire "5.5.0"]
                 [slingshot "0.12.2"]
                 [ring/ring-core "1.4.0-RC1"]
                 [ring/ring-jetty-adapter "1.4.0-RC1"]
                 [commons-daemon/commons-daemon "1.0.15"]
                 ;; [com.ashafa/clutch "0.4.0"]
                 
                 ;; Logging
                 [org.clojure/tools.logging "0.3.1"]
                 [org.apache.logging.log4j/log4j-api "2.3"]
                 [org.apache.logging.log4j/log4j-core "2.3"]
                 [org.apache.logging.log4j/log4j-1.2-api "2.3"]
                 [org.apache.logging.log4j/log4j-jul "2.3"]
                 ]
  :jvm-opts ^:replace []
  :plugins [[lein-environ "1.0.0"]
            [lein-pprint "1.1.2"]]
  :aliases {"bg-repl" ["trampoline" "repl" ":headless" "> repl.out 2> repl.err < /dev/null &"]}
  :profiles {:dev {:debug true
                   :dependencies [[org.clojure/tools.namespace "0.2.10"]
                                  [midje "2.0.0-SNAPSHOT"]
                                  [midje-notifier "0.2.0"]
                                  [ring/ring-devel "1.4.0-RC1"]]
                   :source-paths ["dev"]
                   :resource-paths ^:replace ["dev-resources"]
                   :env {:wsx-logger-name "wiki-search-xml-dev-logger"}}
             :test {:env {:wsx-logger-name "wiki-search-xml-test-logger"}
                    :resource-paths ["dev/resources"]}})
