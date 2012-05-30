package com.github.cleverage.elasticsearch;

/**
 * ElasticSearch Path to request
 * curl -XGET 'http://localhost:9200/twitter/tweet/1'
 *
 *  _index : "twitter",
 "  _type" : "tweet",
 "  _id" : "1",
 *
 */
public class IndexQueryPath {

    String index;
    String type;

    public IndexQueryPath(String type) {
        this.index = IndexService.INDEX_DEFAULT;
        this.type = type;
    }

    public IndexQueryPath(String index, String type) {
        this.index = index;
        this.type = type;
    }

    @Override
    public String toString() {
        return "IndexPath{" + index + "/" + type + "}";
    }
}
