h1. play2-elasticsearch
===================

This module provides Elasticsearch in a play2 application 

h2. Installing

The dependency declaration is

bc. "elasticsearch" % "elasticsearch_2.9.1" % "0.1"

The resolver repository is 

bc. resolvers += Resolver.url("GitHub Play2-elasticsearch Repository", url("http://nboire.github.com/play2-elasticsearch/releases/"))(Resolver.ivyStylePatterns)

h2. Activate the plugin

Play2-elasticsearch requires its plugin to be declared in the conf/play.plugins file.  If this file doesn't exist (it's not created by default when you create a new project),
just create it in the conf directory first, and then add

bc. 10000:elasticsearch.IndexPlugin

