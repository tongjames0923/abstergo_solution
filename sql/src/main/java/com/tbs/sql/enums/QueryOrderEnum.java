package com.tbs.sql.enums;

/**
 * The enum Query order enum.
 */
public enum QueryOrderEnum {
    /**
     * Desc query order enum.
     */
    DESC,
    /**
     * Asc query order enum.
     */
    ASC;

    public static QueryOrderEnum getOrder(String order) {
        if (order.equalsIgnoreCase("desc")) {
            return DESC;
        } else {
            return ASC;
        }
    }

    public static String getOrder(QueryOrderEnum order) {
        if (order.equals(DESC)) {
            return "desc";
        } else {
            return "asc";
        }
    }
}
