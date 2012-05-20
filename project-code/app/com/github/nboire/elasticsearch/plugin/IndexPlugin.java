package com.github.nboire.elasticsearch.plugin;

import com.github.nboire.elasticsearch.IndexClient;
import com.github.nboire.elasticsearch.IndexConfig;
import com.github.nboire.elasticsearch.IndexService;
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

    public IndexPlugin(Application application)
    {
        this.application = application;
    }

    @Override
    public void onStart()
    {
        // ElasticSearch config load from application.conf
        IndexConfig config = new IndexConfig();

        // ElasticSearch client start on local or network
        IndexClient client = new IndexClient();
        try {
            client.start();
        } catch (Exception e) {
            Logger.error(" ElasticSearch : Error when start elasticSearch Client ",e);
        }

        // Create Index If not Exists
        if (!IndexService.existsIndex()) {
            Logger.debug(" ElasticSearch : creating index " + IndexService.INDEX_DEFAULT);
            IndexService.createIndex();
        }

        // Prepare Index ( define mapping if present )
        IndexService.prepareIndex();

        Logger.info("ElasticSearch : Plugin has started");
    }

    @Override
    public void onStop()
    {
        IndexClient.client().close();

        Logger.info("ElasticSearch : Plugin has stopped");
    }
}
