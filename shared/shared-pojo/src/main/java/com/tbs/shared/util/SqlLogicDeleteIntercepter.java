package com.tbs.shared.util;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ExceptionUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.sql.builder.SqlBuildContext;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.interceptor.SqlComposeInterceptor;
import com.tbs.sql.model.where.WhereCompose;
import com.tbs.sql.model.where.WhereInfo;
import com.tbs.sql.model.where.WhereNode;
import com.tbs.sql.util.SqlUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author abstergo
 */
public class SqlLogicDeleteIntercepter implements SqlComposeInterceptor {

    private Map<Class<?>, Optional<WhereInfo>> flagWhereInfoMap = new ConcurrentHashMap<>(8);

    @Override
    public WhereCompose intercept(WhereCompose compose, SqlBuildContext context) {
        var sqlWhereInfo = flagWhereInfoMap.computeIfAbsent(context.getEntityClass(), (k) -> {
            //确认entityClass是否是逻辑删除实体,是否被LogicDeleteEntity注解
            if (!k.isAnnotationPresent(LogicDeleteEntity.class)) {
                return Optional.empty();
            }
            LogicDeleteEntity logicDeleteEntity = k.getDeclaredAnnotation(LogicDeleteEntity.class);
            //生成whereInfo，flag不等于disable值，若disableFlagMap中存在disableFlag，则使用disableFlagMap中的值，否则使用默认值0
            var activeFlags = logicDeleteEntity.activeFlagValues();
            WhereInfo info = new WhereInfo();
            info.setContrast(ContrastEnum.IN);
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
                    flagFieldName = SqlUtils.getFieldNameWithMapping(k.getField("flag"));
                } catch (Exception e) {
                    AssertUtil.throwError(() -> ExceptionUtil.newException(e, "释放字段异常"));
                }
            }

            info.setFieldName(flagFieldName);
            info.setTableName(SqlUtils.getTableName(k));
            info.setValueType(Integer.class);
            return Optional.of(info);
        });
        if (compose == null) {
            compose = new WhereCompose();
        }
        var ls = compose.getChildren();
        if (ls == null) {
            ls = new LinkedList<>();
        }
        if (sqlWhereInfo.isPresent()) {
            ls.add(WhereNode.createInfoNode(ConnectorEnum.AND, sqlWhereInfo.get()));
        }
        compose.setChildren(ls);
        return compose;
    }
}
