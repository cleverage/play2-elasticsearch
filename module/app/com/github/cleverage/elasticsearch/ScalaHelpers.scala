package com.github.cleverage.elasticsearch

import collection.JavaConverters._
import play.api.libs.json.{Json, Writes, Reads}
import org.elasticsearch.search.facet.{FacetBuilder, Facets}
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.index.query.{QueryBuilders, QueryBuilder}
import org.elasticsearch.search.sort.SortBuilder
import org.elasticsearch.search.SearchHit
import org.elasticsearch.action.search.{SearchRequestBuilder, SearchResponse, SearchType}
import concurrent.Future
import concurrent.promise
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.bulk.BulkResponse

/**
 * Scala helpers
 */
object ScalaHelpers {

  /**
   * Base trait for indexable classes
   */
  trait Indexable {
    /**
     * Id used to store/retrieve the document in Elasticsearch
     * @return
     */
    def id: String
  }

  /**
   * Base trait for Manager objects
   * It provides some high-level utilities to index/retrieve/search objects
   * @tparam T The Indexable Type on which the manager applies
   */
  trait IndexableManager[T <: Indexable] {
    /**
     * Elasticsearch type used to index objects
     */
    val indexType: String

    /**
     * Elasticsearch index used to index objects
     * Default is elasticsearch.index.name config value. Can be overriden
     */
    val index: String = IndexService.INDEX_DEFAULT;

    /**
     * IndexQueryPath used to index objects built from index and indexType
     */
    lazy val indexPath = new IndexQueryPath(index, indexType)

    /**
     * Reads used to convert a Json value to an T instance
     * You can use the standard macro to generate a default one :
     * <pre>Json.reads[MyIndexableClass]<pre>
     * or define a custom one if needed
     */
    val reads: Reads[T]

    /**
     * Writes used to convert a T instance to a Json value
     * Use the standard macro to generate a default one :
     * <pre>Json.writes[MyIndexableClass]<pre>
     * or define a custom one if needed
     */
    val writes: Writes[T]

    /**
     * Retrieve a T instance from the Elasticsearch index
     * @param id Id of the object to retrieve
     * @return the object
     */
    def get(id: String): Option[T] = {
      val json = Option(IndexService.getAsString(indexPath, id))
      json.map {
        Json.parse(_).as[T](reads)
      }
    }

    /**
     * Retrieve asynchronously a T instance from the Elasticsearch index
     * @param id
     * @return
     */
    def getAsync(id: String)(implicit executor : scala.concurrent.ExecutionContext): Future[Option[T]] = {
      val getResponseFuture = AsyncUtils.executeAsync(IndexService.getGetRequestBuilder(indexPath, id))
      getResponseFuture.map { response =>
        Option(response.getSourceAsString).map {
          Json.parse(_).as[T](reads)
        }
      }
    }

    /**
     * Index an object
     * @param t the object to index
     * @return the IndexResponse from Elasticsearch
     */
    def index(t: T): IndexResponse = IndexService.index(indexPath, t.id, Json.toJson(t)(writes).toString())

    /**
     * Index an object asynchronously
     * @param t
     * @return
     */
    def indexAsync(t: T): Future[IndexResponse] = {
      AsyncUtils.executeAsync(IndexService.getIndexRequestBuilder(indexPath, t.id, Json.toJson(t)(writes).toString()))
    }

    /**
      * Index multiple objects
     * @param tSeq a Sequence of objects to index
     * @return a Sequence of IndexResponse from Elasticsearch
     */
    def index(tSeq: Seq[T]): Seq[IndexResponse] = tSeq.map(t =>
      IndexService.index(indexPath, t.id, Json.toJson(t)(writes).toString())
    )

    /**
     * Index multiple objects asynchronously
     * @param tSeq
     * @return
     */
    def indexAsync(tSeq: Seq[T])(implicit executor : scala.concurrent.ExecutionContext): Future[Seq[IndexResponse]] = {
      Future.sequence(
        tSeq.map(t =>
          AsyncUtils.executeAsync(IndexService.getIndexRequestBuilder(indexPath, t.id, Json.toJson(t)(writes).toString()))
        )
      )
    }

