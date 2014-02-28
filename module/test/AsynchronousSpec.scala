import com.github.cleverage.elasticsearch.IndexQuery
import concurrent.Await
import concurrent.Future
import concurrent.duration._
import org.elasticsearch.index.query.{FilterBuilders, QueryBuilders}
import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.libs.concurrent.Execution.Implicits._
import play.libs.F

/**
 * Specifications of the Asynchronous API
 */
class AsynchronousSpec extends Specification with ElasticsearchTestHelper with SampleIndexableTestHelper {
  sequential

  "Asynchronous API" should {
    "allow parallel indexing" in {
      running(esFakeApp()) {
        waitForYellowStatus()
        implicit val executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
        val future1 = SampleIndexableManager.indexAsync(first)
        val future2 = SampleIndexableManager.indexAsync(second)
        val future3 = SampleIndexableManager.indexAsync(third)

        val combinedFuture = Future.sequence(List(future1, future2, future3))
        val results = Await.result(combinedFuture, Duration(10, SECONDS))
        results.map {_.getId()} must be equalTo(List("1","2","3"))
      }
    }
    "allow parallel indexing of multiple objects" in {
      running(esFakeApp()) {
        waitForYellowStatus()
        implicit val executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
        val future = SampleIndexableManager.indexAsync(List(first, second, third))

        val results = Await.result(future, Duration(10, SECONDS))
        results.map {_.getId()} must be equalTo(List("1","2","3"))
      }
    }
    "allow parallel bulk indexing of multiple objects" in {
      running(esFakeApp()) {
        waitForYellowStatus()
        implicit val executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
        val future = SampleIndexableManager.indexBulkAsync(List(first, second, third))

        val results = Await.result(future, Duration(10, SECONDS))
        results.getItems().size must be equalTo(3)
      }
    }
    "allow parallel get" in {
      running(esFakeApp()) {
        waitForYellowStatus()
        implicit val executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
        SampleIndexableManager.index(List(first, second, third))

        val future = Future.sequence(List(
          SampleIndexableManager.getAsync("1"),
          SampleIndexableManager.getAsync("2"),
          SampleIndexableManager.getAsync("3")
        ))
        val results = Await.result(future, Duration(10, SECONDS))
        results must beEqualTo(List(first, second, third))
      }
    }
    "allow parallel delete" in {
      running(esFakeApp()) {
        waitForYellowStatus()
        implicit val executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
        SampleIndexableManager.index(List(first, second, third))

        val future1 = SampleIndexableManager.deleteAsync("1")
        val future2 = SampleIndexableManager.deleteAsync("2")
        val future3 = SampleIndexableManager.deleteAsync("3")
        val future = Future.sequence(List(future1, future2, future3))
        val results = Await.result(future, Duration(10, SECONDS))
        results.forall(_.isNotFound) must beFalse
        results.map {_.getId()} must beEqualTo(List("1", "2", "3"))
      }
    }
    /*
    TODO: Check why this test fail on travis and not locally
    "allow parallel requests" in {
      running(esFakeApp()) {
        waitForYellowStatus()
        implicit val executionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
        SampleIndexableManager.index(first)
        SampleIndexableManager.refresh()

        val futures = for (i <- 1 to 10) yield
          SampleIndexableManager.searchAsync(SampleIndexableManager.query.withBuilder(
            QueryBuilders.matchAllQuery()
          ))

        val combinedFuture = Future.sequence(futures)

        val results = Await.result(combinedFuture, Duration(10, SECONDS))
        results.forall(_.totalCount == 1) must beTrue
      }
    }
    */
  }
}
