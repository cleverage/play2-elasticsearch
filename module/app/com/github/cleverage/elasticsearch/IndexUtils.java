package com.github.cleverage.elasticsearch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentBuilder;

import play.Logger;

public abstract class IndexUtils {

    public static <T extends Indexable> List<T> getIndexables(Map map, String key, Class<T> t) {
        List<Map<String, Object>> mapList = (List<Map<String, Object>>) map.get(key);
        List<T> list = new ArrayList<T>();
        if (mapList != null) {
            for (Map<String, Object> map1 : mapList) {
                list.add(getIndexable(map1, t));
            }
        }
        return list;
    }
    
    /**
     * Get indexables from Map
     * @param map
     * @param key
     * @param clazz
     * @return
     */
    public static <T extends Indexable> Map<String, T> getIndexablesFromMap(Map<String, Object> map, String key, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> items = (Map<String, Map<String, Object>>) map.get(key);
        Map<String, T> result = new HashMap<>();
        if (items != null) {
            for (Entry<String, Map<String, Object>> item : items.entrySet()) {
                result.put(item.getKey(), getIndexable(item.getValue(), clazz));
            }
        }
        return result;
    }

    public static <T extends Indexable> T getIndexable(Map map, String key, Class<T> t) {
        Map map1 = (Map) map.get(key);
        return getIndexable(map1, t);
    }

    public static <T extends Indexable> T getIndexable(Map map, Class<T> t) {
        T instance = IndexUtils.getInstanceIndexable(t);
        return (T) instance.fromIndex((Map) map);
    }

    /**
     * Converts a List of Object T to an List of Map<String, Object> for serialize in the index
     *
     * @param listT
     * @param <T>
     * @return
     */
    public static <T extends Indexable> List<Map<String, Object>> toIndex(List<T> listT) {
        List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        for (T t : listT) {
            mapList.add(t.toIndex());
        }
        return mapList;
    }
    
    /**
     * Converts a Map<String, T> to a Map<String, Object> for serialize in the index
     * @param map
     * @return
     */
    public static <T extends Indexable> Map<String, Object> toIndex(Map<String, T> map) {
        Map<String, Object> result = new HashMap<>();
        for (Entry<String, T> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toIndex());
        }
        return result;
    }
    
    public static <T extends Index> T getInstanceIndex(Class<T> clazz) {
        T object = null;
        try {
            object = clazz.newInstance();
        } catch (InstantiationException e) {
            Logger.error("...", e);
        } catch (IllegalAccessException e) {
            Logger.error("...", e);
        }
        return object;
    }

    public static <T extends Indexable> T getInstanceIndexable(Class<T> clazz) {
        T object = null;
        try {
            object = clazz.newInstance();
        } catch (InstantiationException e) {
            Logger.error("...", e);
        } catch (IllegalAccessException e) {
            Logger.error("...", e);
        }
        return object;
    }

    /**
     * Convert String to Object
     *
     * @param value
     * @param targetType
     * @return
     */
    public static Object convertValue(final Object value, final Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.equals(value.getClass())) {
            return value;
        }

        if (targetType.equals(String.class)) {
            return value.toString();
        }
        else if (targetType.equals(BigDecimal.class)) {
            return new BigDecimal(value.toString());
        }
        else if (targetType.equals(Date.class)) {
            return convertToDate(value);
        }
        else if (targetType.equals(Integer.class)) {
            if (value instanceof Number) {
                return Integer.valueOf(((Number) value).intValue());
            }
            else {
                return Integer.valueOf(value.toString());
            }
        }
        else if (targetType.equals(Long.class)) {
            if (value instanceof Number) {
                return Long.valueOf(((Number) value).longValue());
            }
            else {
                return Long.valueOf(value.toString());
            }
        }
        else if (targetType.equals(Double.class)) {
            if (value instanceof Number) {
                return Double.valueOf(((Number) value).doubleValue());
            }
            else {
                return Double.valueOf(value.toString());
            }
        }
        else if (targetType.equals(Float.class)) {
            if (value instanceof Number) {
                return Float.valueOf(((Number) value).floatValue());
            }
            else {
                return Float.valueOf(value.toString());
            }
        }
        else if (targetType.equals(UUID.class)) {
            return UUID.fromString((String) value);
        }
        else if (targetType.equals(GeoPoint.class)) {
            Map geoValuesMap = (Map) value;
            return new GeoPoint((Double)geoValuesMap.get("lat"),(Double)geoValuesMap.get("lon"));
        }
        else {
            return value;
        }
    }

    /**
     * Convert to date.
     *
     * @param value the value
     * @return the date
     */
    private static Date convertToDate(Object value) {
        Date date = null;
        if (value != null && !"".equals(value)) {
            if (value instanceof Long) {
                date = new Date(((Long) value).longValue());

            } else if (value instanceof String) {
                String val = (String) value;
                int dateLength = String.valueOf(Long.MAX_VALUE).length();
                if (dateLength == val.length()) {
                    date = new Date(Long.valueOf(val).longValue());
                } else {
                    date = getDate(val);
                }
            } else {
                date = (Date) value;
            }
        }
        return date;
    }

    private static Date getDate(String val) {
        try {
            return XContentBuilder.defaultDatePrinter.parseDateTime(val).toDate();
        } catch (Throwable t) {
            Logger.error(val, t);
        }
        return null;
    }
}
