package com.github.cleverage.elasticsearch;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.AbstractFacetBuilder;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import play.Logger;

import java.math.BigDecimal;
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
    private List<AbstractFacetBuilder> facets = new ArrayList<AbstractFacetBuilder>();
    private List<SortBuilder> sorts = new ArrayList<SortBuilder>();

    private int from = -1;
    private int size = -1;
    private boolean explain = false;
    private boolean noField = false;

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

    public int getFrom() {
        return this.from;
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

    public int getSize() {
        return this.size;
    }

    public IndexQuery<T> setExplain(boolean explain) {
        this.explain = explain;

        return this;
    }

    /**
     * Adds a facet
     *
     * @param facet
     *            the facet
     * @return self
     */
    public IndexQuery<T> addFacet(AbstractFacetBuilder facet) {
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

    public SearchResponse execute(IndexQueryPath indexQueryPath) {
        return getSearchRequestBuilder(indexQueryPath).execute().actionGet();
    }

    /**
     * Runs the query
     *
     * @return the search results
     */
    public IndexResults<T> fetch(IndexQueryPath indexQueryPath) {

        SearchRequestBuilder request = getSearchRequestBuilder(indexQueryPath);

        return executeSearchRequest(request);
    }

    public IndexResults<T> executeSearchRequest(SearchRequestBuilder request) {

        SearchResponse searchResponse = request.execute().actionGet();

        if (IndexConfig.showRequest) {
            Logger.debug("ElasticSearch : Response -> " + searchResponse.toString());
        }

        IndexResults<T> searchResults = toSearchResults(searchResponse);

        return searchResults;
    }

    public SearchRequestBuilder getSearchRequestBuilder(IndexQueryPath indexQueryPath) {

        // Build request
        SearchRequestBuilder request = IndexClient.client
                .prepareSearch(indexQueryPath.index)
                .setTypes(indexQueryPath.type)
                .setSearchType(SearchType.QUERY_THEN_FETCH);

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
        for (AbstractFacetBuilder facet : facets) {
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

        if (IndexConfig.showRequest) {
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
        long count = getCount(searchResponse);
        Facets facetsResponse = getFacets(searchResponse);

        List<T> results = buildResults(searchResponse);
        if(Logger.isDebugEnabled()) {
            Logger.debug("ElasticSearch : Results -> "+ results.toString());
        }

        long pageSize = getPageSize(size);
        long pageCurrent = getPageCurrent(from, pageSize);
        long pageNb = getPageNb(count, pageSize);

        // Return Results
        return new IndexResults<T>(count, pageSize, pageCurrent, pageNb, results, facetsResponse);
    }

    public static long getPageNb(long count, long pageSize) {
        return (long)Math.ceil(new BigDecimal(count).divide(new BigDecimal(pageSize)).doubleValue());
    }

    public static long getPageCurrent(long from, long pageSize) {
        long pageCurrent = 1;
        if(from > 0) {
            pageCurrent = ((int) (from / pageSize))+1;
        }
        return pageCurrent;
    }

    public static long getPageSize(long size) {
        // pagination
        long pageSize = 10;
        if (size > -1) {
            pageSize = size;
        }
        return pageSize;
    }

    public static Facets getFacets(SearchResponse searchResponse) {
        // Get Facets
        return searchResponse.facets();
    }

    public static long getCount(SearchResponse searchResponse) {
        // Get Total Records Found
        return searchResponse.hits().totalHits();
    }

    private List<T> buildResults(SearchResponse searchResponse) {
        // Get List results
        List<T> results = new ArrayList<T>();

        // Loop on each one
        for (SearchHit h : searchResponse.hits()) {

            // Get Data Map
            Map<String, Object> map = h.sourceAsMap();

            // Create a new Indexable Object for the return
            T objectIndexable = IndexUtils.getInstanceIndex(clazz);
            T t = (T) objectIndexable.fromIndex(map);
            t.id = h.getId();
            t.searchHit = h;

            results.add(t);
        }
        return results;
    }
}

