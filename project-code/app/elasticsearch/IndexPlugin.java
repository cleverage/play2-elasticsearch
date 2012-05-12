package elasticsearch;

import play.Application;
import play.Configuration;
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
        Configuration configuration = application.configuration();
        // you can now access the application.conf settings, including any custom ones you have added

        IndexClient client = new IndexClient();
        try {
            client.start();
        } catch (Exception e) {
            Logger.error(" IndexPlugin : error when start elasticSearch Client ",e);
        }

        Logger.info("IndexPlugin has started");
    }

    @Override
    public void onStop()
    {
        // you may want to tidy up resources here
        Logger.info("IndexPlugin has stopped");
    }
}