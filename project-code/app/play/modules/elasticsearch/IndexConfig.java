package play.modules.elasticsearch;

import play.Logger;
import play.Play;
import play.modules.elasticsearch.annotations.IndexMapping;
import play.modules.elasticsearch.annotations.IndexType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: nboire
 *
 * example :
 elasticsearch.local=false
 elasticsearch.client="192.168.0.46:9300"
 elasticsearch.index.name=play2-elasticsearch
 elasticsearch.index.clazzs="indexing.*"
 elasticsearch.index.show_request=true
 */
public class IndexConfig {

    /**
     *  elasticsearch.local= true / false
     *  Mode local or network
     */
    public static Boolean local = false;

    /**
     *  elasticsearch.client = list of client separate by commas ex : 192.168.0.1:9300,192.168.0.2:9300
     */
    public static String client = null;

    /**
     * Debug mode for log search request and response
     */
    public static Boolean showRequest = false;

    /**
     * The name of the index
     */
    public static String indexName = null;

    /**
     * list of class extends "Index" ex: myPackage.myClass,myPackage2.*
     */
    public static String indexClazzs = null;

    /**
     * List of IndexType and IndexMapping associate
     */
    public static Map<String, String> mappings = new HashMap<String, String>();

    public IndexConfig() {

        this.client = Play.application().configuration().getString("elasticsearch.client");
        this.local = Play.application().configuration().getBoolean("elasticsearch.local");

        this.indexName = Play.application().configuration().getString("elasticsearch.index.name");
        this.indexClazzs = Play.application().configuration().getString("elasticsearch.index.clazzs");

        this.showRequest = Play.application().configuration().getBoolean("elasticsearch.index.show_request");

        loadMapping();
    }


    /**
     * Load classes with @IndexType and initialize mapping if present on the @IndexMapping
     */
    private void loadMapping() {

        Set<String> classes = getClazzs();

        for (String aClass : classes) {
            Class<?> klass = null;
            try {
                // Loading class and annotation for set mapping if is present
                Logger.debug(" ElasticSearch : Loading -> " + aClass);

                klass = Class.forName(aClass, true, Play.application().classloader());
                Object o = klass.newInstance();

                String indexType = getIndexType(o);
                String indexMapping = getIndexMapping(o);

                if (indexType != null && indexMapping != null) {
                    mappings.put(indexType, indexMapping);
                }
            } catch (Throwable e) {
                Logger.error(e.getMessage());
            }
        }
    }

    private String getIndexType(Object instance) {
        IndexType indexTypeAnnotation = instance.getClass().getAnnotation(IndexType.class);
        if (indexTypeAnnotation == null) {
            return null;
        }
        return indexTypeAnnotation.name();
    }

    private String getIndexMapping(Object instance) {
        IndexMapping indexMapping = instance.getClass().getAnnotation(IndexMapping.class);
        if (indexMapping == null) {
            return null;
        }
        return indexMapping.value();
    }

    private Set<String> getClazzs() {

        Set<String> classes = new HashSet<String>();

        if (indexClazzs != null) {
            String[] toLoad = indexClazzs.split(",");
            for (String load : toLoad) {
                load = load.trim();
                if (load.endsWith(".*")) {
                    classes.addAll(Play.application().getTypesAnnotatedWith(load.substring(0, load.length() - 2), IndexType.class));
                } else {
                    classes.add(load);
                }
            }
        }
        return classes;
    }
}
