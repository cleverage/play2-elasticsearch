# play2-elasticsearch
===================

This module provides [Elasticsearch](http://www.elasticsearch.org/) in a play2 application

## Installing

The dependency declaration is

```
"com.github.nboire" % "elasticsearch_2.9.1" % "0.1"
```
The resolver repository is 

```
 resolvers += Resolver.url("GitHub Play2-elasticsearch Repository", url("http://nboire.github.com/play2-elasticsearch/releases/"))(Resolver.ivyStylePatterns)
```

So the Build.scala looks like 
```
import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "elasticsearch-sample"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "com.github.nboire" % "elasticsearch_2.9.1" % "0.1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here      
      resolvers += Resolver.url("GitHub Play2-elasticsearch Repository", url("http://nboire.github.com/play2-elasticsearch/releases/"))(Resolver.ivyStylePatterns)
    )

}
```


## Activate the plugin

Play2-elasticsearch requires its plugin to be declared in the conf/play.plugins file.  If this file doesn't exist (it's not created by default when you create a new project),
just create it in the conf directory first, and then add

```
9000:com.github.nboire.elasticsearch.plugin.IndexPlugin
```

## Configuration
Add settings in conf/application.conf

```
## ElasticSearch Configuration
##############################
## define local mode or not
elasticsearch.local=false

## list clients
elasticsearch.client="192.168.0.46:9300"
# ex : elasticsearch.client="192.168.0.46:9300,192.168.0.47:9300"

## Name of the index
elasticsearch.index.name=play2-elasticsearch

## define package or class separate by commas for loading @IndexType and @IndexMapping information
elasticsearch.index.clazzs="indexing.*"

## show request & result json of search request in log
elasticsearch.index.show_request=true

%test.elasticsearch.local=true
%test.elasticsearch.index.name=play2-elasticsearchtest
```

## Usage

### HelloWorld
Create an Class extends "com.github.nboire.elasticsearch.Index"

Example : [IndexTest.java](https://github.com/nboire/play2-elasticsearch/blob/master/samples/elasticsearch-java/app/indexing/IndexTest.java)

```
IndexTest indexTest = new IndexTest();
indexTest.name = "hello World";
indexTest.index();

IndexTest byId = IndexTest.find.findById("1");

IndexResults<IndexTest> all = IndexTest.find.findAll();

IndexQuery<IndexTest> indexQuery = IndexTest.find.query();
indexQuery.setBuilder(QueryBuilders.queryString("hello"));
IndexResults<IndexTest> results = IndexTest.find.find(indexQuery);

```

### More Complex
Example : https://github.com/nboire/play2-elasticsearch/blob/master/samples/elasticsearch-java/app/indexing/Team.java

See samples/elasticsearch-java application for more sample

## Javadoc
http://nboire.github.com/play2-elasticsearch/javadoc/
