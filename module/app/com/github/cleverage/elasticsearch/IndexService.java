package com.github.cleverage.elasticsearch;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.percolate.PercolateRequestBuilder;
import org.elasticsearch.action.percolate.PercolateResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.indices.IndexMissingException;
import org.elasticsearch.script.ScriptService;
import play.Logger;
import play.libs.F;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;


public abstract class IndexService {

    public static final String INDEX_DEFAULT = IndexClient.config.indexNames[0];
    public static final String PERCOLATOR_TYPE = ".percolator";

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
     * Create an IndexRequestBuilder
     * @param indexPath
     * @param id
     * @param indexable
     * @return
     */
    private static IndexRequestBuilder getIndexRequestBuilder(IndexQueryPath indexPath, String id, Index indexable) {
        return IndexClient.client.prepareIndex(indexPath.index, indexPath.type, id)
                .setSource(indexable.toIndex());
    }

    /**
     * Add Indexable object in the index
     *
     * @param indexPath
     * @param indexable
     * @return
     */
    public static IndexResponse index(IndexQueryPath indexPath, String id, Index indexable) {
        IndexResponse indexResponse = getIndexRequestBuilder(indexPath, id, indexable)
                .execute()
                .actionGet();
        if (Logger.isDebugEnabled()) {
            Logger.debug("ElasticSearch : Index : " + indexResponse.getIndex() + "/" + indexResponse.getType() + "/" + indexResponse.getId() + " from " + indexable.toString());
        }
        return indexResponse;
    }

    /**
     * Add Indexable object in the index asynchronously
     *
     * @param indexPath
     * @param indexable
     * @return
     */
    public static F.Promise<IndexResponse> indexAsync(IndexQueryPath indexPath, String id, Index indexable) {
        return indexAsync(getIndexRequestBuilder(indexPath, id, indexable));
    }

    /**
     * call IndexRequestBuilder on asynchronously
     * @param indexRequestBuilder
     * @return
     */
    public static F.Promise<IndexResponse> indexAsync(IndexRequestBuilder indexRequestBuilder) {
        return AsyncUtils.executeAsyncJava(indexRequestBuilder);
    }

    /**
     * Add a json document to the index
     * @param indexPath
     * @param id
     * @param json
     * @return
     */
    public static IndexResponse index(IndexQueryPath indexPath, String id, String json) {
        return getIndexRequestBuilder(indexPath, id, json).execute().actionGet();
    }

    /**
     * Create an IndexRequestBuilder for a Json-encoded object
     * @param indexPath
     * @param id
     * @param json
     * @return
     */
    public static IndexRequestBuilder getIndexRequestBuilder(IndexQueryPath indexPath, String id, String json) {
        return IndexClient.client.prepareIndex(indexPath.index, indexPath.type, id).setSource(json);
    }

    /**
     * Create a BulkRequestBuilder for a List of Index objects
     * @param indexPath
     * @param indexables
     * @return
     */
    private static BulkRequestBuilder getBulkRequestBuilder(IndexQueryPath indexPath, List<? extends Index> indexables) {
        BulkRequestBuilder bulkRequestBuilder = IndexClient.client.prepareBulk();
        for (Index indexable : indexables) {
            bulkRequestBuilder.add(Requests.indexRequest(indexPath.index)
                    .type(indexPath.type)
                    .id(indexable.id)
                    .source(indexable.toIndex()));
        }
        return bulkRequestBuilder;
    }

    /**
     * Bulk index a list of indexables
     * @param indexPath
     * @param indexables
     * @return
     */
    public static BulkResponse indexBulk(IndexQueryPath indexPath, List<? extends Index> indexables) {
        BulkRequestBuilder bulkRequestBuilder = getBulkRequestBuilder(indexPath, indexables);
        return bulkRequestBuilder.execute().actionGet();
    }

