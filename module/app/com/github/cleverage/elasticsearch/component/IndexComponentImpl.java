package com.github.cleverage.elasticsearch.component;

import com.github.cleverage.elasticsearch.IndexClient;
import com.github.cleverage.elasticsearch.IndexService;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import play.Logger;
import play.api.Configuration;
import play.api.Environment;
import play.inject.ApplicationLifecycle;
import play.libs.F;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by mdangelo on 2/10/16.
 */
public class IndexComponentImpl implements IndexComponent {

  protected final IndexClient client;

  @Override
  public IndexClient getClient() {
    return client;
  }

  @Singleton
  @Inject
  public IndexComponentImpl(Configuration configuration, Environment environment, ApplicationLifecycle lifecycle) {

    // ElasticSearch client start on local or network
    client = new IndexClient(environment, configuration);

    // Load indexName, indexType, indexMapping from annotation
    client.config.loadFromAnnotations();

    try {
      client.start();
    } catch (Exception e) {
      Logger.error("ElasticSearch : Error when starting ElasticSearch Client ",e);
    }

    // We catch these exceptions to allow application to start even if the module start fails
    try {
      // Create Indexes and Mappings        if not Exists
        String[] indexNames = client.config.indexNames;
        for (String indexName : indexNames) {

        if (!IndexService.existsIndex(indexName)) {
          // Create index
          IndexService.createIndex(indexName);

          // Prepare Index ( define mapping if present )
          IndexService.prepareIndex(indexName);
        }
      }

      Logger.info("ElasticSearch : Plugin has started");

    } catch (NoNodeAvailableException e) {
      Logger.error("ElasticSearch : No ElasticSearch node is available. Please check that your configuration is " +
        "correct, that you ES server is up and reachable from the network. Index has not been created and prepared.", e);
    } catch (Exception e) {
      Logger.error("ElasticSearch : An unexpected exception has occurred during index preparation. Index has not been created and prepared.", e);
    }

    lifecycle.addStopHook( () -> {
      if(client!= null) {
        // Deleting index(s) if define in conf
        if (client.config.dropOnShutdown) {
          String[] indexNames = client.config.indexNames;
          for (String indexName : indexNames) {
            if(IndexService.existsIndex(indexName)) {
              IndexService.deleteIndex(indexName);
            }
          }
        }

        // Stopping the client
        try {
          client.stop();
        } catch (Exception e) {
          Logger.error("ElasticSearch : error when stop plugin ",e);
        }
      }
      Logger.info("ElasticSearch : Plugin has stopped");
      return F.Promise.pure(null);
    });
  }
}
