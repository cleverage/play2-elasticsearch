import com.github.cleverage.elasticsearch.ScalaHelpers.{IndexableManager, Indexable}
import play.api.libs.json.Json
import collection.JavaConverters._

/**
 * Test helpers for using a SampleIndexable class
 */
trait SampleIndexableTestHelper {

  /**
   * Sample case class representing an Indexable class
   */
  case class SampleIndexable (id: String, title: String, count: Long, category: String) extends Indexable

  /**
   * Sample IndexableManager for managing SampleIndexable objects
   */
  object SampleIndexableManager extends IndexableManager[SampleIndexable]{
    val indexType = "sampleIndexable"
    val reads = Json.reads[SampleIndexable]
    val writes = Json.writes[SampleIndexable]
  }

  val first = SampleIndexable("1", "blabla is first title", 5, "foo category")
  val second = SampleIndexable("2", "blabla is second title", 10, "bar category")
  val third = SampleIndexable("3", "here is third title", 5, "bar category")

  val sampleIndexableMappingConf = Map("elasticsearch.index.mappings" ->
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

}
