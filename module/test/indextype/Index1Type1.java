package indextype;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.IndexUtils;
import com.github.cleverage.elasticsearch.Indexable;
import com.github.cleverage.elasticsearch.annotations.IndexMapping;
import com.github.cleverage.elasticsearch.annotations.IndexName;
import com.github.cleverage.elasticsearch.annotations.IndexType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IndexName(name = "index1")
@IndexType(name = "type1")
@IndexMapping(value =
                "{" +
                    "\"type1\": {" +
                        "\"properties\": {" +
                            "\"dateCreate\": { \"type\": \"date\"}," +
                            "\"category\"  : { \"type\": \"string\", \"analyzer\": \"keyword\"}" +
                        "}" +
                    "}" +
                "}")
public class Index1Type1 extends Index {

    public static final String NAME = "name";
    public static final String CATEGORY = "category";
    public static final String DATECREATE = "dateCreate";

    public String name;
    public String category;
    public Date dateCreate;

    public Index1Type1() {
    }

    public Index1Type1(String name, String category, Date dateCreate) {
        this.name = name;
        this.category = category;
        this.dateCreate = dateCreate;
    }

    @Override
    public Map toIndex() {
        Map map = new HashMap();
        map.put(NAME, name);
        map.put(CATEGORY, category);
        map.put(DATECREATE, dateCreate);
        return map;
    }

    @Override
    public Indexable fromIndex(Map map) {
        name = (String)map.get(NAME);
        category = (String)map.get(CATEGORY);
        dateCreate = (Date) IndexUtils.convertValue(map.get(DATECREATE), Date.class);
        return this;
    }

    public static Finder<Index1Type1> find = new Finder<Index1Type1>(Index1Type1.class);
}
