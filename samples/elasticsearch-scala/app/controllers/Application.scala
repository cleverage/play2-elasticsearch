package controllers

import play.api._
import play.api.mvc._
import indexing.{IndexTestManager, IndexTest}
import org.elasticsearch.index.query.QueryBuilders
import com.github.cleverage.elasticsearch.ScalaHelpers._
import play.api.libs.concurrent.Execution.Implicits._
import com.github.cleverage.elasticsearch.ScalaHelpers.IndexQuery
import indexing.IndexTest
import concurrent.Future

object Application extends Controller {
  
  def index = Action {
    val indexTest = IndexTest("1", "The name", "The category")
    IndexTestManager.index(indexTest)
    Logger.info("IndexTestManager.index() : " + indexTest)

    val gettingIndexTest = IndexTestManager.get("1")
    Logger.info("IndexTestManager.get() => " + gettingIndexTest)

    IndexTestManager.delete("1")
    Logger.info("IndexTestManager.delete()");

    val gettingIndexTestMore = IndexTestManager.get("1")
    Logger.info("IndexTestManager.get() => " + gettingIndexTestMore)

    IndexTestManager.index(IndexTest("1", "Here is the first name", "First category"))
    IndexTestManager.index(IndexTest("2", "Then comes the second name", "First category"))
    IndexTestManager.index(IndexTest("3", "Here is the third name", "Second category"))
    IndexTestManager.index(IndexTest("4", "Finnaly is the fourth name", "Second category"))

    val indexQuery = IndexQuery[IndexTest]()
      .withBuilder(QueryBuilders.matchQuery("name", "Here"))
    val results: IndexResults[IndexTest] = IndexTestManager.search(indexQuery)

    Logger.info("IndexTestManager.search()" + results);
    //IndexTestManager.delete("1")
    //IndexTestManager.delete("2")
    //IndexTestManager.delete("3")
    //IndexTestManager.delete("4")

    Ok(views.html.index("Your new application is ready."))
  }

  def async = Action {
    IndexTestManager.index(IndexTest("1", "Here is the first name", "First category"))
    IndexTestManager.index(IndexTest("2", "Then comes the second name", "First category"))
    IndexTestManager.index(IndexTest("3", "Here is the third name", "Second category"))
    IndexTestManager.index(IndexTest("4", "Finnaly is the fourth name", "Second category"))

    IndexTestManager.refresh()

    val indexQuery = IndexTestManager.query
      .withBuilder(QueryBuilders.matchQuery("name", "Here"))
    val indexQuery2 = IndexTestManager.query
      .withBuilder(QueryBuilders.matchQuery("name", "third"))

    // Combining futures
    val l: Future[(IndexResults[IndexTest], IndexResults[IndexTest])] = for {
      result1 <- IndexTestManager.searchAsync(indexQuery)
      result2 <- IndexTestManager.searchAsync(indexQuery2)
    } yield (result1, result2)

    Async {
      l.map { case (r1, r2) =>
        Ok(r1.totalCount + " - " + r2.totalCount)
      }
    }

  }
  
}