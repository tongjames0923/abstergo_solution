package com.tbs.sql.model;

import com.tbs.sql.query.IPage;

import java.io.Serializable;

/**
 * @author abstergo
 */
public class QueryPageModel implements IPage, Serializable {

    private static final long serialVersionUID = 7922366116007242398L;
    private final long page;
    private final long perPage;

    /**
     * 第一页第一条
     */
    public static final QueryPageModel ONE = QueryPageModel.of(1L, 1L);

    public QueryPageModel(final long page, final long perPage) {
        if (1 > page) {
            throw new IllegalArgumentException("Page must be greater than 0");
        }
        if (1 > perPage) {
            throw new IllegalArgumentException("PerPage must be greater than 0");
        }
        this.page = page;
        this.perPage = perPage;
    }

    /**
     * 创建一个分页对象
     *
     * @param page    页码，为空时默认为1
     * @param perPage 每页数量，为空时默认为10
     * @return 分页对象
     */
    public static final QueryPageModel of(Long page, Long perPage) {
        return new QueryPageModel(page == null ? 1L : page, perPage == null ? 10L : perPage);
    }

    @Override
    public long from() {
        return (this.page - 1) * this.perPage;
    }

    @Override
    public long number() {
        return this.perPage;
    }
}