    /**
     * Bulk index a list of indexables asynchronously
     * @param indexPath
     * @param indexables
     * @return
     */
    public static F.Promise<BulkResponse> indexBulkAsync(IndexQueryPath indexPath, List<? extends Index> indexables) {
        return AsyncUtils.executeAsyncJava(getBulkRequestBuilder(indexPath, indexables));
    }

    /**
     * Create a BulkRequestBuilder for a List of json-encoded objects
     * @param indexPath
     * @param jsonMap
     * @return
     */
    public static BulkRequestBuilder getBulkRequestBuilder(IndexQueryPath indexPath, Map<String, String> jsonMap) {
        BulkRequestBuilder bulkRequestBuilder = IndexClient.client.prepareBulk();
        for (String id : jsonMap.keySet()) {
            bulkRequestBuilder.add(Requests.indexRequest(indexPath.index).type(indexPath.type).id(id).source(jsonMap.get(id)));
        }
        return bulkRequestBuilder;
    }

    /**
     * Bulk index a list of indexables asynchronously
     * @param bulkRequestBuilder
     * @return
     */
    public static F.Promise<BulkResponse> indexBulkAsync(BulkRequestBuilder bulkRequestBuilder) {
        return AsyncUtils.executeAsyncJava(bulkRequestBuilder);
    }

    /**
     * Create a BulkRequestBuilder for a List of IndexRequestBuilder
     * @return
     */
    public static BulkRequestBuilder getBulkRequestBuilder(Collection<IndexRequestBuilder> indexRequestBuilder) {
        BulkRequestBuilder bulkRequestBuilder = IndexClient.client.prepareBulk();
        for (IndexRequestBuilder requestBuilder : indexRequestBuilder) {
            bulkRequestBuilder.add(requestBuilder);
        }
        return bulkRequestBuilder;
    }

    /**
     * Bulk index a Map of json documents.
     * The id of the document is the key of the Map
     * @param indexPath
     * @param jsonMap
     * @return
     */
    public static BulkResponse indexBulk(IndexQueryPath indexPath, Map<String, String> jsonMap) {
        BulkRequestBuilder bulkRequestBuilder = getBulkRequestBuilder(indexPath, jsonMap);
        return bulkRequestBuilder.execute().actionGet();
    }

    /**
     * Create an UpdateRequestBuilder
     * @param indexPath
     * @param id
     * @return
     */
    public static UpdateRequestBuilder getUpdateRequestBuilder(IndexQueryPath indexPath,
                                                               String id,
                                                               Map<String, Object> updateFieldValues,
                                                               String updateScript) {
        return IndexClient.client.prepareUpdate(indexPath.index, indexPath.type, id)
                .setScriptParams(updateFieldValues)
                .setScript(updateScript, ScriptService.ScriptType.INLINE);
    }

    /**
     * Update a document in the index
     * @param indexPath
     * @param id
     * @param updateFieldValues The fields and new values for which the update should be done
     * @param updateScript
     * @return
     */
    public static UpdateResponse update(IndexQueryPath indexPath,
                                        String id,
                                        Map<String, Object> updateFieldValues,
                                        String updateScript) {
        return getUpdateRequestBuilder(indexPath, id, updateFieldValues, updateScript)
                .execute()
                .actionGet();
    }

    /**
     * Update a document asynchronously
     * @param indexPath
     * @param id
     * @param updateFieldValues The fields and new values for which the update should be done
     * @param updateScript
     * @return
     */
    public static F.Promise<UpdateResponse> updateAsync(IndexQueryPath indexPath,
                                                        String id,
                                                        Map<String, Object> updateFieldValues,
                                                        String updateScript) {
        return updateAsync(getUpdateRequestBuilder(indexPath, id, updateFieldValues, updateScript));
    }

    /**
     * Call update asynchronously
     * @param updateRequestBuilder
     * @return
     */
    public static F.Promise<UpdateResponse> updateAsync(UpdateRequestBuilder updateRequestBuilder) {
        return AsyncUtils.executeAsyncJava(updateRequestBuilder);
    }

