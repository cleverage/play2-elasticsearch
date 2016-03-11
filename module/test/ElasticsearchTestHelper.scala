import com.github.cleverage.elasticsearch.IndexClient
import com.github.cleverage.elasticsearch.component.{IndexComponent, IndexComponentImpl}
import org.elasticsearch.client.Requests
import play.api.Application
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder

import scala.collection.JavaConverters._

/**
 * Helper trait for Elasticsearch Tests
 */
trait ElasticsearchTestHelper {
  val testMapping = Map("testType" ->
    "{\"testType\":{\"properties\":{\"name\":{\"type\":\"string\",\"analyzer\":\"keyword\"}}}}"
  ).asJava

  val testMapping2 = Map("testType2" ->
    "{\"testType2\":{\"properties\":{\"name\":{\"type\":\"string\",\"analyzer\":\"keyword\"}}}}"
  ).asJava

  val elasticsearchAdditionalConf = Map(
    "elasticsearch.local" -> true,
    "elasticsearch.config.resource" -> "elasticsearch.yml",
    "elasticsearch.cluster.name" -> "test-cluster",
    "elasticsearch.index.name" -> "test-index1,test-index2",
    "elasticsearch.index.show_request" -> true,
    "elasticsearch.index.dropOnShutdown" -> true,
    "elasticsearch.index.clazzs" -> "indextype.*",
    "elasticsearch.test-index1.settings" -> "",
    "elasticsearch.test-index2.settings" -> "",
    "elasticsearch.test-index1.mappings" -> testMapping,
    "elasticsearch.test-index2.mappings" -> testMapping2
  )

  val additionalPlugins = Seq(
    "com.github.cleverage.elasticsearch.plugin.IndexPlugin"
  )
  def esFakeApp(): Application = esFakeApp(Map[String, AnyRef]())

  def esFakeApp(moreConfiguration: Map[String, AnyRef]): Application = {
    new GuiceApplicationBuilder()
      .configure(elasticsearchAdditionalConf ++ moreConfiguration)
      .overrides(
        bind[IndexComponent].to[IndexComponentImpl].eagerly()
      ).build()
  }

  def waitForYellowStatus() = IndexClient.client.admin().cluster().health(Requests.clusterHealthRequest().waitForYellowStatus()).actionGet()

}
