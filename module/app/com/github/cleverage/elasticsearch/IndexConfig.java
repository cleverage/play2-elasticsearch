package com.github.cleverage.elasticsearch;

import com.github.cleverage.elasticsearch.annotations.IndexMapping;
import com.github.cleverage.elasticsearch.annotations.IndexType;
import play.Application;
import play.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: nboire
 *
 * example :
 * elasticsearch.local=false
 * elasticsearch.client="192.168.0.46:9300"
 * elasticsearch.cluster.name=myCluster
 * elasticsearch.index.name=play2-elasticsearch
 * elasticsearch.index.clazzs="indexing.*"
 * elasticsearch.index.show_request=true
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
     * elasticsearch.cluster.name = name of the elasticsearch cluster
     */
    public static String clusterName = null;

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
    public static Map<String, String> indexTypes = new HashMap<String, String>();

    /**
     * Play application
     */
    public static Application application;

    public IndexConfig(Application app) {
        this.application = app;
        this.client = app.configuration().getString("elasticsearch.client");
        this.local = app.configuration().getBoolean("elasticsearch.local");
        this.clusterName = app.configuration().getString("elasticsearch.cluster.name");

        this.indexName = app.configuration().getString("elasticsearch.index.name");
        this.indexClazzs = app.configuration().getString("elasticsearch.index.clazzs");

        this.showRequest = app.configuration().getBoolean("elasticsearch.index.show_request");

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
                Logger.debug("ElasticSearch : Registering class " + aClass);

                klass = Class.forName(aClass, true, application.classloader());
                Object o = klass.newInstance();

                String indexType = getIndexType(o);
                String indexMapping = getIndexMapping(o);

                if (indexType != null) {
                    indexTypes.put(indexType, indexMapping);
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
                    classes.addAll(application.getTypesAnnotatedWith(load.substring(0, load.length() - 2), IndexType.class));
                } else {
                    classes.add(load);
                }
            }
        }
        return classes;
    }
}
