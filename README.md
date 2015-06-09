# Search wikipedia XML repository #
A very simple implementation of a Clojure webapp to search wiki xmls


## Bootstrap
- [reloadable-app](https://github.com/mowat27/reloadable-app)

## Framework
- [component](https://github.com/stuartsierra/component)
-  <del>[Luminus](http://www.luminusweb.net)</del>
-  <del>[Luminus Doc](http://www.luminusweb.net/docs)</del>
- [lib-noir](https://github.com/noir-clojure/lib-noir)
- [ring wiki](https://github.com/ring-clojure/ring/wiki)
- [compojure wiki](https://github.com/weavejester/compojure/wiki)
- <del>[Interactive dev](https://github.com/ring-clojure/ring/wiki/Interactive-Development)</del> -> will use ```component``` reset and ```reloadable-app```

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
<br>
- [Aleph](https://github.com/ztellman/aleph)
- [Compojure-API](https://github.com/metosin/compojure-api)

# Bootstrap

For ```couchdb``` it is necessary to create a db with:

- ```curl -X PUT http://127.0.0.1:5984/wsx```


