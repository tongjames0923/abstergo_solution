package com.tbs.sql.criteria;

/**
 * 分页接口
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IPage {
    /** 偏移量 */
    long from();
    /** 数据量 */
    long number();
}
