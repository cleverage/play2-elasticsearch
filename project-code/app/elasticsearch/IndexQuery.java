package elasticsearch;

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
 * An elastic search query
 *
 * @param <T>
 *            the generic model to search for
 */
public class IndexQuery<T extends Indexable> {

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
    public IndexResults<T> fetch(IndexPath indexPath) {
        // Build request
        SearchRequestBuilder request = IndexClient.client()
                .prepareSearch(indexPath.index)
                .setTypes(indexPath.type)
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

        // Only load id field for hydrate
        /*
        if (hydrate) {
            request.addField("_id");
        }
        */

        if (Logger.isDebugEnabled()) {
            Logger.debug("ES Query: "+ builder.toString());
        }

        if (Logger.isDebugEnabled()) {
            request.setExplain(true);
        }

        SearchResponse searchResponse = request.execute().actionGet();
        if (Logger.isDebugEnabled()) {
            Logger.debug("ES Response : "+ searchResponse.toString());
        }

        searchResponse.hits();

        IndexResults<T> searchResults = toSearchResults(searchResponse);

        return searchResults;
    }

    public IndexResults<T> toSearchResults(SearchResponse searchResponse) {
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
            try {
                T objectIndexable = clazz.newInstance();
                results.add((T) objectIndexable.fromIndex(map));

            } catch (InstantiationException e) {
                Logger.error("...", e);
            } catch (IllegalAccessException e) {
                Logger.error("...",e);
            }
        }

        if(Logger.isDebugEnabled()) {
            Logger.debug("Results : "+ results.toString());
        }

        // Return Results
        return new IndexResults<T>(count, results, facetsResponse);
    }
}

