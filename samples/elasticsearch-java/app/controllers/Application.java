package controllers;

import com.github.cleverage.elasticsearch.IndexQuery;
import com.github.cleverage.elasticsearch.IndexResults;
import com.github.cleverage.elasticsearch.IndexService;
import indexing.IndexTest;
import indexing.Team;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregator;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import java.util.List;

public class Application extends Controller {

  public static Result index() {

    // ElasticSearch HelloWorld
    IndexTest indexTest = new IndexTest();
    // "id" is mandatory if you want to update your document or to get by id else "id" is not mandatory
    indexTest.id = "1";
    indexTest.name = "hello World";
    indexTest.index();

    IndexTest byId = IndexTest.find.byId("1");

    IndexResults<IndexTest> all = IndexTest.find.all();

    IndexQuery<IndexTest> indexQuery = IndexTest.find.query();
    indexQuery.setBuilder(QueryBuilders.queryStringQuery("hello"));

    IndexResults<IndexTest> indexResults = IndexTest.find.search(indexQuery);


    // Team indexing
    // search All
    IndexResults<Team> allTeam = Team.find.all();

    // search All + aggregation country

    IndexQuery<Team> queryCountry = Team.find.query();
    queryCountry.addAggregation(AggregationBuilders.terms("countryF").field("country.name"));
    IndexResults<Team> allAndAggregationCountry = Team.find.search(queryCountry);
    TermsAggregator countryF = allAndAggregationCountry.aggregations.get("countryF");

    // search All + aggregation players.position
    IndexQuery<Team> queryPlayers = Team.find.query();
    queryPlayers.addAggregation(AggregationBuilders.terms("playersF").field("players.position"));
    queryPlayers.addAggregation(AggregationBuilders.nested("players"));
    IndexResults<Team> allAndAggregationAge = Team.find.search(queryPlayers);


    return ok(index.render("Your new application is ready."));
  }

    public static F.Promise<Result> async() {
        // ElasticSearch HelloWorld
        IndexTest indexTest = new IndexTest();
        // "id" is mandatory if you want to update your document or to get by id else "id" is not mandatory
        indexTest.id = "1";
        indexTest.name = "hello World";
        indexTest.index();

        // ElasticSearch HelloWorld
        IndexTest indexTest2 = new IndexTest();
        // "id" is mandatory if you want to update your document or to get by id else "id" is not mandatory
        indexTest.id = "2";
        indexTest.name = "hello Bob";
        indexTest.index();

        IndexService.refresh();

        IndexQuery<IndexTest> query1 = IndexTest.find.query();
        query1.setBuilder(QueryBuilders.matchQuery("name", "hello"));
        IndexQuery<IndexTest> query2 = IndexTest.find.query();
        query2.setBuilder(QueryBuilders.matchQuery("name", "bob"));

        F.Promise<IndexResults<IndexTest>> indexResultsPromise1 = IndexTest.find.searchAsync(query1);
        F.Promise<IndexResults<IndexTest>> indexResultsPromise2 = IndexTest.find.searchAsync(query2);

        F.Promise<List<IndexResults<IndexTest>>> combinedPromise = F.Promise.sequence(indexResultsPromise1, indexResultsPromise2);
        return combinedPromise.map(new F.Function<List<IndexResults<IndexTest>>, Result>() {
                @Override
                public Result apply(List<IndexResults<IndexTest>> indexResultsList) throws Throwable {
                    return ok(indexResultsList.get(0).totalCount + " - " + indexResultsList.get(1).totalCount);
                }
            }
        );

    }

}