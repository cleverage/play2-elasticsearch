package com.github.cleverage.elasticsearch.module;

import com.github.cleverage.elasticsearch.component.IndexComponent;
import com.github.cleverage.elasticsearch.component.IndexComponentImpl;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;


/**
 * Created by mdangelo on 2/10/16.
 */
public class IndexModule extends Module {

  public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
    return seq(
      bind(IndexComponent.class).to(IndexComponentImpl.class).eagerly()
    );
  }
}
