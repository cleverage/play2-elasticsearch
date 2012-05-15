package controllers;

import elasticsearch.IndexProduct;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import java.util.Date;

public class Application extends Controller {
  
  public static Result index() {

      // test indexing a product
      IndexProduct indexProduct = new IndexProduct();
      indexProduct.id = "1";
      indexProduct.name = "name";
      indexProduct.dateCreate = new Date();
      indexProduct.price = 12;

      indexProduct.index();

      return ok(index.render("Your new application is ready."));
  }
  
}