    protected def createBulkMap(tSeq: Seq[T]) = {
      tSeq.map {
        t => t.id -> Json.toJson(t)(writes).toString()
      }.toMap
    }

    /**
     * Index multiple objects in bulk mode
     * @param tSeq
     * @return
     */
    def indexBulk(tSeq: Seq[T]): BulkResponse = {
      IndexService.indexBulk(indexPath, createBulkMap(tSeq).asJava)
    }


    /**
     * Index multiple objects in bulk mode asynchronously
     * @param tSeq
     * @return
     */
    def indexBulkAsync(tSeq: Seq[T]): Future[BulkResponse] = {
      AsyncUtils.executeAsync(IndexService.getBulkRequestBuilder(indexPath, createBulkMap(tSeq).asJava))
    }

    /**
     * Delete an object from the elasticsearch index
     * @param id Id of the object to delete
     * @return the DeleteResponse from Elasticsearch
     */
    def delete(id: String): DeleteResponse = IndexService.delete(indexPath, id)

    /**
     * Delete an object from the elasticsearch index asynchronously
     * @param id Id of the object to delete
     * @return the DeleteResponse from Elasticsearch
     */
    def deleteAsync(id: String): Future[DeleteResponse] = {
      AsyncUtils.executeAsync(IndexService.getDeleteRequestBuilder(indexPath, id))
    }

    /**
     * Executes a query on the Elasticsearch index
     * @param indexQuery the IndexQuery to execute
     * @return an IndexResults containing the results and associated metadata
     */
    def search(indexQuery: IndexQuery[T]): IndexResults[T] = indexQuery.fetch(indexPath, reads)

    /**
     * Executes a query on the Elasticsearch index asynchronously
     * @param indexQuery
     * @return a Future of IndexResults
     */
    def searchAsync(indexQuery: IndexQuery[T])(implicit executor : scala.concurrent.ExecutionContext): Future[IndexResults[T]] = indexQuery.fetchAsync(indexPath, reads)

    /**
     * Refresh the index
     */
    def refresh() = IndexService.refresh()

    /**
     * Initialize a query for the correct object type
     * @return a default query
     */
    def query: IndexQuery[T] = IndexQuery[T]()

  }

