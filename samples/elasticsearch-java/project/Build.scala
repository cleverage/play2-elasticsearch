import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "elasticsearch-sample"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "com.github.nboire" % "elasticsearch_2.9.1" % "1.0"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here      
      resolvers += "Local Play Repository" at "file://Users/nboire/dev/java/play/play-2.0/repository/local"
      //resolvers += "Local Play Repository" at "file://path/to/play-2.0/repository/local"
       
    )

}
