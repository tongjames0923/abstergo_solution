package com.tbs.app.error.transformer;

import com.tbs.app.constant.SystemI18nConstant;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.IErrorTransformer;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.i18n.I18nUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * 数据库异常转换器
 * <p>
 * 原始异常消息含完整 SQL 语句、表名与约束名，属服务端实现细节：仅作为 cause 链保留以便日志排查，
 * 对外一律回传 i18n 通用提示，避免经统一响应体泄露数据库结构。
 * </p>
 *
 * @author abstergo
 */
@Component
public class SQLErrorTransformer implements IErrorTransformer {
    @Override
    public boolean support(Throwable throwable) {
        return throwable != null && (throwable instanceof SQLException || throwable instanceof DataAccessException);
    }

    @Override
    public FormattedRuntimeException toFormattedRuntimeException(Throwable throwable) {
        return new FormattedRuntimeException(throwable, I18nUtil.get(SystemI18nConstant.DATA_ACCESS_ERROR))
            .errorLevel(ErrorLevelEnum.DATA_ACCESS_ERROR);
    }

    /**
     * 必须优先于默认转换器（默认 support 恒为 true），否则本转换器永远不会命中
     *
     * @return 最高优先级
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
