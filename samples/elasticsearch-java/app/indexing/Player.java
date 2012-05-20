package indexing;

import com.github.nboire.elasticsearch.Indexable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: nboire
 * Date: 18/05/12
 */
public class Player implements Indexable {

    public enum Position {
        GOALKEEPER, DEFENDER, MIDFIELDER, FORWARD
    }

    public String name;
    public Integer weight;
    public List<String> position = new ArrayList<String>();

    @Override
    public Map toIndex() {
        HashMap map = new HashMap();
        map.put("name", name);
        map.put("weight", weight);
        map.put("position", position);
        return map;
    }

    @Override
    public Indexable fromIndex(Map map) {
        if (map == null) {
            return this;
        }

        this.name = (String) map.get("name");
        this.weight = (Integer) map.get("weight");
        this.position = (List<String>) map.get("position");
        return this;
    }
}
