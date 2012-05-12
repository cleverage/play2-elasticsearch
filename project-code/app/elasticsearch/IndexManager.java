package elasticsearch;

import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.indices.IndexMissingException;
import play.Logger;
import play.Play;

import java.util.Map;


/**
 * User: nboire
 * Date: 19/04/12
 */
public abstract class IndexManager {

    public static final String INDEX_DEFAULT = Play.application().configuration().getString("elasticsearch.index");

    /**
     * Add Indexable object in the index
     * @param indexPath
     * @param indexable
     * @return
     */
    public static IndexResponse index(IndexPath indexPath, String id, Indexable indexable) {

        IndexResponse indexResponse = IndexClient.client().prepareIndex(INDEX_DEFAULT, indexPath.type, id)
                    .setSource(indexable.toIndex())
                    .execute()
                    .actionGet();
        if (Logger.isDebugEnabled()) {
            Logger.debug("ElasticSearch Index : "+indexResponse.getIndex() + "/" + indexResponse.getType() + "/"+ indexResponse.getId()+" from " + indexable.toString());
        }
        return indexResponse;
    }


    /**
     * Get Indexable Object for an Id
     *
     * @param indexPath
     * @param clazz
     * @return
     */
    public static <T extends Indexable> T get(IndexPath indexPath, Class<T> clazz, String id) {

        T indexable = getInstance(clazz);

        GetResponse getResponse = IndexClient.client().prepareGet(indexPath.index, indexPath.type, id)
                .execute()
                .actionGet();

        // Create a new Indexable Object for the return
        Map<String,Object> map = getResponse.sourceAsMap();

        // if not result retunr null object
        if(map == null) {
            return null;
        }

        indexable = (T)indexable.fromIndex(map);

        if (Logger.isDebugEnabled()) {
            Logger.debug("ElasticSearch Get : "+ indexable.toString());
        }

        return indexable;
    }

    private static <T extends Indexable> T getInstance(Class<T> clazz) {
        T object = null;
        try {
            object = clazz.newInstance();
        } catch (InstantiationException e) {
            Logger.error("...",e);
        } catch (IllegalAccessException e) {
            Logger.error("...",e);
        }
        return object;
    }

    /**
     * Delete element in index
     * @param indexPath
     * @return
     */
    public static DeleteResponse delete(IndexPath indexPath, String id) {

        DeleteResponse deleteResponse = IndexClient.client().prepareDelete(indexPath.index, indexPath.type, id)
                .execute()
                .actionGet();

        if (Logger.isDebugEnabled()) {
            Logger.debug("ElasticSearch Delete : "+ deleteResponse.toString());
        }

        return deleteResponse;
    }

    /**
     * Search information on Index from a query
     * @param indexQuery
     * @param <T>
     * @return
     */
    public static <T extends Indexable> IndexResults<T> search(IndexPath indexPath, IndexQuery<T> indexQuery) {

        return indexQuery.fetch(indexPath);
    }

    /**
     * Delete the index
     */
    public static void deleteAll() {
        try {
            IndexClient.client().admin().indices().prepareDelete(INDEX_DEFAULT).execute().actionGet();
        } catch (IndexMissingException indexMissing) {
            Logger.info("ElasticSearch Index " + INDEX_DEFAULT + " no exists");
        } catch (Exception e) {
            Logger.error("ElasticSearch Index drop error : " + e.toString());
        }
    }

    /**
     * Refresh full index
     */
    public static void refresh() {
        IndexClient.client().admin().indices().refresh(new RefreshRequest(INDEX_DEFAULT));
    }

    /**
     * Flush full index
     */
    public static void flush() {
        IndexClient.client().admin().indices().flush(new FlushRequest(INDEX_DEFAULT));
    }
}
