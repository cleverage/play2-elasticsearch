package indextype;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.IndexUtils;
import com.github.cleverage.elasticsearch.Indexable;
import com.github.cleverage.elasticsearch.annotations.IndexMapping;
import com.github.cleverage.elasticsearch.annotations.IndexName;
import com.github.cleverage.elasticsearch.annotations.IndexType;
import org.elasticsearch.common.geo.GeoPoint;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IndexName(name = "index1")
@IndexType(name = "type1")
@IndexMapping(value =
                "{" +
                    "\"type1\": {" +
                        "\"properties\": {" +
                            "\"name\": { \"type\": \"string\"}," +
                            "\"dateCreate\": { \"type\": \"date\"}," +
                            "\"category\"  : { \"type\": \"string\", \"analyzer\": \"keyword\"}," +
                            "\"location\" : {" +
                                "\"type\" : \"geo_point\", " +
                                "\"lat_lon\" : \"true\", " +
                                "\"normalize\" : \"false\"," +
                                "\"validate\" : \"true\"" +
                            "}"+
                        "}" +
                    "}" +
                "}")
public class Index1Type1 extends Index {

    public static final String NAME = "name";
    public static final String CATEGORY = "category";
    public static final String DATECREATE = "dateCreate";
    public static final String LOCATION ="location";

    public String name;
    public String category;
    public Date dateCreate;
    public GeoPoint location;

    public Index1Type1() {
    }

    public Index1Type1(String id, String name, String category, Date dateCreate) {
        this(id,name, category, dateCreate , null);
    }

    public Index1Type1(String id, String name, String category, Date dateCreate, GeoPoint location){
        this.id = id;
        this.name = name;
        this.category = category;
        this.dateCreate = dateCreate;
        this.location = location;
    }

    @Override
    public Map toIndex() {
        Map map = new HashMap();
        map.put(NAME, name);
        map.put(CATEGORY, category);
        map.put(DATECREATE, dateCreate);
        map.put(LOCATION , location);
        return map;
    }

    @Override
    public Indexable fromIndex(Map map) {
        name = (String)map.get(NAME);
        category = (String)map.get(CATEGORY);
        dateCreate = (Date) IndexUtils.convertValue(map.get(DATECREATE), Date.class);
        location = (GeoPoint) IndexUtils.convertValue(map.get(LOCATION), GeoPoint.class);
        return this;
    }

    public static Finder<Index1Type1> find = new Finder<Index1Type1>(Index1Type1.class);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Index1Type1 that = (Index1Type1) o;

        if (category != null ? !category.equals(that.category) : that.category != null) return false;
        if (dateCreate != null ? !dateCreate.equals(that.dateCreate) : that.dateCreate != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (location != null ? !location.equals(that.location) : that.location != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (dateCreate != null ? dateCreate.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }
}
