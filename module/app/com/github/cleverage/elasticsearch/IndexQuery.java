package com.github.cleverage.elasticsearch;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.FacetBuilder;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import play.Logger;
import play.libs.F;
import scala.concurrent.Future;
import scala.concurrent.Promise;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An ElasticSearch query
 *
 * @param <T> extends Index
 */
public class IndexQuery<T extends Index> {

    /**
     * Objet retourné dans les résultats
     */
    private final Class<T> clazz;

    /**
     * Query searchRequestBuilder
     */
    private QueryBuilder builder = QueryBuilders.matchAllQuery();
    private String query = null;
    private List<FacetBuilder> facets = new ArrayList<FacetBuilder>();
    private List<SortBuilder> sorts = new ArrayList<SortBuilder>();

    private int from = -1;
    private int size = -1;
    private boolean explain = false;
    private boolean noField = false;
    private String preference = null;
    private String route = null;

    public IndexQuery(Class<T> clazz) {
        Validate.notNull(clazz, "clazz cannot be null");
        this.clazz = clazz;
    }

    public IndexQuery<T> setBuilder(QueryBuilder builder) {
        this.builder = builder;

        return this;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setNoField(boolean noField) {
        this.noField = noField;
    }

    /**
     * Sets from
     *
     * @param from
     *            record index to start from
     * @return self
     */
    public IndexQuery<T> from(int from) {
        this.from = from;

        return this;
    }

    /**
     * Sets fetch size
     *
     * @param size
     *            the fetch size
     * @return self
     */
    public IndexQuery<T> size(int size) {
        this.size = size;

        return this;
    }

    public IndexQuery<T> setExplain(boolean explain) {
        this.explain = explain;

        return this;
    }

    /**
     * Sets a preference of which shard replicas to execute the search request on.
     * @param preference
     *
     * @return
     */
    public IndexQuery<T> setPreference(String preference) {
        this.preference = preference;

        return this;
    }

    /**
     * Adds a facet
     *
     * @param facet
     *            the facet
     * @return self
     */
    public IndexQuery<T> addFacet(FacetBuilder facet) {
        Validate.notNull(facet, "facet cannot be null");
        facets.add(facet);

        return this;
    }

    /**
     * Sorts the result by a specific field
     *
     * @param field
     *            the sort field
     * @param order
     *            the sort order
     * @return self
     */
    public IndexQuery<T> addSort(String field, SortOrder order) {
        Validate.notEmpty(field, "field cannot be null");
        Validate.notNull(order, "order cannot be null");
        sorts.add(SortBuilders.fieldSort(field).order(order));

        return this;
    }

    /**
     * Adds a generic {@link SortBuilder}
     *
     * @param sort
     *            the sort searchRequestBuilder
     * @return self
     */
    public IndexQuery<T> addSort(SortBuilder sort) {
        Validate.notNull(sort, "sort cannot be null");
        sorts.add(sort);

        return this;
    }

    /**
     * Runs the query
     * @param indexQueryPath
     * @return
     */
    public IndexResults<T> fetch(IndexQueryPath indexQueryPath){
        return fetch(indexQueryPath, null);
    }

    /**
     * Runs the query with a filter
     * @param indexQueryPath
     * @param filter
     * @return
     */
    public IndexResults<T> fetch(IndexQueryPath indexQueryPath, FilterBuilder filter) {

        SearchRequestBuilder request = getSearchRequestBuilder(indexQueryPath, filter);
        return executeSearchRequest(request);
    }

    /**
     * Runs the query asynchronously
     * @param indexQueryPath
     * @return
     */
    public F.Promise<IndexResults<T>> fetchAsync(IndexQueryPath indexQueryPath) {
        return fetchAsync(indexQueryPath, null);
    }

    /**
     * Runs the query asynchronously with a filter
     * @param indexQueryPath
     * @param filter
     * @return
     */
    public F.Promise<IndexResults<T>> fetchAsync(IndexQueryPath indexQueryPath, FilterBuilder filter) {
        SearchRequestBuilder request = getSearchRequestBuilder(indexQueryPath, filter);
        F.Promise<SearchResponse> searchResponsePromise = AsyncUtils.executeAsyncJava(request);
        return searchResponsePromise.map(new F.Function<SearchResponse, IndexResults<T>>() {
            @Override
            public IndexResults<T> apply(SearchResponse searchResponse) throws Throwable {
                return toSearchResults(searchResponse);
            }
        });

    }

    public IndexResults<T> executeSearchRequest(SearchRequestBuilder request) {

        SearchResponse searchResponse = request.execute().actionGet();

        if (IndexClient.config.showRequest) {
            Logger.debug("ElasticSearch : Response -> " + searchResponse.toString());
        }

        IndexResults<T> searchResults = toSearchResults(searchResponse);

        return searchResults;
    }

    public SearchRequestBuilder getSearchRequestBuilder(IndexQueryPath indexQueryPath){
        return getSearchRequestBuilder(indexQueryPath, null);
    }

    public SearchRequestBuilder getSearchRequestBuilder(IndexQueryPath indexQueryPath, FilterBuilder filter) {

        // Build request
        SearchRequestBuilder request = IndexClient.client
                .prepareSearch(indexQueryPath.index)
                .setTypes(indexQueryPath.type)
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setPostFilter(filter);

        // set Query
        if (StringUtils.isNotBlank(query)) {
            request.setQuery(query);
        }
        else
        {
            request.setQuery(builder);
        }

        // set no Fields -> only return id and type
        if(noField) {
            request.setNoFields();
        }

        // Facets
        for (FacetBuilder facet : facets) {
            request.addFacet(facet);
        }

        // Sorting
        for (SortBuilder sort : sorts) {
            request.addSort(sort);
        }

        // Paging
        if (from > -1) {
            request.setFrom(from);
        }
        if (size > -1) {
            request.setSize(size);
        }

        // Explain
        if (explain) {
            request.setExplain(true);
        }

        if (preference != null) {
            request.setPreference(preference);
        }

        if(route != null){
            request.setRouting(route);
        }

        if (IndexClient.config.showRequest) {
            if (StringUtils.isNotBlank(query)) {
                Logger.debug("ElasticSearch : Query -> " + query);
            }
            else
            {
                Logger.debug("ElasticSearch : Query -> "+ builder.toString());
            }
        }
        return request;
    }

    private IndexResults<T> toSearchResults(SearchResponse searchResponse) {
        // Get Total Records Found
        long count = searchResponse.getHits().totalHits();

        // Get Facets
        Facets facetsResponse = searchResponse.getFacets();

        // Get List results
        List<T> results = new ArrayList<T>();

        // Loop on each one
        for (SearchHit h : searchResponse.getHits()) {

            // Get Data Map
            Map<String, Object> map = h.sourceAsMap();

            // Create a new Indexable Object for the return
            T objectIndexable = IndexUtils.getInstanceIndex(clazz);
            T t = (T) objectIndexable.fromIndex(map);
            t.id = h.getId();
            t.searchHit = h;

            results.add(t);
        }

        if(Logger.isDebugEnabled()) {
            Logger.debug("ElasticSearch : Results -> "+ results.toString());
        }

        // pagination
        long pageSize = 10;
        if (size > -1) {
            pageSize = size;
        }

        long pageCurrent = 1;
        if(from > 0) {
            pageCurrent = ((int) (from / pageSize))+1;
        }

        long pageNb;
        if (pageSize == 0) {
            pageNb = 1;
        } else {
            pageNb = (long)Math.ceil(new BigDecimal(count).divide(new BigDecimal(pageSize), 2, RoundingMode.HALF_UP).doubleValue());
        }

        // Return Results
        return new IndexResults<T>(count, pageSize, pageCurrent, pageNb, results, facetsResponse);
    }

}