  /**
   * Query wrapper for scala
   * @param builder the Elasticsearch QueryBuilder to use
   * @param facetBuilders the Elasticsearch FacetBuilders to use
   * @param sortBuilders the Elasticsearch SortBuilders to use
   * @param from the first element to retrieve
   * @param size the number of element to retrieve
   * @param explain flag used to activate explain
   * @param noField flag used to activate the "noField"
   * @tparam T Type into which the results will be converted
   */
  case class IndexQuery[T <: Indexable](
    val builder: QueryBuilder = QueryBuilders.matchAllQuery(),
    val facetBuilders: List[FacetBuilder] = Nil,
    val sortBuilders: List[SortBuilder] = Nil,
    val from: Option[Int] = None,
    val size: Option[Int] = None,
    val explain: Option[Boolean] = None,
    val noField: Boolean = false
  ) {
    def withBuilder(builder: QueryBuilder): IndexQuery[T] = copy(builder = builder)
    def addFacet(facet: FacetBuilder): IndexQuery[T] = copy(facetBuilders = facet :: facetBuilders)
    def addSort(sort: SortBuilder): IndexQuery[T] = copy(sortBuilders = sortBuilders :+ sort)
    def withFrom(from: Int): IndexQuery[T] = copy(from = Some(from))
    def withSize(size: Int): IndexQuery[T] = copy(size = Some(size))
    def withExplain(explain: Boolean): IndexQuery[T] = copy(explain = Some(explain))
    def withNoField(noField: Boolean): IndexQuery[T] = copy(noField = noField)

    /**
     * Executes the query
     * @param indexPath indexPath on which we run the query
     * @param reads Reads used to convert results back
     * @return results of the query
     */
    def fetch(indexPath: IndexQueryPath, reads: Reads[T]): IndexResults[T] = {
      val request = buildRequest(indexPath)
      val response = request.execute().actionGet()
      IndexResults(this, response, reads)
    }

    /**
     * Executes the query asynchronously
     * @param indexPath
     * @param reads
     * @return
     */
    def fetchAsync(indexPath: IndexQueryPath, reads: Reads[T])(implicit executor : scala.concurrent.ExecutionContext): Future[IndexResults[T]] = {
      val request = buildRequest(indexPath)
      AsyncUtils.executeAsync(request).map {
        r => IndexResults(this, r, reads)
      }
    }

    /**
     * Build a SearchRequestBuilder from the indexQuery members
     * @param indexPath
     * @return
     */
    def buildRequest(indexPath: IndexQueryPath): SearchRequestBuilder = {
      val request = IndexClient.client.prepareSearch(indexPath.index)
        .setTypes(indexPath.`type`)
        .setSearchType(SearchType.QUERY_THEN_FETCH)
      request.setQuery(builder)
      facetBuilders.foreach {
        request.addFacet(_)
      }
      sortBuilders.foreach {
        request.addSort(_)
      }
      from.foreach {
        request.setFrom(_)
      }
      size.foreach {
        request.setSize(_)
      }
      explain.foreach {
        request.setExplain(_)
      }
      if (noField) {
        request.setNoFields()
      }
      request
    }
  }

  /**
   * Case class used to store a "Rich" result (the result with its associated SearchHit)
   * @param result
   * @param hit
   * @tparam T
   */
  case class IndexResult[T <: Indexable](result: T, hit: SearchHit)

  /**
   * Results wrapper for scala
   * @param totalCount the totalHits returned by elasticsearch
   * @param pageSize the pageSize (used to paginate results)
   * @param pageCurrent the current page
   * @param pageNb the number of pages
   * @param results List of results converted back to Indexable instances
   * @param facets List of facets
   * @tparam T Type into which the results are converted
   */
  case class IndexResults[T <: Indexable](
    totalCount: Long,
    pageSize: Long,
    pageCurrent: Long,
    pageNb: Long,
    results: List[T],
    hits: List[SearchHit],
    facets: Facets) {
    /**
     * Use this if you need the SearchHit associated with your Result
     */
    lazy val richResults: List[IndexResult[T]] = for ((result, hit) <- results.zip(hits)) yield IndexResult(result, hit)
  }

  object IndexResults {
    /**
     * Construct an IndexResults
     * @param indexQuery the indexQuery used to request Elasticsearch
     * @param searchResponse the raw Elasticsearch response
     * @param reads Reads used to convert the results back to Indexable instances
     * @tparam T Type into which the results are converted
     * @return constructed IndexResults
     */
    def apply[T <: Indexable](indexQuery: IndexQuery[T], searchResponse: SearchResponse, reads: Reads[T]): IndexResults[T] = {
      val totalCount: Long = searchResponse.getHits().totalHits()
      val pageSize: Long =
        indexQuery.size.fold(searchResponse.getHits().hits().length.toLong)(_.toLong)
      val pageCurrent: Long = indexQuery.from.fold (1L){ f => ((f / pageSize) + 1) }
      val hits = searchResponse.getHits().asScala.toList

      new IndexResults[T](
        totalCount = totalCount,
        pageSize = pageSize,
        pageCurrent = pageCurrent,
        pageNb = if (pageSize == 0) 1 else math.round(math.ceil(totalCount / pageSize.toDouble)),
        // Converting Json hits to Indexable entities
        hits = hits,
        results = hits.map {
          h => Json.parse(h.getSourceAsString).as[T](reads)
        },
        facets = searchResponse.getFacets()
      )
    }
  }

}
