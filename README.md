# play2-elasticsearch
===================

This module provides an easy [Elasticsearch](http://www.elasticsearch.org/)(v0.20.4) integration in a play2 application

## Versions
 * 0.1 -> initial version
 * 0.2 -> Percolators support
 * 0.3 -> IndexResult : adding pagination data
 * 0.4 -> upgrade ES to 0.19.10
 * 0.4.1 -> allow advanced query ( with highlight, .... ) 
 * 0.4.2 -> upgrade play2.0.4 + allow index settings in conf 
 
 * 0.5.0 -> compatibility with play 2.1-RC1
 * 0.5.1 -> upgrade to ES 0.20.4 - works correctly with play 2.1-RC3
 * 0.5.2 -> upgrade to play 2.1.0 - includes scala helpers
 * 0.5.3 -> upgrade to ES 0.20.5 / moving artifact's organization from "com.github.cleverage / elasticsearch" to "com.clever-age / play2-elasticsearch"

## Install

The dependency declaration is :
```
"com.clever-age" % "play2-elasticsearch" % "0.5.3"
```
or, for play 2.0 :
```
"com.github.cleverage" % "elasticsearch_2.9.1" % "0.4.2"
```

Starting from 0.5.3, the library is published on the sbt community repository. You can use the following resolvers :
```
resolvers += Resolver.url("play-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
resolvers += Resolver.url("play-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns)
```

For older versions, you should use the following resolver :
```
resolvers += Resolver.url("GitHub Play2-elasticsearch Repository", url("http://cleverage.github.com/play2-elasticsearch/releases/"))(Resolver.ivyStylePatterns)
```

So the Build.scala should look like :
```
import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "elasticsearch-sample"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "com.clever-age" % "play2-elasticsearch" % "0.5.3"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
      resolvers += Resolver.url("play-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
      resolvers += Resolver.url("play-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns)
    )

}
```
or for play 2.0 : 
```
import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "elasticsearch-sample"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "com.github.cleverage" % "elasticsearch_2.9.1" % "0.4.2"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here      
      resolvers += Resolver.url("GitHub Play2-elasticsearch Repository", url("http://cleverage.github.com/play2-elasticsearch/releases/"))(Resolver.ivyStylePatterns)
    )
}
```

## Activate the plugin

The Play2-elasticsearch module requires its plugin class to be declared in the conf/play.plugins file. If this file doesn't exist (it's not created by default when you create a new project),
just create it in the conf directory first, and then add
```
9000:com.github.cleverage.elasticsearch.plugin.IndexPlugin
```

## Configuration
You can configure the module in conf/application.conf (or in any configuration file included in your application.conf)

```
## ElasticSearch Configuration
##############################
## define local mode or not
elasticsearch.local=false

## Coma-separated list of clients
elasticsearch.client="192.168.0.46:9300"
# ex : elasticsearch.client="192.168.0.46:9300,192.168.0.47:9300"

## Name of the index
elasticsearch.index.name="play2-elasticsearch"

## Custom settings to apply when creating the index (optional)
elasticsearch.index.settings="{ analysis: { analyzer: { my_analyzer: { type: \"custom\", tokenizer: \"standard\" } } } }"

## define package or class separate by commas for loading @IndexType and @IndexMapping information
elasticsearch.index.clazzs="indexing.*"

## show request & result json of search request in log (it will be logged using Logger.debug())
elasticsearch.index.show_request=true
```

## Usage

### HelloWorld
Create a Class extending "com.github.cleverage.elasticsearch.Index"

Example : [IndexTest.java](https://github.com/cleverage/play2-elasticsearch/blob/master/samples/elasticsearch-java/app/indexing/IndexTest.java)

```
IndexTest indexTest = new IndexTest();
indexTest.name = "hello World";
indexTest.index();

IndexTest byId = IndexTest.find.byId("1");

IndexResults<IndexTest> all = IndexTest.find.all();

IndexQuery<IndexTest> indexQuery = IndexTest.find.query();
indexQuery.setBuilder(QueryBuilders.queryString("hello"));
IndexResults<IndexTest> results = IndexTest.find.search(indexQuery);

```

### More Complex
Example : https://github.com/cleverage/play2-elasticsearch/blob/master/samples/elasticsearch-java/app/indexing/Team.java

See samples/elasticsearch-java application for more sample

## Scala
Starting from version 0.5.2, Scala helpers are available (see module com.github.cleverage.elasticsearch.ScalaHelpers).

See samples/elasticsearch-scala application for a basic example

## Authors
http://twitter.com/nboire & http://twitter.com/mguillermin

## License
This code is released under the MIT License