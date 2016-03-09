import com.github.cleverage.elasticsearch.ScalaHelpers._
import org.elasticsearch.index.query.{QueryBuilder, QueryBuilders}
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.Helpers._

import scala.collection.JavaConverters._

/**
 * Test of the indexing process
 */
class IndexingSpec extends Specification with ElasticsearchTestHelper {

  /**
   * Sample case class representing an Indexable class
   */
  case class SampleIndexable (id: String, title: String, count: Long, category: String) extends Indexable

  /**
   * Sample IndexableManager for managing SampleIndexable objects
   */
  object SampleIndexableManager extends IndexableManager[SampleIndexable]{
    override val index = "test-index2"
    val indexType = "sampleIndexable"
    val reads = Json.reads[SampleIndexable]
    val writes = Json.writes[SampleIndexable]
  }

  val first = SampleIndexable("1", "blabla is first title", 5, "foo category")
  val second = SampleIndexable("2", "blabla is second title", 10, "bar category")
  val third = SampleIndexable("3", "here is third title", 5, "bar category")

  val sampleIndexableMappingConf = Map("elasticsearch.test-index2.mappings" ->
    Map("sampleIndexable" ->
      """
        |{
        |  "sampleIndexable": {
        |    "properties": {
        |      "category": {
        |        "type":"string",
        |        "analyzer":"keyword"
        |      }
        |    }
        |  }
        |}
      """.stripMargin).asJava
  )

  sequential

  def search(qb: QueryBuilder) = SampleIndexableManager.search(SampleIndexableManager.query.withBuilder(qb).withSize(10))

  "IndexableManager" should {
    "not retrieve anything if nothing is indexed" in {
      running(esFakeApp) {
        waitForYellowStatus()
        SampleIndexableManager.get("1") must beNone
      }
    }
  }

  "Indexable objects" should {
    "be indexable and retrievable" in {
      running(esFakeApp) {
        waitForYellowStatus()
        val expected = SampleIndexable("1", "the title", 5, "foo category")
        SampleIndexableManager.index(expected)
        val result = SampleIndexableManager.get(expected.id)
        result must beSome.which(_.equals(expected))
      }
    }
  }

  "Indexable objects" should {
    "be indexable in bulk mode" in {
      running(esFakeApp) {
        waitForYellowStatus()
        val bulkResponse = SampleIndexableManager.indexBulk(List(first, second, third))
        bulkResponse.getItems().size must be equalTo(3)
      }
    }
  }

  "Indexable objects" should {
    "be returned by a query" in {
      running(esFakeApp) {
        waitForYellowStatus()
        SampleIndexableManager.index(List(first, second, third))
        SampleIndexableManager.refresh()
        val titleResults = search(QueryBuilders.wildcardQuery("title", "blabla"))
        titleResults.totalCount must beEqualTo(2)
        titleResults.results must containAllOf(List(first, second))

        val countResults = search(QueryBuilders.termQuery("count", 5))
        countResults.totalCount must beEqualTo(2)
        countResults.results must containAllOf(List(first, third))
        // hits should also contain 2 elements
        countResults.hits.size must beEqualTo(2)
        countResults.richResults.size must beEqualTo(2)
      }
    }
  }

  "Query pager" should {
    "compute correctly pageCurrent" in {
      running(esFakeApp) {
        waitForYellowStatus()
        SampleIndexableManager.index(first)
        SampleIndexableManager.refresh()

        val oneResult = search(QueryBuilders.termQuery("id", "1"))

        oneResult.pageCurrent must beEqualTo(1)
        oneResult.pageNb must beEqualTo(1)
      }
    }
  }

  "String field without keyword mapping" should {
    "be tokenized" in {
      running(esFakeApp) {
        waitForYellowStatus()
        SampleIndexableManager.index(List(first, second, third))
        SampleIndexableManager.refresh()
        val categoryResults = search(QueryBuilders.termQuery("category", "bar category"))
        categoryResults.totalCount must beEqualTo(0)

        val tokenCategoryResults = search(QueryBuilders.termQuery("category", "bar"))
        tokenCategoryResults.totalCount must beEqualTo(2)
        tokenCategoryResults.results must containAllOf(List(second, third))
      }
    }
  }

  "String field with keyword mapping" should {
    "not be tokenized" in {
      running(esFakeApp(sampleIndexableMappingConf)) {
        waitForYellowStatus()
        SampleIndexableManager.index(List(first, second, third))
        SampleIndexableManager.refresh()
        val categoryResults = search(QueryBuilders.termQuery("category", "bar category"))
        categoryResults.totalCount must beEqualTo(2)
        categoryResults.results must containAllOf(List(second, third))

        val tokenCategoryResults = search(QueryBuilders.termQuery("category", "bar"))
        tokenCategoryResults.totalCount must beEqualTo(0)
      }
    }
  }

}
