package com.github.cleverage.elasticsearch;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.IndicesExistsRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.percolate.PercolateRequestBuilder;
import org.elasticsearch.action.percolate.PercolateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.indices.IndexMissingException;
import play.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


public abstract class IndexService {

    public static final String INDEX_DEFAULT = IndexConfig.indexName;
    public static final String INDEX_PERCOLATOR = "_percolator";

    /**
     * get indexRequest to index from a specific request
     *
     * @return
     */
    public static IndexRequestBuilder getIndexRequest(IndexQueryPath indexPath, String id, Index indexable) {
        return new IndexRequestBuilder(IndexClient.client, indexPath.index)
                .setType(indexPath.type)
                .setId(id)
                .setSource(indexable.toIndex());
    }

    /**
     * index from an request
     *
     * @param requestBuilder
     * @return
     */
    public static IndexResponse index(IndexRequestBuilder requestBuilder) {

        IndexResponse indexResponse = requestBuilder.execute().actionGet();

        if (Logger.isDebugEnabled()) {
            Logger.debug("ElasticSearch : Index " + requestBuilder.toString());
        }
        return indexResponse;
    }

    /**
     * Add Indexable object in the index
     *
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
            Logger.debug("ElasticSearch : Index : " + indexResponse.getIndex() + "/" + indexResponse.getType() + "/" + indexResponse.getId() + " from " + indexable.toString());
        }
        return indexResponse;
    }

    /**
     * Add a json document to the index
     * @param indexPath
     * @param id
     * @param json
     * @return
     */
    public static IndexResponse index(IndexQueryPath indexPath, String id, String json) {
        IndexResponse indexResponse = IndexClient.client.prepareIndex(INDEX_DEFAULT, indexPath.type, id)
                .setSource(json)
                .execute()
                .actionGet();
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
            Logger.debug("ElasticSearch : Delete " + deleteResponse.toString());
        }

