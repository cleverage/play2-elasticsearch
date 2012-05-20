import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "elasticsearch"
    val appVersion      = "0.1"

    val appDependencies = Seq(
      // Add your project dependencies here
      "org.elasticsearch" % "elasticsearch" % "0.19.3"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      organization := "com.github.nboire"
    )

}
