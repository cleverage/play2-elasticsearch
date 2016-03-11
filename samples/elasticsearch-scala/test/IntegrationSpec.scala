package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
class IntegrationSpec extends Specification {

  // TODO: why does application.conf not work?
  val elasticsearchAdditionalConf = Map(
    "elasticsearch.local" -> true,
    "elasticsearch.config.resource" -> "elasticsearch.yml",
    "elasticsearch.cluster.name" -> "test-cluster",
    "elasticsearch.index.name" -> "test-index1,test-index2",
    "elasticsearch.index.show_request" -> true,
    "elasticsearch.index.dropOnShutdown" -> true,
    "elasticsearch.index.clazzs" -> "indextype.*",
    "elasticsearch.test-index1.settings" -> "",
    "elasticsearch.test-index2.settings" -> ""
  )

  "Application" should {

    "work from within a browser" in {

      running(TestServer(3333, FakeApplication(additionalConfiguration = elasticsearchAdditionalConf)), HTMLUNIT) { browser =>

        browser.goTo("http://localhost:3333/")

        browser.pageSource must contain("Your new application is ready.")

      }
    }
    
  }
  
}
