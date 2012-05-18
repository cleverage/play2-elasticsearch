package play.modules.elasticsearch;

import java.util.Map;

/**
 * User: nboire
 * Date: 17/05/12
 */
public interface Indexable {

    public Map toIndex();
    public Indexable fromIndex(Map map);
}
