package com.github.cleverage.elasticsearch.component;

import com.github.cleverage.elasticsearch.IndexClient;
import com.google.inject.ImplementedBy;

@ImplementedBy(IndexComponentImpl.class)
public interface IndexComponent {

  public IndexClient getClient();
}

