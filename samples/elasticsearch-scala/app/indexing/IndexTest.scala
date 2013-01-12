package indexing

import com.github.cleverage.elasticsearch.annotations.IndexType
import com.github.cleverage.elasticsearch.Elasticsearch
import com.github.cleverage.elasticsearch.Elasticsearch.IndexableHelper


case class IndexTest(id: String, name: String, category: String) extends Elasticsearch.Indexable

object IndexTestHelper extends IndexableHelper[IndexTest] {
  import play.api.libs.json._
  import play.api.libs.json.Json._
  import play.api.libs.functional.syntax._

  val indexType = "indexTest"
  val reads: Reads[IndexTest] = Json.reads[IndexTest]
  val writes: Writes[IndexTest] = Json.writes[IndexTest]
}
