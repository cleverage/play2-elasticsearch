package com.github.cleverage.elasticsearch;

import com.github.cleverage.elasticsearch.annotations.IndexMapping;
import com.github.cleverage.elasticsearch.annotations.IndexName;
import com.github.cleverage.elasticsearch.annotations.IndexType;
import com.typesafe.config.ConfigValue;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import play.Logger;
import play.api.Configuration;
import play.api.Environment;
import play.libs.Json;
import play.libs.MyReflectionsCache;
import scala.Option;
import scala.Tuple2;
import scala.collection.Iterator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

;


/**
 * User: nboire
 *
 * example :
 * elasticsearch.local=false
 * elasticsearch.client="192.168.0.46:9300"
 * elasticsearch.cluster.name=myCluster
 * elasticsearch.index.name=play2-elasticsearch
 * elasticsearch.index.settings="{ analysis: { analyzer: { my_analyzer: { type: \"custom\", tokenizer: \"standard\" } } } }"
 * elasticsearch.index.clazzs="indexing.*"
 * elasticsearch.index.show_request=true
 */
public class IndexConfig {

    /**
     *  elasticsearch.local= true / false
     *  Mode local or network
     */
    public Boolean local = false;
    
    /**
     *  elasticsearch.client.sniff = true / false
     *  Sniff for nodes.
     */
    public Boolean sniffing = true;

    /**
     * elasticsearch.local.config = configuration file load on local mode.
     * eg : conf/elasticsearch.yml
     */
    public String localConfig = null;

    /**
     *  elasticsearch.client = list of client separate by commas ex : 192.168.0.1:9300,192.168.0.2:9300
     */
    public String client = null;

    /**
     * elasticsearch.cluster.name = name of the elasticsearch cluster
     */
    public String clusterName = null;

    /**
     * Debug mode for log search request and response
     */
    public Boolean showRequest = false;

    /**
     * The name of the index
     */
    public String[] indexNames = new String[0];

    /**
     * Custom settings to apply when creating the index. ex: "{ analysis: { analyzer: { my_analyzer: { type : "custom", tokenizer: "standard" } } } }" 
     */
    public Map<String, String> indexSettings = new HashMap<String, String>();

    /**
     * list of class extends "Index" ex: myPackage.myClass,myPackage2.*
     */
    public String indexClazzs = null;

    /**
     * List of IndexType and IndexMapping associate
     */
    public Map<IndexQueryPath, String> indexMappings = new HashMap<IndexQueryPath, String>();

    /**
     * Drop the index on application shutdown
     * Should probably be used only in tests
     */
    public boolean dropOnShutdown = false;

    /**
     * Drop the index on application shutdown
     * Should probably be used only in tests
     */
    public boolean routingReqd = false;

    /**
     * Play configuration
     */
    public Configuration configuration;

    /**
     * Play environment
     */
    public Environment environment;

    private static final Option<scala.collection.immutable.Set<String>> empty = Option.apply(new scala.collection.immutable.HashSet<>());

    public IndexConfig(Environment environment, Configuration configuration) {

        this.environment = environment;
        this.configuration = configuration;

        this.client = (configuration.getString("elasticsearch.client", empty) == Option.apply((String)null)) ? "" : configuration.getString("elasticsearch.client", empty).get();
        this.clusterName = (configuration.getString("elasticsearch.cluster.name", empty) == Option.apply((String)null)) ? "" : configuration.getString("elasticsearch.cluster.name", empty).get();
        this.indexClazzs = (configuration.getString("elasticsearch.index.clazzs", empty) == Option.apply((String)null)) ? "" : configuration.getString("elasticsearch.index.clazzs", empty).get();
        String indexNameConf = (configuration.getString("elasticsearch.index.name", empty) == Option.apply((String)null)) ? "" : configuration.getString("elasticsearch.index.name", empty).get();

        this.sniffing = (configuration.getBoolean("elasticsearch.sniff") == Option.apply(null)) ? false : (Boolean)configuration.getBoolean("elasticsearch.sniff").get();
        this.local = (configuration.getBoolean("elasticsearch.local") == Option.apply(null)) ? false : (Boolean)configuration.getBoolean("elasticsearch.local").get();
        this.localConfig = (configuration.getString("elasticsearch.config.resource", empty) == Option.apply((String)null)) ? "" : configuration.getString("elasticsearch.config.resource", empty).get();

        this.showRequest = (configuration.getBoolean("elasticsearch.index.show_request") == Option.apply(null)) ? false : (Boolean)configuration.getBoolean("elasticsearch.index.show_request").get();
        this.dropOnShutdown = (configuration.getBoolean("elasticsearch.index.dropOnShutdown") == Option.apply(null)) ? false : (Boolean)configuration.getBoolean("elasticsearch.index.dropOnShutdown").get();

        if(indexNameConf != null) {
            LinkedList<String> indexNamesL = new LinkedList<String>();
            String[] indexNamesTab = indexNameConf.split(",");
            for (String indexNameElem : indexNamesTab) {
                String indexNameTmp = indexNameElem.trim();
                indexNamesL.add(indexNameTmp);
            }
            indexNames = indexNamesL.toArray(indexNames);

            for (String indexName : indexNames) {

                // Load settings
                loadSettingsFromConfig(indexName);

                // Load Mapping from conf
                loadMappingFromConfig(indexName);
            }

        } else {
            Logger.info("ElasticSearch : no indexNames(s) defined in property 'elasticsearch.index.name'");
        }
    }

