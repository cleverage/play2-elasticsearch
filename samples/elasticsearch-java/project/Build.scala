import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "elasticsearch-sample"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
    )

    lazy val elasticsearchModule = RootProject(file("../../module"))

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here
    ).dependsOn(elasticsearchModule)
}