import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "play2-elasticsearch"
    val appVersion      = "0.5.3"

    val appDependencies = Seq(
      javaCore,
      // Add your project dependencies here
      "org.elasticsearch" % "elasticsearch" % "0.20.5",
      "com.spatial4j" % "spatial4j" % "0.3"
    )

    val main =  play.Project(appName, appVersion, appDependencies).settings(
      organization := "com.cleverage",
      resolvers += "oss sonytape (release)" at "http://oss.sonatype.org/content/repositories/releases/",
      publishArtifact in(Compile, packageDoc) := false
    )

}
