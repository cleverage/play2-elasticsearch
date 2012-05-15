# play2-elasticsearch
===================

This module provides Elasticsearch in a play2 application 

## Installing

The dependency declaration is

```
"elasticsearch" % "elasticsearch_2.9.1" % "0.1"
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
      "elasticsearch" % "elasticsearch_2.9.1" % "0.1"
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
10000:elasticsearch.IndexPlugin
```
