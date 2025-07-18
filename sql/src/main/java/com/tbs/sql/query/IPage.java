package com.tbs.sql.query;

/**
 * @author abstergo
 */
public interface IPage {
    /**
     * 偏移量
     *
     * @return
     */
    long from();

    /**
     * 数据量
     *
     * @return
     */
    long number();
}
