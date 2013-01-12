package controllers

import play.api._
import play.api.mvc._
import indexing.{IndexTestHelper, IndexTest}
import org.elasticsearch.index.query.QueryBuilders
import com.github.cleverage.elasticsearch.Elasticsearch.IndexResults
import com.github.cleverage.elasticsearch.Elasticsearch.IndexQuery

object Application extends Controller {
  
  def index = Action {
    val indexTest = IndexTest("1", "The name", "The category")
    IndexTestHelper.index(indexTest)
    Logger.info("IndexTestHelper.index() : " + indexTest)

    val gettingIndexTest = IndexTestHelper.get("1")
    Logger.info("IndexTestHelper.get() => " + gettingIndexTest)

    IndexTestHelper.delete("1")
    Logger.info("IndexTestHelper.delete()");

    val gettingIndexTestMore = IndexTestHelper.get("1")
    Logger.info("IndexTestHelper.get() => " + gettingIndexTestMore)

    IndexTestHelper.index(IndexTest("1", "Here is the first name", "First category"))
    IndexTestHelper.index(IndexTest("2", "Then comes the second name", "First category"))
    IndexTestHelper.index(IndexTest("3", "Here is the third name", "Second category"))
    IndexTestHelper.index(IndexTest("4", "Finnaly is the fourth name", "Second category"))

    val indexQuery = IndexQuery[IndexTest]()
      .builder(QueryBuilders.matchQuery("name", "Here"))
    val results: IndexResults[IndexTest] = IndexTestHelper.search(indexQuery)

    Logger.info("IndexTestHelper.search()" + results);
    //IndexTestHelper.delete("1")
    //IndexTestHelper.delete("2")
    //IndexTestHelper.delete("3")
    //IndexTestHelper.delete("4")

    Ok(views.html.index("Your new application is ready."))
  }
  
}