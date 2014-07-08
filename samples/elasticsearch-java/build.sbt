import PlayKeys._

name         := "elasticsearch-java-sample"

version      := "1.0-SNAPSHOT"

lazy val esModule = RootProject(file("../../module"))

lazy val root = project.in(file(".")).enablePlugins(PlayJava).dependsOn(esModule).aggregate(esModule)
