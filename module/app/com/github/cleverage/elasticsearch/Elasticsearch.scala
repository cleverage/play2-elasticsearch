package com.github.cleverage.elasticsearch

import collection.JavaConverters._
import play.api.libs.json.{Json, Writes, Reads}
import play.api.Logger
import org.elasticsearch.search.facet.{AbstractFacetBuilder, Facets}
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.index.query.{QueryBuilders, QueryBuilder}
import org.elasticsearch.search.sort.SortBuilder
import org.elasticsearch.action.search.{SearchResponse, SearchType}

/**
 * Scala helpers
 */
object Elasticsearch {

  trait Indexable {
    def id: String
  }

  trait IndexableManager[T <: Indexable] {
    val indexType: String
    lazy val indexPath = new IndexQueryPath(indexType)

    val reads: Reads[T]
    val writes: Writes[T]

    def get(id: String): Option[T] = {
      val json = Option(IndexService.getAsString(indexPath, id))
      json.map {
        Json.parse(_).as[T](reads)
      }
    }
    def index(t: T): IndexResponse = IndexService.index(indexPath, t.id, Json.toJson(t)(writes).toString())
    def index(tSeq: Seq[T]): Seq[IndexResponse] = tSeq.map(t => IndexService.index(indexPath, t.id, Json.toJson(t)(writes).toString()))
    def delete(id: String): DeleteResponse = IndexService.delete(indexPath, id)
    def search(indexQuery: IndexQuery[T]): IndexResults[T] = indexQuery.fetch(indexPath, reads)
    def refresh() = IndexService.refresh()
    def query: IndexQuery[T] = IndexQuery[T]()

  }

  case class IndexQuery[T <: Indexable](
    val builder: QueryBuilder = QueryBuilders.matchAllQuery(),
    val facetBuilders: List[AbstractFacetBuilder] = Nil,
    val sortBuilders: List[SortBuilder] = Nil,
    val from: Option[Int] = None,
    val size: Option[Int] = None,
    val explain: Option[Boolean] = None,
    val noField: Boolean = false
  ) {
    def withBuilder(builder: QueryBuilder): IndexQuery[T] = copy(builder = builder)
    def addFacet(facet: AbstractFacetBuilder): IndexQuery[T] = copy(facetBuilders = facet :: facetBuilders)
    def addSort(sort: SortBuilder): IndexQuery[T] = copy(sortBuilders = sort :: sortBuilders)
    def withFrom(from: Int): IndexQuery[T] = copy(from = Some(from))
    def withSize(size: Int): IndexQuery[T] = copy(size = Some(size))
    def withExplain(explain: Boolean): IndexQuery[T] = copy(explain = Some(explain))
    def withNoField(noField: Boolean): IndexQuery[T] = copy(noField = noField)
    def fetch(indexPath: IndexQueryPath, reads: Reads[T]): IndexResults[T] = {
      val request = IndexClient.client.prepareSearch(indexPath.index)
        .setTypes(indexPath.`type`)
        .setSearchType(SearchType.QUERY_THEN_FETCH)
      request.setQuery(builder)
      facetBuilders.foreach { request.addFacet(_) }
      sortBuilders.foreach { request.addSort(_) }
      from.foreach { request.setFrom(_) }
      size.foreach { request.setSize(_) }
      explain.foreach { request.setExplain(_) }
      if (noField) { request.setNoFields() }
      val response = request.execute().actionGet()
      IndexResults(this, response, reads)
    }
  }

  case class IndexResults[T <: Indexable](
    totalCount: Long,
    pageSize: Long,
    pageCurrent: Long,
    pageNb: Long,
    results: List[T],
    facets: Facets)

  object IndexResults {
    def apply[T <: Indexable](indexQuery: IndexQuery[T], searchResponse: SearchResponse, reads: Reads[T]): IndexResults[T] = {
      val totalCount: Long = searchResponse.hits().totalHits()
      val pageSize: Long =
        indexQuery.size.fold(searchResponse.hits().hits().length.toLong)(_.toLong)
      new IndexResults[T](
        totalCount = totalCount,
        pageSize = pageSize,
        pageCurrent = {
          indexQuery.from.fold (1L){ f => ((f / pageSize) + 1) }
        },
        pageNb = if (pageSize == 0) 1 else math.round(math.ceil(totalCount / pageSize)),
        results = searchResponse.hits().asScala.toList.map {
          h => Json.parse(h.getSourceAsString).as[T](reads)
        },
        facets = searchResponse.facets
      )
    }
  }

}
