import com.github.cleverage.elasticsearch.IndexClient;
import indextype.Index1Type1;
import indextype.Index2Type1;
import org.elasticsearch.action.index.IndexResponse;
import org.junit.Test;
import play.test.FakeApplication;

import java.util.*;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

public class ElasticsearchTestJava {

    public FakeApplication esFakeApplication() {

        Map<String, Object> additionalConfiguration = new HashMap<String, Object>();
        additionalConfiguration.put("elasticsearch.local", true);
        additionalConfiguration.put("elasticsearch.index.name", "index1,index2");
        additionalConfiguration.put("elasticsearch.index.clazzs", "indextype.*");
        additionalConfiguration.put("elasticsearch.index.show_request", true);
        additionalConfiguration.put("elasticsearch.index.dropOnShutdown", true);

        List<String> additionalPlugin = new ArrayList<String>();
        additionalPlugin.add("com.github.cleverage.elasticsearch.plugin.IndexPlugin");

        return fakeApplication(additionalConfiguration, additionalPlugin);
    }


    @Test
    public void checkIndexNames() {
        running(esFakeApplication(), new Runnable() {
            public void run() {
                assertThat(IndexClient.config.indexNames.length).isEqualTo(2);
                assertThat(IndexClient.config.indexNames[0]).isEqualTo("index1");
                assertThat(IndexClient.config.indexNames[1]).isEqualTo("index2");
            }
        });
    }

    @Test
    public void checkIndex1Create() {
        running(esFakeApplication(), new Runnable() {
            public void run() {
                String name1 = "name1";
                String category = "category";
                Date date = new Date();

                // init new Type1
                Index1Type1 type1 = new Index1Type1();
                type1.name = name1;
                type1.category = category;
                type1.dateCreate = date;

                // indexing
                IndexResponse index = type1.index();

                // Read
                Index1Type1 typeResult = Index1Type1.find.byId(index.getId());

                assertThat(typeResult).isNotNull();
                assertThat(typeResult.name).isEqualTo(name1);
                assertThat(typeResult.category).isEqualTo(category);
                assertThat(typeResult.dateCreate).isEqualTo(date);
                assertThat(typeResult.getIndexPath().index).isEqualTo("index1");
                assertThat(typeResult.getIndexPath().type).isEqualTo("type1");
            }
        });
    }

    @Test
    public void checkIndex2Create() {
        running(esFakeApplication(), new Runnable() {
            public void run() {
                String name1 = "name1";
                String category = "category";
                Date date = new Date();

                // init new Type1
                Index2Type1 type1 = new Index2Type1();
                type1.name = name1;
                type1.category = category;
                type1.dateCreate = date;

                // indexing
                IndexResponse index = type1.index();

                // Read
                Index2Type1 typeResult = Index2Type1.find.byId(index.getId());

                assertThat(typeResult).isNotNull();
                assertThat(typeResult.name).isEqualTo(name1);
                assertThat(typeResult.category).isEqualTo(category);
                assertThat(typeResult.dateCreate).isEqualTo(date);
                assertThat(typeResult.getIndexPath().index).isEqualTo("index2");
                assertThat(typeResult.getIndexPath().type).isEqualTo("type1");
            }
        });
    }

}