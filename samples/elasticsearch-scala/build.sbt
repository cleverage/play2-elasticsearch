import PlayKeys._

name         := "elasticsearch-scala-sample"

version      := "1.0-SNAPSHOT"

lazy val esModule = RootProject(file("../../module"))

lazy val root = project.in(file(".")).enablePlugins(PlayScala).dependsOn(esModule).aggregate(esModule)
