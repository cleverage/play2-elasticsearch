package controllers;

import com.github.cleverage.elasticsearch.IndexQuery;
import com.github.cleverage.elasticsearch.IndexResults;
import indexing.IndexTest;
import indexing.Team;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacet;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

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
      indexQuery.setBuilder(QueryBuilders.queryString("hello"));

      IndexResults<IndexTest> indexResults = IndexTest.find.search(indexQuery);


      // Team indexing
      // search All
      IndexResults<Team> allTeam = Team.find.all();

      // search All + facet country

      IndexQuery<Team> queryCountry = Team.find.query();
      queryCountry.addFacet(FacetBuilders.termsFacet("countryF").field("country.name"));
      IndexResults<Team> allAndFacetCountry = Team.find.search(queryCountry);
      TermsFacet countryF = allAndFacetCountry.facets.facet("countryF");

      // search All + facet players.position
      IndexQuery<Team> queryPlayers = Team.find.query();
      queryPlayers.addFacet(FacetBuilders.termsFacet("playersF").field("players.position").nested("players"));
      IndexResults<Team> allAndFacetAge = Team.find.search(queryPlayers);


      return ok(index.render("Your new application is ready."));
  }
  
}