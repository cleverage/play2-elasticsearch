import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "play2-elasticsearch"
    val appVersion      = "0.7.1"

    val appDependencies = Seq(
      javaCore,
      // Add your project dependencies here
      "org.elasticsearch" % "elasticsearch" % "0.90.3",
      "org.apache.commons" % "commons-lang3" % "3.1",
      "org.codehaus.jackson" % "jackson-core-asl" % "1.6.1"
    )

    val main =  play.Project(appName, appVersion, appDependencies).settings(
      organization := "de.envisia",
      //crossPaths := false,
      //publishMavenStyle := false,
      resolvers += "oss sonytape (release)" at "http://oss.sonatype.org/content/repositories/releases/",
      publishTo := Some(Resolver.file("file", file(Option(System.getProperty("repository.path")).getOrElse("/tmp"))))
    )

}
