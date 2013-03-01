import com.github.cleverage.elasticsearch.IndexClient;
import com.github.cleverage.elasticsearch.IndexQuery;
import com.github.cleverage.elasticsearch.IndexResults;
import com.github.cleverage.elasticsearch.IndexService;
import indextype.Index1Type1;
import indextype.Index2Type1;
import org.elasticsearch.action.index.IndexResponse;
import org.junit.Test;
import play.libs.F;
import play.test.FakeApplication;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

    @Test
    public void asynchronousSearch() {
        running(esFakeApplication(), new Runnable() {
            @Override
            public void run() {
                Index1Type1 index1Type1 = new Index1Type1("name1", "category", new Date());
                index1Type1.index();
                IndexService.refresh();

                IndexQuery<Index1Type1> query = new IndexQuery<>(Index1Type1.class);
                List<F.Promise<? extends IndexResults<Index1Type1>>> promises = new ArrayList<F.Promise<? extends IndexResults<Index1Type1>>>();
                for (int i = 0; i < 10; i++) {
                    promises.add(query.fetchAsync(index1Type1.getIndexPath()));
                }

                F.Promise<List<IndexResults<Index1Type1>>> combinedPromise = F.Promise.sequence(promises);
                List<IndexResults<Index1Type1>> indexResultsList = combinedPromise.get(10L, TimeUnit.SECONDS);
                assertThat(indexResultsList.size()).isEqualTo(10);
                for (IndexResults<Index1Type1> indexResults : indexResultsList) {
                    assertThat(indexResults.totalCount).isEqualTo(1);
                }
            }
        });
    }
}