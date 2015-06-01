(defproject wiki_search_xml "0.1.0-SNAPSHOT"
  :description "A text search app within wikipedia xml abstract files"
  :url "https://github.com/arichiardi/wiki-search-xml"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [com.stuartsierra/component "0.2.3"]
                 [environ "1.0.0"]]
  :plugins [[lein-environ "1.0.0"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.10"]
                                  [midje "1.6.3"]
                                  [midje-notifier "0.2.0"]]
                   :source-paths ["dev"]
                   :resource-paths ["resources/test"]}
             :test {:env {:wiki-text-xml "enwiki-20150515-abstract24.xml"}}})
