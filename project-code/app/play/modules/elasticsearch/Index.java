package play.modules.elasticsearch;

import org.elasticsearch.action.index.IndexResponse;
import play.Logger;
import play.modules.elasticsearch.annotations.IndexType;

/**
 * User: nboire
 * Date: 19/04/12
 */
public abstract class Index implements Indexable {

    public String id;

    /**
     * Return indexName and indexType
     * @return
     */
    public IndexQueryPath getIndexPath() {

        IndexType indexTypeAnnotation = this.getClass().getAnnotation(IndexType.class);
        if(indexTypeAnnotation == null) {
            Logger.error("ElasticSearch : Class " + this.getClass().getCanonicalName() + " no contain @IndexType(name) annotation ");
        }
        String indexType = indexTypeAnnotation.name();

        return new IndexQueryPath(IndexManager.INDEX_DEFAULT, indexType);
    }

    /**
     * Index this Document
     * @return
     * @throws Exception
     */
    public IndexResponse index() {

        return IndexManager.index(getIndexPath(), id, this);
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
        public T findById(String id) {
            return IndexManager.get(queryPath, type, id);
        }

        /**
         * Retrieves all entities of the given type.
         */
        public IndexResults<T> findAll() {
            return find(query());
        }

        /**
         * Find method
         * @param query
         * @return
         * @throws Exception
         */
        public IndexResults<T> find(IndexQuery<T> query) {

            return IndexManager.search(queryPath, query);
        }
    }


}
