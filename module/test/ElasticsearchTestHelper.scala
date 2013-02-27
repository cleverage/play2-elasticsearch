import scala.collection.JavaConverters._
import play.api.test.FakeApplication

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
    "elasticsearch.index.dropOnShutdown" -> true,
    "elasticsearch.test-index1.settings" -> "",
    "elasticsearch.test-index2.settings" -> "",
    "elasticsearch.test-index1.mappings" -> testMapping,
    "elasticsearch.test-index2.mappings" -> testMapping2
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
