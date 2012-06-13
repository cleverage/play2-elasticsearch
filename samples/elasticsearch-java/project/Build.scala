import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "elasticsearch-sample"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "com.github.cleverage" % "elasticsearch_2.9.1" % "0.3"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here
      //resolvers += "Local Play Repository" at "file://Users/nboire/dev/java/play/play-2.0/repository/local/"
      resolvers += Resolver.url("GitHub play2-elasticsearch Repository", url("http://cleverage.github.com/play2-elasticsearch/releases/"))(Resolver.ivyStylePatterns)
    )
}
