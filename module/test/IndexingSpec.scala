import com.github.cleverage.elasticsearch.Elasticsearch._
import org.elasticsearch.index.query.QueryBuilders
import org.specs2.mutable.Specification
import play.api.libs.json.{Json, Writes, Reads}
import play.api.test.Helpers._

/**
 * Test of the indexing process
 */
class IndexingSpec extends Specification with ElasticsearchTestHelper {

  /**
   * Sample case class representing an Indexable class
   */
  case class SampleIndexable (id: String, title: String, count: Long) extends Indexable

  /**
   * Sample IndexableManager for managing SampleIndexable objects
   */
  object SampleIndexableManager extends IndexableManager[SampleIndexable]{
    val indexType = "sampleIndexable"
    val reads = Json.reads[SampleIndexable]
    val writes = Json.writes[SampleIndexable]
  }

  sequential

  "IndexableManager" should {
    "not retrieve anything if nothing is indexed" in {
      running(esFakeApp) {
        SampleIndexableManager.get("1") must beNone
      }
    }
  }

  "Indexable objects" should {
    "be indexable and retrievable" in {
      running(esFakeApp) {
        val expected = SampleIndexable("1", "the title", 5)
        SampleIndexableManager.index(expected)
        val result = SampleIndexableManager.get(expected.id)
        result must beSome.which(_.equals(expected))
      }
    }
  }

  "Indexable objects" should {
    "be returned by a query" in {
      running(esFakeApp) {
        val first = SampleIndexable("1", "blabla is first title", 5)
        val second = SampleIndexable("2", "blabla is second title", 10)
        val third = SampleIndexable("3", "here is third title", 5)
        SampleIndexableManager.index(List(first, second, third))
        SampleIndexableManager.refresh()
        val titleQuery = IndexQuery[SampleIndexable]()
          .builder(QueryBuilders.wildcardQuery("title", "blabla"))
          .size(10)
        val titleResults = SampleIndexableManager.search(titleQuery)
        titleResults.totalCount must beEqualTo(2)
        titleResults.results must containAllOf(List(first, second))

        val countQuery = IndexQuery[SampleIndexable]()
          .builder(QueryBuilders.termQuery("count", 5))
          .size(10)
        val countResults = SampleIndexableManager.search(countQuery)
        countResults.totalCount must beEqualTo(2)
        countResults.results must containAllOf(List(first, third))
      }
    }
  }
}
