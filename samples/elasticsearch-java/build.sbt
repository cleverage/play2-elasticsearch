
name         := "elasticsearch-java-sample"

version      := "1.0-SNAPSHOT"

// DO NOT include a top-level directory in the outputting tgz file
topLevelDirectory := None

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

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
// routesGenerator := InjectedRoutesGenerator

lazy val esModule = RootProject(file("../../module"))

lazy val root = project.in(file(".")).dependsOn(esModule).aggregate(esModule).enablePlugins(PlayJava)

javaOptions in Test += "-Dconfig.file=conf/application.conf"
