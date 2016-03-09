
name         := "elasticsearch-java-sample"

version      := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaCore,
  // Add your project dependencies here
  "org.elasticsearch" % "elasticsearch" % "2.1.1",
  "org.codehaus.groovy" % "groovy-all" % "2.3.8",
  "org.apache.commons" % "commons-lang3" % "3.1",
  "org.easytesting" % "fest-assert" % "1.4" % "test",
  "org.specs2" %% "specs2-core" % "3.7.2" % "test"
)

lazy val esModule = RootProject(file("../../module"))

lazy val root = project.in(file(".")).dependsOn(esModule).aggregate(esModule).enablePlugins(PlayJava)
