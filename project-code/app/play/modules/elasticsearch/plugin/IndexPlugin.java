package play.modules.elasticsearch.plugin;

import play.modules.elasticsearch.IndexClient;
import play.modules.elasticsearch.IndexConfig;
import play.modules.elasticsearch.IndexManager;
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
        if (!IndexManager.existsIndex()) {
            Logger.debug(" ElasticSearch : creating index " + IndexManager.INDEX_DEFAULT);
            IndexManager.createIndex();
        }

        // Prepare Index ( define mapping if present )
        IndexManager.prepareIndex();

        Logger.info("ElasticSearch : Plugin has started");
    }

    @Override
    public void onStop()
    {
        IndexClient.client().close();

        Logger.info("ElasticSearch : Plugin has stopped");
    }
}
