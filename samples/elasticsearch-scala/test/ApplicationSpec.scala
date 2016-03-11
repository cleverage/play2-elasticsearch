package test

import org.specs2.mutable._
import play.api.test.Helpers._
import play.api.test._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends Specification {

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
    
    "send 404 on a bad request" in {
      running(FakeApplication(additionalConfiguration = elasticsearchAdditionalConf)) {
        val a404 = route(FakeRequest(GET, "/boum")).get
        status(a404) must  equalTo(NOT_FOUND)
      }
    }
    
    "render the index page" in {
      running(FakeApplication(additionalConfiguration = elasticsearchAdditionalConf)) {
        val home = route(FakeRequest(GET, "/")).get
        
        status(home) must equalTo(OK)
        contentType(home) must beSome.which(_ == "text/html")
        contentAsString(home) must contain ("Your new application is ready.")
      }
    }
  }
}