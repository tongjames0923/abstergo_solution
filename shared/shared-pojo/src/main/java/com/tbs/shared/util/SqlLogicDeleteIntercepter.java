package com.tbs.shared.util;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ExceptionUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.sql.enums.QueryConnectorEnum;
import com.tbs.sql.enums.QueryContrastEnum;
import com.tbs.sql.framework.intercepter.ISqlInfoComposeIntercepter;
import com.tbs.sql.framework.model.SqlWhereCompose;
import com.tbs.sql.framework.model.SqlWhereInfo;
import com.tbs.sql.framework.model.SqlWhereNode;
import com.tbs.sql.utils.SqlUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author abstergo
 */
public class SqlLogicDeleteIntercepter implements ISqlInfoComposeIntercepter {

    private Map<Class<?>, Optional<SqlWhereInfo>> flagSqlWhereInfoMap = new ConcurrentHashMap<>(8);

    @Override
    public SqlWhereCompose intercept(SqlWhereCompose compose, Class<?> entityClass) {
        var sqlWhereInfo = flagSqlWhereInfoMap.computeIfAbsent(entityClass, (k) -> {
            //确认entityClass是否是逻辑删除实体,是否被LogicDeleteEntity注解
            if (!entityClass.isAnnotationPresent(LogicDeleteEntity.class)) {
                return Optional.empty();
            }
            LogicDeleteEntity logicDeleteEntity = entityClass.getDeclaredAnnotation(LogicDeleteEntity.class);
            //生成whereInfo，flag不等于disable值，若disableFlagMap中存在disableFlag，则使用disableFlagMap中的值，否则使用默认值0
            var activeFlags = logicDeleteEntity.activeFlagValues();
            SqlWhereInfo info = new SqlWhereInfo();
            info.setContrast(QueryContrastEnum.IN);
            if (activeFlags != null && activeFlags.length > 0) {
                List<Integer> activeFlagList = new ArrayList<>(activeFlags.length);
                for (int i = 0; i < activeFlags.length; i++) {
                    activeFlagList.add(activeFlags[i]);
                }
                info.setFieldValue(activeFlagList);
            } else {
                return Optional.empty();
            }
            String flagFieldName = logicDeleteEntity.filedName();
            if (StrUtil.isEmpty(flagFieldName)) {
                flagFieldName = "flag";
                //设置字段名，读取反射信息中的flag字段名
                try {
                    flagFieldName = SqlUtils.getFiledNameWithMapping(entityClass.getField("flag"));
                } catch (Exception e) {
                    AssertUtil.throwError(() -> ExceptionUtil.newException(e, "释放字段异常"));
                }
            }

            info.setFieldName(flagFieldName);
            info.setTableName(SqlUtils.getTableName(entityClass));
            info.setValueType(Integer.class);
            return Optional.of(info);
        });
        if (compose == null) {
            compose = new SqlWhereCompose();
        }
        var ls = compose.getChildren();
        if (CollUtil.isEmpty(ls)) {
            ls = new LinkedList<>();
        }
        if (sqlWhereInfo.isPresent()) {
            ls.add(SqlWhereNode.createInfoNode(QueryConnectorEnum.AND, sqlWhereInfo.get()));
        }
        return compose;
    }
}
