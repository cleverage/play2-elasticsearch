import xerial.sbt.Sonatype.SonatypeKeys._
import xerial.sbt.Sonatype._

import scala.Some

name := "play2-elasticsearch"

version := "2.1-SNAPSHOT"

// DO NOT include a top-level directory in the outputting tgz file
topLevelDirectory := None

lazy val root = (project in file(".")).enablePlugins(PlayScala)

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

sonatypeSettings

organization := "com.clever-age"

profileName := "com.clever-age"

crossPaths := false

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/cleverage/play2-elasticsearch"))

testOptions in Test +=
  Tests.Argument(TestFrameworks.Specs2, "sequential", "true", "junitxml", "console")

testOptions in Test +=
  Tests.Argument(TestFrameworks.JUnit, "--ignore-runners=org.specs2.runner.JUnitRunner")

pomExtra := (
  <scm>
    <url>git@github.com:cleverage/play2-elasticsearch.git</url>
    <connection>scm:git:git@github.com:cleverage/play2-elasticsearch.git</connection>
  </scm>
    <developers>
      <developer>
        <id>nboire</id>
        <name>Nicolas Boire</name>
      </developer>
      <developer>
        <id>mguillermin</id>
        <name>Matthieu Guillermin</name>
        <url>http://matthieuguillermin.fr</url>
      </developer>
    </developers>)

