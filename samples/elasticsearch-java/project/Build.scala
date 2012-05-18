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
      //resolvers += "Local Play Repository" at "file://Users/nboire/dev/java/play/play-2.0/repository/local/"
      resolvers += Resolver.url("GitHub Play2-elasticsearch Repository", url("http://nboire.github.com/play2-elasticsearch/releases/"))(Resolver.ivyStylePatterns)
    )

}
