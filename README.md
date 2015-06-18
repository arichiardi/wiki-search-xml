# Search wikipedia XML repository #
An implementation of a Clojure webapp to search wiki abstracts using ```core.async```.

The app leverages [Apache Commons Daemon](https://commons.apache.org/proper/commons-daemon/) and therefore can be launched with the lenghty:

- ```/usr/bin/jsvc -debug -server -cwd `pwd` -pidfile ~/tmp/wiki-search-xml-daemon.pid -user $USER -cp `pwd`/target/wiki_search_xml-0.1.0-SNAPSHOT-standalone.jar wiki_search_xml.daemon```

After having executed the classic:

- ``` lein uberjar``` or ```lein with-profile dev uberjar```

The app will be waiting for calls to its ```/search``` api on port ```7501```, like so:

- ```http://127.0.0.1:7501/search?q=blatnaya```

Finally, a repl session can be launched with:

- ```lein repl```

where:

- ```(go)``` starts up the system (network binding included)
- ```(stop)``` stops it
- ```(reset)``` refreshes the clj files and restarts

## Bootstrap
- [reloadable-app](https://github.com/mowat27/reloadable-app)

## Framework
- [component](https://github.com/stuartsierra/component)
- <del>[Luminus](http://www.luminusweb.net)</del>
- <del>[Luminus Doc](http://www.luminusweb.net/docs)</del>
- <del>[lib-noir](https://github.com/noir-clojure/lib-noir)</del>
- [ring wiki](https://github.com/ring-clojure/ring/wiki)
- [compojure wiki](https://github.com/weavejester/compojure/wiki)
- <del>[Interactive dev](https://github.com/ring-clojure/ring/wiki/Interactive-Development)</del> -> will use ```component``` reset and ```reloadable-app```
- [http-kit](http://www.http-kit.org/)

As I won't require any frontend, I went for a stable and solid framework that is based on ```ring```'s reliabilty and simplicity.
I was also tempted by [this other template](https://github.com/borkdude/lein-new-liberagent) but it looked a bit overkill for what I want to implement.

## Test Driven Development
- [Midje](https://github.com/marick/Midje)
- [Midje wiki](https://github.com/marick/Midje/wiki)
- [Midje introduction](https://github.com/marick/Midje/wiki/A-tutorial-introduction)
- [lein-midje](https://github.com/marick/lein-midje)
- <del>[Clojure-jump-to-file](https://github.com/marick/Midje/wiki/Clojure-jump-to-file)</del>
- [midje-notifier](https://github.com/glittershark/midje-notifier)

I decided to experiment TDD using ```Midje``` instead of ```clojure.test``` for two reasons: it looks more flexible and interactive (have a look at midje-notifier). 

## DB
- [Couchdb Docs](http://docs.couchdb.org/en/1.6.1)
- [Couchdb on Ubuntu](https://launchpad.net/~couchdb/+archive/ubuntu/stable)

## Other libraries (might be useful or not)
- [org.clojure/data.xml](https://github.com/clojure/data.xml)-> xml/parse is lazy and can handle efficienty huge .xml files
- [Cheshire](https://github.com/dakrone/cheshire)
- [Aleph](https://github.com/ztellman/aleph)
- [Compojure-API](https://github.com/metosin/compojure-api)




