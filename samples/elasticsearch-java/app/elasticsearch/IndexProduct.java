package elasticsearch;

import org.elasticsearch.action.index.IndexResponse;
import play.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: nboire
 * Date: 20/04/12
 */
public class IndexProduct extends Indexable {

    public static final String INDEX = IndexManager.INDEX_DEFAULT;
    public static final String INDEX_TYPE = "product";

    public Long id;
    public String name;
    public Integer price;
    public Date dateCreate;

    public IndexProduct() {
    }

    @Override
    public Map toIndex() {

        HashMap map = new HashMap();
        map.put("name", name);
        map.put("price", price);
        map.put("dateCreate", dateCreate);

        return map;
    }

    @Override
    public Indexable fromIndex(Map map) {

        Logger.debug("IndexProduct From Index : " + map.toString());

        IndexProduct indexProduct = new IndexProduct();
        indexProduct.name = (String) map.get("name");
        indexProduct.price = (Integer) IndexMapping.convertValue(map.get("price"), Integer.class);
        indexProduct.dateCreate = (Date) IndexMapping.convertValue(map.get("dateCreate"), Date.class);


        return indexProduct;
    }

    // Method for gestion Index elasticsearch
    public static IndexPath getPath() {
        return new IndexPath(INDEX, INDEX_TYPE);
    }

    public IndexResponse index() {

        return IndexManager.index(getPath(), ""+id, this);
    }

    public static IndexResponse index(IndexProduct indexProduct) {

        return IndexManager.index(getPath(), ""+indexProduct.id, indexProduct);
    }

    public static IndexProduct get(String id) {

        return IndexManager.get(getPath(), IndexProduct.class, id);
    }

    public static void delete(String id) {
        IndexPath indexPath = new IndexPath(INDEX_TYPE);
        IndexManager.delete(indexPath, id);
    }
}
