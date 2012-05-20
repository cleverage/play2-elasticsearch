package indexing;

import com.github.nboire.elasticsearch.Index;
import com.github.nboire.elasticsearch.Indexable;
import com.github.nboire.elasticsearch.annotations.IndexType;

import java.util.HashMap;
import java.util.Map;

/**
 * User: nboire
 * Date: 18/05/12
 */
@IndexType(name = "indexTest")
public class IndexTest extends Index {

    public String name;

    // Find method static for request
    public static Finder<IndexTest> find = new Finder<IndexTest>(IndexTest.class);

    @Override
    public Map toIndex() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", name);
        return map;
    }

    @Override
    public Indexable fromIndex(Map map) {
        this.name = (String) map.get("name");
        return this;
    }
}
