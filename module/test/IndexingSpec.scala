import com.github.cleverage.elasticsearch.Elasticsearch._
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
   * Sample IndexableHelper for managing SampleIndexable objects
   */
  object SampleIndexableHelper extends IndexableHelper[SampleIndexable]{
    val indexType = "sampleIndexable"
    val reads = Json.reads[SampleIndexable]
    val writes = Json.writes[SampleIndexable]
  }

  sequential

  "IndexableHelper" should {
    "not retrieve anything if nothing is indexed" in {
      running(esFakeApp) {
        SampleIndexableHelper.get("1") must beNone
      }
    }
  }

  "Indexable objects" should {
    "be indexable and retrievable" in {
      running(esFakeApp) {
        val expected = SampleIndexable("1", "the title", 5)
        SampleIndexableHelper.index(expected)
        val result = SampleIndexableHelper.get(expected.id)
        result must beSome.which(_.equals(expected))
      }
    }
  }
}
