package com.tbs.sql.model.query;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.sql.criteria.IPage;

import java.io.Serializable;

/**
 * 分页查询模型
 *
 * @author abstergo
 * @since 1.0.0
 */
public class QueryPageModel implements IPage, Serializable {

    private static final long serialVersionUID = 7922366116007242398L;
    private final long page;
    private final long perPage;

    /** 第一页第一条 */
    public static final QueryPageModel ONE = QueryPageModel.of(1L, 1L);

    public QueryPageModel(final long page, final long perPage) {
        AssertUtil.toAssert(1 > page).message("Page must be greater than 0").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        AssertUtil.toAssert(1 > perPage).message("PerPage must be greater than 0")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        this.page = page;
        this.perPage = perPage;
    }

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
