package com.github.nboire.elasticsearch;

import org.elasticsearch.action.admin.indices.exists.IndicesExistsRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.indices.IndexMissingException;
import play.Application;
import play.Logger;

import java.util.Map;


public abstract class IndexService {

    public static final String INDEX_DEFAULT = IndexConfig.indexName;

    /**
     * Add Indexable object in the index
     * @param indexPath
     * @param indexable
     * @return
     */
    public static IndexResponse index(IndexQueryPath indexPath, String id, Index indexable) {

        IndexResponse indexResponse = IndexClient.client.prepareIndex(INDEX_DEFAULT, indexPath.type, id)
                    .setSource(indexable.toIndex())
                    .execute()
                    .actionGet();
        if (Logger.isDebugEnabled()) {
            Logger.debug("ElasticSearch : Index : "+indexResponse.getIndex() + "/" + indexResponse.getType() + "/"+ indexResponse.getId()+" from " + indexable.toString());
        }
        return indexResponse;
    }

    /**
     * Delete element in index
     * @param indexPath
     * @return
     */
    public static DeleteResponse delete(IndexQueryPath indexPath, String id) {

        DeleteResponse deleteResponse = IndexClient.client.prepareDelete(indexPath.index, indexPath.type, id)
                .execute()
                .actionGet();

        if (Logger.isDebugEnabled()) {
            Logger.debug("ElasticSearch : Delete "+ deleteResponse.toString());
        }

        return deleteResponse;
    }

    /**
     * Get Indexable Object for an Id
     *
     * @param indexPath
     * @param clazz
     * @return
     */
    public static <T extends Index> T get(IndexQueryPath indexPath, Class<T> clazz, String id) {

        T t = IndexUtils.getInstanceIndex(clazz);

        GetResponse getResponse = IndexClient.client.prepareGet(indexPath.index, indexPath.type, id)
                .execute()
                .actionGet();

        if (!getResponse.exists()) {
            return null;
        }

        // Create a new Indexable Object for the return
        Map<String,Object> map = getResponse.sourceAsMap();

        t = (T)t.fromIndex(map);
        t.id = getResponse.getId();

        if (Logger.isDebugEnabled()) {
            Logger.debug("ElasticSearch : Get " + t.toString());
        }
        return t;
    }

    /**
     * Search information on Index from a query
     * @param indexQuery
     * @param <T>
     * @return
     */
    public static <T extends Index> IndexResults<T> search(IndexQueryPath indexPath, IndexQuery<T> indexQuery) {

        return indexQuery.fetch(indexPath);
    }


    /**
     * Test if an indice Exists
     * @return true if exists
     */
    public static boolean existsIndex() {

        Client client = IndexClient.client;
        AdminClient admin = client.admin();
        IndicesAdminClient indices = admin.indices();
        IndicesExistsRequestBuilder indicesExistsRequestBuilder = indices.prepareExists(INDEX_DEFAULT);
        IndicesExistsResponse response = indicesExistsRequestBuilder.execute().actionGet();

        return response.exists();
    }

    /**
     * Create the index
     */
    public static void createIndex() {
        try {
            IndexClient.client.admin().indices().prepareCreate(INDEX_DEFAULT).execute().actionGet();
        } catch (Exception e) {
            Logger.error("ElasticSearch : Index create error : " + e.toString());
        }
    }

    /**
     * Delete the index
     */
    public static void deleteIndex() {
        try {
            IndexClient.client.admin().indices().prepareDelete(INDEX_DEFAULT).execute().actionGet();
        } catch (IndexMissingException indexMissing) {
            Logger.debug("ElasticSearch : Index " + INDEX_DEFAULT + " no exists");
        } catch (Exception e) {
            Logger.error("ElasticSearch : Index drop error : " + e.toString());
        }
    }

    /**
     * Create Mapping ( for example mapping type : nested, geo_point  )
     * see http://www.elasticsearch.org/guide/reference/mapping/
     *
     * {
        "tweet" : {
            "properties" : {
                "message" : {"type" : "string", "store" : "yes"}
            }
        }
       }
     * @param indexType
     * @param indexMapping
     */
    public static void createMapping(String indexType, String indexMapping) {
        Logger.debug("ElasticSearch : Creating Mapping " + indexType + " :  " + indexMapping);
        PutMappingResponse response = IndexClient.client.admin().indices().preparePutMapping(IndexService.INDEX_DEFAULT).setType(indexType).setSource(indexMapping).execute().actionGet();
    }


    /**
     * Delete if exist, create index and prepareIndex
     * @param application
     */
    public static void cleanIndex(Application application) {

        Logger.debug("ElasticSearch : Clean Index starting ... ");

        if (existsIndex()) {
            deleteIndex();
        }

        createIndex();

        prepareIndex();

        Logger.debug("ElasticSearch : Clean Index ok ");
    }

    /**
     * call createMapping for list of @indexType
     */
    public static void prepareIndex() {

        Map<String, String> indexTypes = IndexConfig.indexTypes;
        for (String indexType : indexTypes.keySet()) {

            String indexMapping = indexTypes.get(indexType);
            if(indexMapping != null) {
                createMapping(indexType, indexMapping);
            }
        }
    }

    public static void cleanIndex() {

        if (IndexService.existsIndex()) {
            IndexService.deleteIndex();
        }
        IndexService.createIndex();
        IndexService.prepareIndex();
    }
    /**
     * Refresh full index
     */
    public static void refresh() {
        IndexClient.client.admin().indices().refresh(new RefreshRequest(INDEX_DEFAULT)).actionGet();
    }

    /**
     * Flush full index
     */
    public static void flush() {
        IndexClient.client.admin().indices().flush(new FlushRequest(INDEX_DEFAULT)).actionGet();
    }

}
