import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "play2-elasticsearch"
    val appVersion      = "0.5.4"

    val appDependencies = Seq(
      javaCore,
      // Add your project dependencies here
      "org.elasticsearch" % "elasticsearch" % "0.20.5",
      "com.spatial4j" % "spatial4j" % "0.3"
    )

    val main =  play.Project(appName, appVersion, appDependencies).settings(
      organization := "com.clever-age",
      crossPaths := false,
      publishMavenStyle := false,
      resolvers += "oss sonytape (release)" at "http://oss.sonatype.org/content/repositories/releases/",
      publishTo <<= (version) { version: String =>
        val scalasbt = "http://repo.scala-sbt.org/scalasbt/"
        val (name, url) = if (version.contains("-SNAPSHOT"))
          ("sbt-plugin-snapshots", scalasbt+"sbt-plugin-snapshots")
        else
          ("sbt-plubin-releases", scalasbt+"sbt-plugin-releases")
        Some(Resolver.url(name, new URL(url))(Resolver.ivyStylePatterns))
      }
    )

}
