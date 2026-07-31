package com.tbs.app.intercept;

import com.tbs.app.service.department.IDepartmentUserRelationService;
import com.tbs.app.service.role.IUserRoleRelationService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.common.util.base.BeanUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.shared.enums.SystemDataScopeEnum;
import com.tbs.shared.pojo.ICommonCreateAuditable;
import com.tbs.shared.pojo.ICustomDataScopeEntity;
import com.tbs.shared.pojo.IDeptBelongEntity;
import com.tbs.sql.builder.SqlBuildContext;
import com.tbs.sql.criteria.IQueryCriteria;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.interceptor.SqlComposeInterceptor;
import com.tbs.sql.model.where.WhereCompose;
import com.tbs.sql.model.where.WhereInfo;
import com.tbs.sql.model.where.WhereNode;
import com.tbs.sql.util.SqlUtils;
import com.tbs.web.context.RequestContextHolder;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.stream.Collectors;

/**
 * 数据权限拦截器
 *
 * @author abstergo
 */
@Slf4j
public class DataPermissionIntercepter implements SqlComposeInterceptor {

    public static final String DATA_PERMISSION_ENABLE_FLAG_KEY = "dataPermissionEnableFlag";

    public static final void setEnableFlag(boolean flag) {
        RequestContextHolder.current().put(DATA_PERMISSION_ENABLE_FLAG_KEY, flag);
    }

    public static final boolean getEnableFlag() {
        var context = RequestContextHolder.current();
        if (context == null) {
            return false;
        }
        return (boolean)context.get(DATA_PERMISSION_ENABLE_FLAG_KEY).orElse(false);
    }

    @Override
    public WhereCompose intercept(WhereCompose compose, SqlBuildContext sqlContext) {
        var context = RequestContextHolder.current();
        if (context == null) {
            return compose;
        }

        var flag = context.get(DATA_PERMISSION_ENABLE_FLAG_KEY);
        if (flag.isEmpty()) {
            return compose;
        }
        var entityClass = sqlContext.getEntityClass();
        Object entity = sqlContext.getSqlEntity();
        if (BeanUtil.isBaseFrom(entityClass, IQueryCriteria.class)) {
            entityClass = ((IQueryCriteria)entity).getSelectSQLInfo().getEntityClass();
        }
        if (entity == null) {
            return compose;
        }

        boolean createByEntity = BeanUtil.isBaseFrom(entityClass, ICommonCreateAuditable.class);

        boolean depBelongEntity = BeanUtil.isBaseFrom(entityClass, IDeptBelongEntity.class);

        boolean customBelongEntity = BeanUtil.isBaseFrom(entityClass, ICustomDataScopeEntity.class);

        if (!depBelongEntity && !customBelongEntity) {
            log.debug("数据权限拦截器: 不是部门关联实体类也不是自定义数据权限处理实体，不处理");
            return compose;
        }
        var user = AuthUtil.getInstance().getCurrentUser();
        var u = CollUtil.getFirst(SpringUtil.getBean(ISystemUserService.class).queryByUserName(user.getUsername()));
        var roles = SpringUtil.getBean(IUserRoleRelationService.class).queryUserRoles(u.getId());
        if (roles.isEmpty()) {
            compose.getChildren()
                .add(WhereNode.createInfoNode(ConnectorEnum.AND, WhereInfo.builder().rawSql("1=0").build()));
            return compose;
        }

        var userDepts =
            SpringUtil.getBean(IDepartmentUserRelationService.class).queryUserDepartments(u.getId()).stream()
                .map(d -> d.getId()).collect(Collectors.toList());

        var maxDataPermission = SystemDataScopeEnum.SELF.getCode();
        for (var r : roles) {
            var dp = r.getDataScope();
            if (dp == null) {
                continue;
            }
            if (dp < maxDataPermission) {
                maxDataPermission = dp;
            }
        }
        switch (EnumUtil.valueOf(SystemDataScopeEnum.class, maxDataPermission)) {
            case ALL:
                break;
            case DEPT:
                if (!depBelongEntity) {
                    log.warn("数据权限拦截器: 非部门关联实体类，不处理");
                    return compose;
                }
                Field f = null;
                try {
                    f = entityClass.getDeclaredField("deptId");
                } catch (Exception e) {
                    log.error("数据权限拦截器: 获取部门字段失败", e);
                }
                if (f == null) {
                    log.error("数据权限拦截器: 获取部门字段失败");
                    return compose;
                }
                var fieldName = SqlUtils.getFieldNameWithMapping(f);

                compose.getChildren().add(WhereNode.createInfoNode(ConnectorEnum.AND,
                    WhereInfo.builder().fieldName(fieldName).contrast(ContrastEnum.IN).fieldValue(userDepts).build()));
                break;
            case SELF:
                if (!createByEntity) {
                    log.warn("数据权限拦截器: 非创建人关联实体类，不处理");
                    return compose;
                }
                Field f1 = null;
                try {
                    f1 = entityClass.getDeclaredField("createBy");
                } catch (Exception e) {
                    log.error("数据权限拦截器: 获取创建人字段失败", e);
                }
                if (f1 == null) {
                    log.error("数据权限拦截器: 获取创建人字段失败");
                    return compose;
                }
                var fieldName1 = SqlUtils.getFieldNameWithMapping(f1);
                compose.getChildren().add(WhereNode.createInfoNode(ConnectorEnum.AND,
                    WhereInfo.builder().fieldName(fieldName1).contrast(ContrastEnum.EQUAL).fieldValue(u.getId())
                        .build()));
                break;

            default:
                log.error("数据权限拦截器: 无效的权限类型");
                break;
        }

        return compose;
    }
}
