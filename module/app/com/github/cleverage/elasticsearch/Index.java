package com.github.cleverage.elasticsearch;

import com.github.cleverage.elasticsearch.annotations.IndexName;
import com.github.cleverage.elasticsearch.annotations.IndexType;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.search.SearchHit;
import play.Logger;
import scala.concurrent.Future;

@JsonIgnoreProperties({"searchHit"})
public abstract class Index implements Indexable {

    public String id;

    /**
     * SearchHit is use when do a search
     * It's contains information like "_index", "_type", "_id", "_score", "hightlight", ...
     */
    public SearchHit searchHit;

    /**
     * Return indexNames and indexType
     * @return
     */
    public IndexQueryPath getIndexPath() {

        IndexType indexTypeAnnotation = this.getClass().getAnnotation(IndexType.class);
        if(indexTypeAnnotation == null) {
            Logger.error("ElasticSearch : Class " + this.getClass().getCanonicalName() + " no contain @IndexType(name) annotation ");
        }
        String indexType = indexTypeAnnotation.name();

        String indexName = IndexService.INDEX_DEFAULT;
        IndexName indexNameAnnotation = this.getClass().getAnnotation(IndexName.class);
        if(indexNameAnnotation != null) {
            indexName = indexNameAnnotation.name();
        }

        return new IndexQueryPath(indexName, indexType);
    }

    /**
     * Index this Document
     * @return
     * @throws Exception
     */
    public IndexResponse index() {

        return IndexService.index(getIndexPath(), id, this);
    }

    /**
     * Delete this Document
     * @return
     * @throws Exception
     */
    public DeleteResponse delete() {

        return IndexService.delete(getIndexPath(), id);
    }


    /**
     * Helper for index queries.
     */
    public static class Finder<T extends Index> {

        private final Class<T> type;
        private IndexQueryPath queryPath;

        /**
         * Creates a finder for document of type <code>T</code>
         * @param type
         */
        public Finder(Class<T> type) {
            this.type = type;
            T t = IndexUtils.getInstanceIndex(type);
            this.queryPath = t.getIndexPath();
        }

        /**
         * Return a query for request this Index
         * @return
         */
        public IndexQuery<T> query() {
            return new IndexQuery<T>(type);
        }

        /**
         * Retrieves an entity by ID.
         * @param id
         * @return
         */
        public T byId(String id) {
            return IndexService.get(queryPath, type, id);
        }

        /**
         * Retrieves all entities of the given type.
         */
        public IndexResults<T> all() {
            return search(query());
        }

        /**
         * Find method
         * @param query
         * @return
         * @throws Exception
         */
        public IndexResults<T> search(IndexQuery<T> query) {

            return IndexService.search(queryPath, query);
        }

        public Future<IndexResults<T>> searchFuture(IndexQuery<T> query) {

            return IndexService.searchFuture(queryPath, query);
        }
    }


}
