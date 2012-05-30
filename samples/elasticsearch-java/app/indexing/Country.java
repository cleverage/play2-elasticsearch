package indexing;

import com.github.cleverage.elasticsearch.Indexable;

import java.util.HashMap;
import java.util.Map;

/**
 * User: nboire
 * Date: 18/05/12
 */
public class Country implements Indexable {
    public String name;
    public String continent;

    @Override
    public Map toIndex() {
        HashMap map = new HashMap();
        map.put("name", name);
        map.put("continent", continent);
        return map;
    }

    @Override
    public Indexable fromIndex(Map map) {
        if (map == null) {
            return this;
        }

        this.name = (String) map.get("name");
        this.continent = (String) map.get("continent");
        return this;
    }
}
