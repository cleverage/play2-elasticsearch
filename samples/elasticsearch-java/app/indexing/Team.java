package indexing;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.IndexUtils;
import com.github.cleverage.elasticsearch.Indexable;
import com.github.cleverage.elasticsearch.annotations.IndexMapping;
import com.github.cleverage.elasticsearch.annotations.IndexType;

import java.util.*;

/**
 * User: nboire
 * Date: 20/04/12
 */
@IndexType(name = "team")
@IndexMapping(value = "{ players : { properties : { players : { type : \"nested\" }, name : { type : \"string\", analyzer : \"team_name_analyzer\" } } } }")
public class Team extends Index {

    public String name;
    public Date dateCreate;
    public String level;
    public Country country;
    public List<Player> players = new ArrayList<Player>();

    // Find method static for request
    public static Finder<Team> find = new Finder<Team>(Team.class);

    @Override
    public Map toIndex() {

        HashMap map = new HashMap();
        map.put("name", name);
        map.put("level", level);
        map.put("dateCreate", dateCreate);

        // Serialize a Indexable Object
        map.put("country", country.toIndex());

        // Serialize a List of Indexable Object
        map.put("players", IndexUtils.toIndex(players));

        return map;
    }

    @Override
    public Indexable fromIndex(Map map) {

        if (map == null) {
            return this;
        }

        this.name = (String) map.get("name");
        this.level = (String) map.get("level");
        this.dateCreate = (Date) IndexUtils.convertValue(map.get("dateCreate"), Date.class);

        // UnSerialize to a Indexable Object
        this.country = IndexUtils.getIndexable(map, "country", Country.class);

        // UnSerialize to a List<Indexable> Object
        this.players = IndexUtils.getIndexables(map, "players", Player.class);

        return this;
    }

    @Override
    public String toString() {
        return "Team{" +
                "name='" + name + '\'' +
                '}';
    }
}