        return deleteResponse;
    }

    /**
     * Get the json representation of a document from an id
     * @param indexPath
     * @param id
     * @return
     */
    public static String getAsString(IndexQueryPath indexPath, String id) {
        return IndexClient.client.prepareGet(indexPath.index, indexPath.type, id)
                .execute()
                .actionGet()
                .getSourceAsString();
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

        GetRequestBuilder getRequestBuilder = IndexClient.client.prepareGet(indexPath.index, indexPath.type, id);
        GetResponse getResponse = getRequestBuilder.execute().actionGet();

        if (!getResponse.exists()) {
            return null;
        }

        // Create a new Indexable Object for the return
        Map<String, Object> map = getResponse.sourceAsMap();

        t = (T) t.fromIndex(map);
        t.id = getResponse.getId();

        if (Logger.isDebugEnabled()) {
            Logger.debug("ElasticSearch : Get " + t.toString());
        }
        return t;
    }

    /**
     * Get a reponse for a simple request
     * @param indexName
     * @param indexType
     * @param id
     * @return
     */
    public static GetResponse get(String indexName, String indexType, String id) {

        GetRequestBuilder getRequestBuilder = IndexClient.client.prepareGet(indexName, indexType, id);
        GetResponse getResponse = getRequestBuilder.execute().actionGet();
        return getResponse;
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
            CreateIndexRequestBuilder creator = IndexClient.client.admin().indices().prepareCreate(INDEX_DEFAULT);
            if (IndexConfig.indexSettings != null) {
            	creator.setSettings(IndexConfig.indexSettings);
            }
            creator.execute().actionGet();
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
     * <p/>
     * {
     * "tweet" : {
     * "properties" : {
     * "message" : {"type" : "string", "store" : "yes"}
     * }
     * }
     * }
     *
     * @param indexType
     * @param indexMapping
     */
    public static PutMappingResponse createMapping(String indexType, String indexMapping) {
        Logger.debug("ElasticSearch : Creating Mapping " + indexType + " :  " + indexMapping);
        PutMappingResponse response = IndexClient.client.admin().indices().preparePutMapping(IndexService.INDEX_DEFAULT).setType(indexType).setSource(indexMapping).execute().actionGet();
        return response;
    }

    /**
     * call createMapping for list of @indexType
     */
    public static void prepareIndex() {

        Map<String, String> indexTypes = IndexConfig.indexTypes;
        for (String indexType : indexTypes.keySet()) {

            String indexMapping = indexTypes.get(indexType);
            if (indexMapping != null) {
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


    /**
     * Create Percolator from a queryBuilder
     *
     * @param namePercolator
     * @param queryBuilder
     * @return
     * @throws IOException
     */
    public static IndexResponse createPercolator(String namePercolator, QueryBuilder queryBuilder) {

        XContentBuilder source = null;
        try {
            source = jsonBuilder().startObject()
                    .field("query", queryBuilder)
                    .endObject();
        } catch (IOException e) {
            Logger.error("Elasticsearch : Erreur when create percolator from a queryBuilder", e);
        }

        IndexRequestBuilder percolatorRequest =
                IndexClient.client.prepareIndex(INDEX_PERCOLATOR,
                        IndexService.INDEX_DEFAULT,
                        namePercolator)
                        .setSource(source);

        return percolatorRequest.execute().actionGet();
    }

    /**
     * Create Percolator
     *
     * @param namePercolator
     * @param query
     * @return
     * @throws IOException
     */
    public static IndexResponse createPercolator(String namePercolator, String query) {

        IndexRequestBuilder percolatorRequest =
                IndexClient.client.prepareIndex(INDEX_PERCOLATOR,
                        IndexService.INDEX_DEFAULT,
                        namePercolator)
                        .setSource("{\"query\": " + query + "}");

        return percolatorRequest.execute().actionGet();
    }

    /**
     * Check if a percolator exists
     * @param namePercolator
     * @return
     */
    public static boolean precolatorExists(String namePercolator) {
        try {
            GetResponse responseExist = IndexService.getPercolator(namePercolator);
            return (responseExist.exists());
        } catch (IndexMissingException e) {
            return false;
        }
    }

    /**
     * Delete Percolator
     *
     * @param namePercolator
     * @return
     */
    public static DeleteResponse deletePercolator(String namePercolator) {
        return delete(new IndexQueryPath(INDEX_PERCOLATOR, IndexService.INDEX_DEFAULT), namePercolator);
    }

    /**
     * Delete all percolators
     */
    public static void deletePercolators() {
        try {
            DeleteIndexResponse deleteIndexResponse = IndexClient.client.admin().indices().prepareDelete(INDEX_PERCOLATOR).execute().actionGet();
            if(!deleteIndexResponse.acknowledged()){
                throw new Exception(" no acknowledged");
            }
        } catch (IndexMissingException indexMissing) {
            Logger.debug("ElasticSearch : Index " + INDEX_PERCOLATOR + " no exists");
        } catch (Exception e) {
            Logger.error("ElasticSearch : Index drop error : " + e.toString());
        }
    }

    /**
     * Get the percolator details
     * @param name
     * @return
     */
    public static GetResponse getPercolator(String name) {
        return get(INDEX_PERCOLATOR, IndexService.INDEX_DEFAULT, name);
    }

    /**
     * Get percolator match this Object
     *
     * @param indexable
     * @return
     * @throws IOException
     */
    public static List<String> getPercolatorsForDoc(Index indexable) {

        PercolateRequestBuilder percolateRequestBuilder = new PercolateRequestBuilder(IndexClient.client, indexable.getIndexPath().index, indexable.getIndexPath().type);

        XContentBuilder doc = null;
        try {
            doc = jsonBuilder().startObject().startObject("doc").startObject(indexable.getIndexPath().type);
            Map<String, Object> map = indexable.toIndex();
            for (String key : map.keySet()) {
                if (key != null && map.get(key) != null) {
                    doc.field(key, map.get(key));
                }
            }
            doc.endObject().endObject().endObject();
        } catch (Exception e) {
            Logger.debug("Elasticsearch : Error when get percolator for ");
        }

        percolateRequestBuilder.setSource(doc);

        PercolateResponse percolateResponse = percolateRequestBuilder.execute().actionGet();
        if (percolateResponse == null) {
            return null;
        }
        return percolateResponse.matches();
    }
}
