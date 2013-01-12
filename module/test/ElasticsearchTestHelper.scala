import play.api.test.FakeApplication

/**
 * Helper trait for Elasticsearch Tests
 */
trait ElasticsearchTestHelper {
  val elasticsearchAdditionalConf = Map(
    "elasticsearch.cluster.name" -> "test-cluster",
    "elasticsearch.index.name" -> "test-index",
    "elasticsearch.index.dropOnShutdown" -> true
  )

  val additionalPlugins = Seq(
    "com.github.cleverage.elasticsearch.plugin.IndexPlugin"
  )

  def esFakeApp = FakeApplication(
    additionalConfiguration = elasticsearchAdditionalConf,
    additionalPlugins = additionalPlugins)

}
