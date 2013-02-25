package com.github.cleverage.elasticsearch.plugin;

import com.github.cleverage.elasticsearch.IndexClient;
import com.github.cleverage.elasticsearch.IndexConfig;
import com.github.cleverage.elasticsearch.IndexService;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import play.Application;
import play.Logger;
import play.Plugin;

/**
 * ElasticSearch PLugin for Play 2 written in Java.
 * User: nboire
 * Date: 12/05/12
 */
public class IndexPlugin extends Plugin
{
    private final Application application;
    private IndexClient client = null;

    public IndexPlugin(Application application)
    {
        this.application = application;
    }

    @Override
    public void onStart()
    {
        // ElasticSearch config load from application.conf
        IndexConfig config = new IndexConfig(application);

        // ElasticSearch client start on local or network
        client = new IndexClient();
        try {
            client.start();
        } catch (Exception e) {
            Logger.error("ElasticSearch : Error when starting ElasticSearch Client ",e);
        }

        // We catch these exceptions to allow application to start even if the module start fails
        try {
            // Create Index and Mapping if not Exists
            if (!IndexService.existsIndex()) {
                Logger.debug("ElasticSearch : creating index " + IndexService.INDEX_DEFAULT);
                IndexService.createIndex();

                // Prepare Index ( define mapping if present )
                IndexService.prepareIndex();
            }
            Logger.info("ElasticSearch : Plugin has started");

        } catch (NoNodeAvailableException e) {
            Logger.error("ElasticSearch : No ElasticSearch node is available. Please check that your configuration is " +
                    "correct, that you ES server is up and reachable from the network. Index has not been created and prepared.", e);
        } catch (Exception e) {
            Logger.error("ElasticSearch : An unexpected exception has occurred during index preparation. Index has not been created and prepared.", e);
        }

    }

    @Override
    public void onStop()
    {
        if (IndexConfig.dropOnShutdown && IndexService.existsIndex()) {
            IndexService.deleteIndex();
        }

        if(client!= null) {
            try {
                client.stop();
            } catch (Exception e) {
                Logger.error("ElasticSearch : error when stop plugin ",e);
            }
        }
        Logger.info("ElasticSearch : Plugin has stopped");
    }
}
