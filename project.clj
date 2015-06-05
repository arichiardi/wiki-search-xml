(defproject wiki_search_xml "0.1.0-SNAPSHOT"
  :description "A text search app within wikipedia xml abstract files"
  :url "https://github.com/arichiardi/wiki-search-xml"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.stuartsierra/component "0.2.3"]
                 [environ "1.0.0"]
                 [clj-http-lite "0.2.1"]
                 [cheshire "5.5.0"]
                 [slingshot "0.12.2"]
                 
                 ;; Logging
                 [org.slf4j/log4j-over-slf4j "1.7.12"] 
                 [org.slf4j/jcl-over-slf4j "1.7.12"] 
                 [org.slf4j/jul-to-slf4j "1.7.12"] 
                 [org.slf4j/slf4j-api "1.7.12"] 
                 [ch.qos.logback/logback-classic "1.0.13"]]
  
  :plugins [[lein-environ "1.0.0"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.10"]
                                  [midje "2.0.0-SNAPSHOT"]
                                  [midje-notifier "0.2.0"]]
                   :source-paths ["dev"]
                   :resource-paths ["resources/test"]
                   :env {:wsx-test-xml "enwiki-20150515-abstract24.xml"
                         :wsx-logger-name "wiki-search-xml-dev-logger"}}
             :test {:env {:wsx-test-xml "enwiki-20150515-abstract24.xml"
                          :wsx-logger-name "wiki-search-xml-test-logger"}}})
