package play.modules.elasticsearch;

/**
 * User: nboire
 * Date: 23/04/12
 */

import org.apache.commons.lang.Validate;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.AbstractFacetBuilder;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import play.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An ElasticSearch query
 *
 * @param <T>
 *            the generic model to search for
 */
public class IndexQuery<T extends Index> {

    /**
     * Objet retourné dans les résultats
     */
    private final Class<T> clazz;

    /**
     * Query searchRequestBuilder
     */
    private final QueryBuilder builder;
    private final List<AbstractFacetBuilder> facets;
    private final List<SortBuilder> sorts;

    private int from = -1;
    private int size = -1;
    private boolean explain = false;

    public IndexQuery(Class<T> clazz, QueryBuilder builder) {
        Validate.notNull(clazz, "clazz cannot be null");
        Validate.notNull(builder, "searchRequestBuilder cannot be null");
        this.clazz = clazz;
        this.builder = builder;
        this.facets = new ArrayList<AbstractFacetBuilder>();
        this.sorts = new ArrayList<SortBuilder>();
    }

    public QueryBuilder getQueryBuilder() {
        return builder;
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

    /**
     * Runs the query
     *
     * @return the search results
     */
    public IndexResults<T> fetch(IndexQueryPath indexQueryPath) {
        // Build request
        SearchRequestBuilder request = IndexClient.client()
                .prepareSearch(indexQueryPath.index)
                .setTypes(indexQueryPath.type)
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(builder);

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

        // Todo load select fields
        if (IndexConfig.showRequest) {
            Logger.debug("ElasticSearch : Query -> "+ builder.toString());
        }

        // Execute query
        SearchResponse searchResponse = request.execute().actionGet();

        if (IndexConfig.showRequest) {
            Logger.debug("ElasticSearch : Response -> "+ searchResponse.toString());
        }

        IndexResults<T> searchResults = toSearchResults(searchResponse);

        return searchResults;
    }

    private IndexResults<T> toSearchResults(SearchResponse searchResponse) {
        // Get Total Records Found
        long count = searchResponse.hits().totalHits();

        // Get Facets
        Facets facetsResponse = searchResponse.facets();

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

            results.add(t);
        }

        if(Logger.isDebugEnabled()) {
            Logger.debug("ElasticSearch : Results -> "+ results.toString());
        }

        // Return Results
        return new IndexResults<T>(count, results, facetsResponse);
    }
}

