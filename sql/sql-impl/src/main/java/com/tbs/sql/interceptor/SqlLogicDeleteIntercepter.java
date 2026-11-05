package com.tbs.sql.interceptor;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ExceptionUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;

import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.sql.builder.ISqlBuildContext;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.model.where.WhereCompose;
import com.tbs.sql.model.where.WhereInfo;
import com.tbs.sql.model.where.WhereNode;
import com.tbs.sql.util.SqlUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author abstergo
 */
public class SqlLogicDeleteIntercepter implements SqlComposeInterceptor {

    private Map<Class<?>, List<WhereInfo>> flagWhereInfoMap = new ConcurrentHashMap<>(8);

    @Override
    public WhereCompose intercept(WhereCompose compose, ISqlBuildContext context) {
        var sqlWhereInfo = flagWhereInfoMap.computeIfAbsent(context.getEntityClass(), (k) -> {
            //确认entityClass是否是逻辑删除实体,是否被LogicDeleteEntity注解
            if (!k.isAnnotationPresent(LogicDeleteEntity.class)) {
                return List.of();
            }
            LogicDeleteEntity logicDeleteEntity = k.getDeclaredAnnotation(LogicDeleteEntity.class);
            //生成whereInfo，flag不等于disable值，若disableFlagMap中存在disableFlag，则使用disableFlagMap中的值，否则使用默认值0
            var activeFlags = logicDeleteEntity.activeFlagValues();
            List<WhereInfo> whereInfos = new ArrayList<>(2);
            WhereInfo info = new WhereInfo();
            info.setContrast(ContrastEnum.IN);
            if (activeFlags != null && activeFlags.length > 0) {
                List<Integer> activeFlagList = new ArrayList<>(activeFlags.length);
                for (int i = 0; i < activeFlags.length; i++) {
                    activeFlagList.add(activeFlags[i]);
                }
                info.setFieldValue(activeFlagList);
            } else {
                return List.of();
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
            whereInfos.add(info);
            if (!logicDeleteEntity.onNullIsDeleted()) {
                WhereInfo onNUllAct = new WhereInfo();
                onNUllAct.setContrast(ContrastEnum.IS_NULL);
                onNUllAct.setFieldName(flagFieldName);
                onNUllAct.setTableName(SqlUtils.getTableName(k));
                whereInfos.add(onNUllAct);
            }

            return whereInfos;
        });
        if (compose == null) {
            compose = new WhereCompose();
        }
        var ls = compose.getChildren();
        if (ls == null) {
            ls = new LinkedList<>();
        }
        if (CollUtil.isNotEmpty(sqlWhereInfo)) {
            WhereCompose whereCompose = new WhereCompose();
            for (WhereInfo whereInfo : sqlWhereInfo) {
                whereCompose.getChildren().add(WhereNode.createInfoNode(ConnectorEnum.OR, whereInfo));
            }
            ls.add(WhereNode.createComposeNode(ConnectorEnum.AND, whereCompose));
        }
        compose.setChildren(ls);
        return compose;
    }
}
