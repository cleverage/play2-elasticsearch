package play.modules.elasticsearch;

/**
 * Classe permettant d'identifier le path a requeter dans ElasticSearch
 * curl -XGET 'http://localhost:9200/twitter/tweet/1'
 *
 * _index : "twitter",
 " _type" : "tweet",
 " _id" : "1",
 *
 * User: nboire
 * Date: 25/04/12
 */
public class IndexQueryPath {

    String index;
    String type;

    public IndexQueryPath(String type) {
        this.index = IndexManager.INDEX_DEFAULT;
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
