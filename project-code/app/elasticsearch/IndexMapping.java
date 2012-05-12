package elasticsearch;

import org.elasticsearch.common.xcontent.XContentBuilder;
import play.Logger;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Classe permettant de convertir les données récupéré de l'index ( toutes des strings ) en Object
 * User: nboire
 * Date: 25/04/12
 */
public abstract class IndexMapping {

    /**
     * Convert String to Object
     * @param value
     * @param targetType
     * @return
     */
    public static Object convertValue(final Object value, final Class<?> targetType) {
        if (targetType.equals(value.getClass())) {
            // Types match
            return value;
        }

        // Types do not match, perform conversion where needed
        if (targetType.equals(String.class)) {
            return value.toString();
        } else if (targetType.equals(BigDecimal.class)) {
            return new BigDecimal(value.toString());
        } else if (targetType.equals(Date.class)) {
            return convertToDate(value);

            // Use Number intermediary where possible
        } else if (targetType.equals(Integer.class)) {
            if (value instanceof Number) {
                return Integer.valueOf(((Number) value).intValue());
            } else {
                return Integer.valueOf(value.toString());
            }
        } else if (targetType.equals(Long.class)) {
            if (value instanceof Number) {
                return Long.valueOf(((Number) value).longValue());
            } else {
                return Long.valueOf(value.toString());
            }
        } else if (targetType.equals(Double.class)) {
            if (value instanceof Number) {
                return Double.valueOf(((Number) value).doubleValue());
            } else {
                return Double.valueOf(value.toString());
            }
        } else if (targetType.equals(Float.class)) {
            if (value instanceof Number) {
                return Float.valueOf(((Number) value).floatValue());
            } else {
                return Float.valueOf(value.toString());
            }

            // Fallback to simply returning the value
        } else {
            return value;
        }
    }

    /**
     * Convert to date.
     *
     * @param value
     *            the value
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

    /**
     * Gets the date.
     *
     * @param val
     *            the val
     * @return the date
     */
    private static Date getDate(String val) {
        try {
            // Use ES internal converter
            return XContentBuilder.defaultDatePrinter.parseDateTime(val).toDate();
        } catch (Throwable t) {
            Logger.error(val,t);
        }
        return null;
    }
}