    private void loadSettingsFromConfig(String indexName) {
        String setting = (configuration.getString("elasticsearch." + indexName + ".settings", empty) == Option.apply((String)null)) ? "" : configuration.getString("elasticsearch." + indexName + ".settings", empty).get();
        if(StringUtils.isNotEmpty(setting)) {
            indexSettings.put(indexName, setting);
        }
    }

    /**
     * Load classes with @IndexType,@IndexName and initialize mapping if present on the @IndexMapping
     */
    public void loadFromAnnotations() {

        Set<String> classes = getClazzs();

        for (String aClass : classes) {
            Class<?> klass = null;
            try {
                // Loading class and annotation for set mapping if is present
                Logger.debug("ElasticSearch : Registering class " + aClass);

                klass = Class.forName(aClass, true, configuration.getClass().getClassLoader());
                Object o = klass.newInstance();

                String indexType = getIndexType(o);
                String indexMapping = getIndexMapping(o);
                String indexName = getIndexName(o, indexNames);

                if (indexType != null) {
                    IndexQueryPath path = new IndexQueryPath(indexName, indexType);
                    indexMappings.put(path, indexMapping);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Logger.error(e.getMessage());
            }
        }
    }

    /**
     * Load additional mappings from config entry "elasticsearch.index.mapping"
     * @param indexName
     */
    private void loadMappingFromConfig(String indexName) {
        Configuration mappingConfig = (configuration.getConfig("elasticsearch." + indexName + ".mappings") == Option.apply((Configuration)null)) ? null : configuration.getConfig("elasticsearch." + indexName + ".mappings").get();

        if (mappingConfig != null) {

            Iterator<Tuple2<String, ConfigValue>> iter = mappingConfig.entrySet().iterator();

            while (iter.hasNext()) {
                Tuple2<String, ConfigValue> mapping = iter.next();
                String indexType = mapping._1();
                IndexQueryPath indexQueryPath = new IndexQueryPath(indexName, indexType);

                if (mapping._2().unwrapped() instanceof String) {
                    indexMappings.put(indexQueryPath, (String) mapping._2().unwrapped());
                } else {
                    try {
                        indexMappings.put(indexQueryPath, Json.toJson(mapping._2().unwrapped()).toString());
                    } catch (Exception e) {
                        Logger.warn("Incorrect value in elasticsearch.index.mappings", e);
                    }
                }
            }
        }
    }

    private String getIndexName(Object instance, String[] indexNames) {
        IndexName indexNameAnnotation = instance.getClass().getAnnotation(IndexName.class);
        if (indexNameAnnotation == null) {
            if(indexNames.length>0) {
                return indexNames[0];
            }
            return null;
        }
        return indexNameAnnotation.name();
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

                    //TODO: remove the "play.libs.{MyClasspath,MyReflectionsCache}" local patch when we upgrade to play 2.5.0
                    Reflections reflections = MyReflectionsCache.getReflections(environment.classLoader(), load.substring(0, load.length() - 2));

                    for(Class c : reflections.getTypesAnnotatedWith(IndexName.class)){
                        classes.add(c.getName());
                    }
                    for(Class c : reflections.getTypesAnnotatedWith(IndexType.class)){
                        classes.add(c.getName());
                    }
                } else {
                    classes.add(load);
                }
            }
        }
        return classes;
    }

    @Override
    public String toString() {
        return "IndexConfig{" +
                "local=" + local +
                ", localConfig='" + localConfig + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", showRequest=" + showRequest +
                ", sniffing=" + sniffing +
                ", indexNames=" + (indexNames == null ? null : Arrays.asList(indexNames)) +
                ", indexSettings=" + indexSettings +
                ", indexClazzs='" + indexClazzs + '\'' +
                ", indexMappings=" + indexMappings +
                ", dropOnShutdown=" + dropOnShutdown +
                '}';
    }
}
