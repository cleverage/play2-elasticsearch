import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "elasticsearch"
    val appVersion      = "0.4.3"

    val appDependencies = Seq(
      javaCore,
      // Add your project dependencies here
      "org.elasticsearch" % "elasticsearch" % "0.19.10"
    )

    val main =  play.Project(appName, appVersion, appDependencies).settings(
      organization := "com.github.cleverage",
      resolvers += "oss sonytape (release)" at "http://oss.sonatype.org/content/repositories/releases/"
    )

}
