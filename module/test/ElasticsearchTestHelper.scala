import java.util
import play.api.test.FakeApplication

/**
 * Helper trait for Elasticsearch Tests
 */
trait ElasticsearchTestHelper {
  val testMapping = new util.HashMap[String, String]()
  testMapping.put("testType", "{\"testType\":{\"properties\":{\"name\":{\"type\":\"string\",\"analyzer\":\"keyword\"}}}}")

  val elasticsearchAdditionalConf = Map(
    "elasticsearch.cluster.name" -> "test-cluster",
    "elasticsearch.index.name" -> "test-index",
    "elasticsearch.index.dropOnShutdown" -> true,
    "elasticsearch.index.mappings" -> testMapping
  )

  val additionalPlugins = Seq(
    "com.github.cleverage.elasticsearch.plugin.IndexPlugin"
  )

  def esFakeApp = FakeApplication(
    additionalConfiguration = elasticsearchAdditionalConf,
    additionalPlugins = additionalPlugins)

}
