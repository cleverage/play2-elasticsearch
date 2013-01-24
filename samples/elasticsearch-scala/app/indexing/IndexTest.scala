package indexing

import com.github.cleverage.elasticsearch.Elasticsearch
import com.github.cleverage.elasticsearch.Elasticsearch.IndexableManager


case class IndexTest(id: String, name: String, category: String) extends Elasticsearch.Indexable

object IndexTestManager extends IndexableManager[IndexTest] {
  import play.api.libs.json._

  val indexType = "indexTest"
  val reads: Reads[IndexTest] = Json.reads[IndexTest]
  val writes: Writes[IndexTest] = Json.writes[IndexTest]
}
