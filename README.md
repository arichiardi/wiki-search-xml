# Search wikipedia XML repository #
A very simple implementation of a Clojure webapp to search wiki xmls


## Bootstrap
- [reloadable-app](https://github.com/mowat27/reloadable-app)

## Framework
- [Component](https://github.com/stuartsierra/component)
-  <del>[Luminus](http://www.luminusweb.net)</del>
-  <del>[Luminus Doc](http://www.luminusweb.net/docs)</del>
- [lein-ring](https://github.com/weavejester/lein-ring)
- [ring wiki](https://github.com/ring-clojure/ring/wiki)

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

## Other libraries (might be useful or not)
- <del>[XML parsing](http://clojure-doc.org/articles/tutorials/parsing_xml_with_zippers.html) -> Can it handle efficienty huge .xml files (aka, lazily/stream-like and not storing them all in memory) ? </del>
- [Cheshire](https://github.com/dakrone/cheshire)
<br>