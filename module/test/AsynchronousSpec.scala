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
    "allow parallel requests" in {
      running(esFakeApp()) {
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
  }
}