    /**
     * Create a DeleteRequestBuilder
     * @param indexPath
     * @param id
     * @return
     */
    public static DeleteRequestBuilder getDeleteRequestBuilder(IndexQueryPath indexPath, String id) {
        return IndexClient.client.prepareDelete(indexPath.index, indexPath.type, id);
    }

    /**
     * Delete element in index asynchronously
     * @param indexPath
     * @return
     */
    public static F.Promise<DeleteResponse> deleteAsync(IndexQueryPath indexPath, String id) {
        return AsyncUtils.executeAsyncJava(getDeleteRequestBuilder(indexPath, id));
    }

    /**
     * Delete element in index
     * @param indexPath
     * @return
     */
    public static DeleteResponse delete(IndexQueryPath indexPath, String id) {
        DeleteResponse deleteResponse = getDeleteRequestBuilder(indexPath, id)
                .execute()
                .actionGet();

        if (Logger.isDebugEnabled()) {
            Logger.debug("ElasticSearch : Delete " + deleteResponse.toString());
        }

        return deleteResponse;
    }

    /**
     * Create a GetRequestBuilder
     * @param indexPath
     * @param id
     * @return
     */
    public static GetRequestBuilder getGetRequestBuilder(IndexQueryPath indexPath, String id) {
        return IndexClient.client.prepareGet(indexPath.index, indexPath.type, id);
    }

    /**
     * Get the json representation of a document from an id
     * @param indexPath
     * @param id
     * @return
     */
    public static String getAsString(IndexQueryPath indexPath, String id) {
        return getGetRequestBuilder(indexPath, id)
                .execute()
                .actionGet()
                .getSourceAsString();
    }

    private static <T extends Index> T getTFromGetResponse(Class<T> clazz, GetResponse getResponse) {
        T t = IndexUtils.getInstanceIndex(clazz);
        if (!getResponse.isExists()) {
            return null;
        }

        // Create a new Indexable Object for the return
        Map<String, Object> map = getResponse.getSourceAsMap();

        t = (T) t.fromIndex(map);
        t.id = getResponse.getId();
        return t;
    }

    /**
     * Get Indexable Object for an Id
     *
     * @param indexPath
     * @param clazz
     * @return
     */
    public static <T extends Index> T get(IndexQueryPath indexPath, Class<T> clazz, String id) {
        GetRequestBuilder getRequestBuilder = getGetRequestBuilder(indexPath, id);
        GetResponse getResponse = getRequestBuilder.execute().actionGet();
        return getTFromGetResponse(clazz, getResponse);
    }

