import play.Project._
import scala.Some
import xerial.sbt.Sonatype.SonatypeKeys._
import xerial.sbt.Sonatype._

name := "play2-elasticsearch"

version := "1.1-SNAPSHOT"

libraryDependencies ++= Seq(
  javaCore,
  // Add your project dependencies here
  "org.elasticsearch" % "elasticsearch" % "1.1.1",
  "org.apache.commons" % "commons-lang3" % "3.1"
)

play.Project.playJavaSettings

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

