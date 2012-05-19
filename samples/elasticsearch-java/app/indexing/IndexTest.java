package indexing;

import play.modules.elasticsearch.Index;
import play.modules.elasticsearch.Indexable;
import play.modules.elasticsearch.annotations.IndexType;

import java.util.HashMap;
import java.util.Map;

/**
 * User: nboire
 * Date: 18/05/12
 */
@IndexType(name = "indexTest")
public class IndexTest extends Index {

    public String name;

    @Override
    public Map toIndex() {
        Map<String, Object> map = new HashMap<String,Object>();
        map.put("name",name);
        return map;
    }

    @Override
    public Indexable fromIndex(Map map) {
        this.name = (String)map.get("name");
        return this;
    }
}