    /**
     * Get Indexable Object for an Id asynchronously
     * @param indexPath
     * @param clazz
     * @param id
     * @param <T>
     * @return
     */
    public static <T extends Index> F.Promise<T> getAsync(IndexQueryPath indexPath, final Class<T> clazz, String id) {
        F.Promise<GetResponse> responsePromise = AsyncUtils.executeAsyncJava(getGetRequestBuilder(indexPath, id));
        return responsePromise.map(
            new F.Function<GetResponse, T>() {
                public T apply(GetResponse getResponse) {
                    return getTFromGetResponse(clazz, getResponse);
                }
            }
        );
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
     * Search asynchronously information on Index from a query
     * @param indexPath
     * @param indexQuery
     * @param <T>
     * @return
     */
    public static <T extends Index> F.Promise<IndexResults<T>> searchAsync(IndexQueryPath indexPath,
                                                                           IndexQuery<T> indexQuery,
                                                                           FilterBuilder filter) {
        return indexQuery.fetchAsync(indexPath, filter);
    }

    /**
     * Test if an indice Exists
     * @return true if exists
     */
    public static boolean existsIndex(String indexName) {

        Client client = IndexClient.client;
        AdminClient admin = client.admin();
        IndicesAdminClient indices = admin.indices();
        IndicesExistsRequestBuilder indicesExistsRequestBuilder = indices.prepareExists(indexName);
        IndicesExistsResponse response = indicesExistsRequestBuilder.execute().actionGet();

        return response.isExists();
    }

    /**
     * Create the index
     */
    public static void createIndex(String indexName) {
        Logger.debug("ElasticSearch : creating index [" + indexName + "]");
        try {
            CreateIndexRequestBuilder creator = IndexClient.client.admin().indices().prepareCreate(indexName);
            String setting = IndexClient.config.indexSettings.get(indexName);
            if (setting != null) {
                creator.setSettings(setting);
            }
            creator.execute().actionGet();
        } catch (Exception e) {
            Logger.error("ElasticSearch : Index create error : " + e.toString());
        }
    }

    /**
     * Delete the index
     */
    public static void deleteIndex(String indexName) {
        Logger.debug("ElasticSearch : deleting index [" + indexName + "]");
        try {
            IndexClient.client.admin().indices().prepareDelete(indexName).execute().actionGet();
        } catch (IndexMissingException indexMissing) {
            Logger.debug("ElasticSearch : Index " + indexName + " no exists");
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
     * @param indexName
     * @param indexType
     * @param indexMapping
     */
    public static PutMappingResponse createMapping(String indexName, String indexType, String indexMapping) {
        Logger.debug("ElasticSearch : creating mapping [" + indexName + "/" + indexType + "] :  " + indexMapping);
        PutMappingResponse response = IndexClient.client.admin().indices().preparePutMapping(indexName).setType(indexType).setSource(indexMapping).execute().actionGet();
        return response;
    }

    /**
     * Read the Mapping for a type
     * @param indexType
     * @return
     */
    public static String getMapping(String indexName, String indexType) {
        ClusterState state = IndexClient.client.admin().cluster()
                .prepareState()
                .setIndices(IndexService.INDEX_DEFAULT)
                .execute().actionGet().getState();
        MappingMetaData mappingMetaData = state.getMetaData().index(indexName).mapping(indexType);
        if (mappingMetaData != null) {
            return mappingMetaData.source().toString();
        } else {
            return null;
        }
    }

    /**
     * call createMapping for list of @indexType
     * @param indexName
     */
    public static void prepareIndex(String indexName) {

        Map<IndexQueryPath, String> indexMappings = IndexClient.config.indexMappings;
        for (IndexQueryPath indexQueryPath : indexMappings.keySet()) {

            if(indexName != null && indexName.equals(indexQueryPath.index)) {
                String indexType = indexQueryPath.type;
                String indexMapping = indexMappings.get(indexQueryPath);
                if (indexMapping != null) {
                    createMapping(indexName, indexType, indexMapping);
                }
            }
        }
    }

    public static void cleanIndex() {

        String[] indexNames = IndexClient.config.indexNames;
        for (String indexName : indexNames) {
            cleanIndex(indexName);
        }
    }

    public static void cleanIndex(String indexName) {

        if (IndexService.existsIndex(indexName)) {
            IndexService.deleteIndex(indexName);
        }
        IndexService.createIndex(indexName);
        IndexService.prepareIndex(indexName);
    }

    /**
     * Refresh full index
     */
    public static void refresh() {
        String[] indexNames = IndexClient.config.indexNames;
        for (String indexName : indexNames) {
            refresh(indexName);
        }
    }

    /**
     * Refresh an index
     * @param indexName
     */
    private static void refresh(String indexName) {
        IndexClient.client.admin().indices().refresh(new RefreshRequest(indexName)).actionGet();
    }

    /**
     * Flush full index
     */
    public static void flush() {
        String[] indexNames = IndexClient.config.indexNames;
        for (String indexName : indexNames) {
            flush(indexName);
        }
    }

    /**
     * Flush an index
     * @param indexName
     */
    public static void flush(String indexName) {
        IndexClient.client.admin().indices().flush(new FlushRequest(indexName)).actionGet();
    }

    /**
     * Create Percolator from a queryBuilder
     *
     * @param namePercolator
     * @param queryBuilder
     * @return
     */
    public static IndexResponse createPercolator(String namePercolator, QueryBuilder queryBuilder) {
        return createPercolator(INDEX_DEFAULT, namePercolator, queryBuilder,false);
    }

    public static IndexResponse createPercolator(String indexName, String queryName, QueryBuilder queryBuilder, boolean immediatelyAvailable) {
        XContentBuilder source = null;
        try {
            source = jsonBuilder().startObject()
                    .field("query", queryBuilder)
                    .endObject();
        } catch (IOException e) {
            Logger.error("Elasticsearch : Error when creating percolator from a queryBuilder", e);
        }

        IndexRequestBuilder percolatorRequest =
                IndexClient.client.prepareIndex(indexName,
                        PERCOLATOR_TYPE,
                        queryName)
                        .setSource(source)
                        .setRefresh(immediatelyAvailable);

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
        return createPercolator(INDEX_DEFAULT,namePercolator,query,false);
    }

    public static IndexResponse createPercolator(String indexName, String queryName, String query, boolean immediatelyAvailable) {
        IndexRequestBuilder percolatorRequest =
                IndexClient.client.prepareIndex(indexName,
                        PERCOLATOR_TYPE,
                        queryName)
                        .setSource("{\"query\": " + query + "}")
                        .setRefresh(immediatelyAvailable);

        return percolatorRequest.execute().actionGet();
    }


    /**
     * Check if a percolator exists
     * @param namePercolator
     * @return
     */
    public static boolean percolatorExists(String namePercolator) {
        return percolatorExistsInIndex(namePercolator, INDEX_DEFAULT);
    }

    public static boolean percolatorExistsInIndex(String namePercolator, String indexName){
        try {
            GetResponse responseExist = IndexService.getPercolator(indexName, namePercolator);
            return (responseExist.isExists());
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
        return deletePercolator(IndexService.INDEX_DEFAULT, namePercolator);
    }

    public static DeleteResponse deletePercolator(String indexName, String namePercolator) {
        return delete(new IndexQueryPath(indexName, PERCOLATOR_TYPE), namePercolator);
    }


    // See important notes section on http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-percolate.html
    /*/**
     * Delete all percolators
     *//*
    public static void deletePercolators() {
        try {
            DeleteIndexResponse deleteIndexResponse = IndexClient.client.admin().indices().prepareDelete(INDEX_PERCOLATOR).execute().actionGet();
            if(!deleteIndexResponse.isAcknowledged()){
                throw new Exception(" no acknowledged");
            }
        } catch (IndexMissingException indexMissing) {
            Logger.debug("ElasticSearch : Index " + INDEX_PERCOLATOR + " no exists");
        } catch (Exception e) {
            Logger.error("ElasticSearch : Index drop error : " + e.toString());
        }
    }*/

    /**
     * Get the percolator details
     * @param queryName
     * @return
     */
    public static GetResponse getPercolator(String queryName) {
        return getPercolator(INDEX_DEFAULT, queryName);
    }

    /**
     * Get the percolator details on an index
     * @param indexName
     * @param queryName
     * @return
     */
    public static GetResponse getPercolator(String indexName, String queryName){
        return get(indexName, PERCOLATOR_TYPE, queryName);
    }

    /**
     * Get percolator match this Object
     *
     * @param indexable
     * @return
     * @throws IOException
     */
    public static List<String> getPercolatorsForDoc(Index indexable) {

        PercolateRequestBuilder percolateRequestBuilder = new PercolateRequestBuilder(IndexClient.client);
        percolateRequestBuilder.setDocumentType(indexable.getIndexPath().type);

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
        List<String> matchedQueryIds = new ArrayList<String>();
        PercolateResponse.Match[] matches = percolateResponse.getMatches();
        for(PercolateResponse.Match match : matches){
            matchedQueryIds.add(match.getId().string());
        }
        return matchedQueryIds;
    }
}
