import scala.collection.JavaConverters._
import play.api.test.FakeApplication

/**
 * Helper trait for Elasticsearch Tests
 */
trait ElasticsearchTestHelper {
  val testMapping = Map("testType" ->
    "{\"testType\":{\"properties\":{\"name\":{\"type\":\"string\",\"analyzer\":\"keyword\"}}}}"
  ).asJava

  val elasticsearchAdditionalConf = Map(
    "elasticsearch.local" -> true,
    "elasticsearch.cluster.name" -> "test-cluster",
    "elasticsearch.index.name" -> "test-index",
    "elasticsearch.index.dropOnShutdown" -> true,
    "elasticsearch.index.mappings" -> testMapping
  )

  val additionalPlugins = Seq(
    "com.github.cleverage.elasticsearch.plugin.IndexPlugin"
  )

  def esFakeApp(): FakeApplication = esFakeApp(Map[String, AnyRef]())

  def esFakeApp(moreConfiguration: Map[String, AnyRef]): FakeApplication = FakeApplication(
    additionalConfiguration = elasticsearchAdditionalConf ++ moreConfiguration,
    additionalPlugins = additionalPlugins
  )

}
