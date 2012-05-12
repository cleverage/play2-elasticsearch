package elasticsearch;

import java.util.Map;

/**
 * User: nboire
 * Date: 19/04/12
 */
public abstract class Indexable {

    public abstract Map toIndex();
    public abstract Indexable fromIndex(Map map);
}
