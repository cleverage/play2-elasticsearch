import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.Indexable;

import java.util.HashMap;
import java.util.Map;

/**
 * Class used to test Java API
 */
public class IndexableTest extends Index {

    public String name;

    public IndexableTest(String name) {
        this.name = name;
    }

    @Override
    public Map toIndex() {
        Map map = new HashMap();
        map.put("name", name);
        return map;
    }

    @Override
    public Indexable fromIndex(Map map) {
        this.name = (String)map.get("name");
        return this;
    }
}
