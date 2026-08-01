package com.tbs.app.intercept;

import com.tbs.app.service.department.IDepartmentUserRelationService;
import com.tbs.app.service.impl.web.PreAuthUtils;
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
import com.tbs.shared.pojo.entity.SystemRoleEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.sql.builder.ISqlBuildContext;
import com.tbs.sql.criteria.IQueryCriteria;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.interceptor.SqlComposeInterceptor;
import com.tbs.sql.model.where.WhereCompose;
import com.tbs.sql.model.where.WhereInfo;
import com.tbs.sql.model.where.WhereNode;
import com.tbs.sql.util.SqlUtils;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据权限拦截器
 *
 * @author abstergo
 */
@Slf4j
public class DataPermissionIntercepter implements SqlComposeInterceptor {

    /**
     * 字段缓存，避免重复反射获取字段
     */
    private static final ConcurrentHashMap<Class<?>, Field> DEPT_FIELD_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, Field> CREATE_BY_FIELD_CACHE = new ConcurrentHashMap<>();

    public static final String DATA_PERMISSION_ENABLE_FLAG_KEY = "dataPermissionEnableFlag";

    public static final void setEnableFlag(ISqlBuildContext context, boolean flag) {
        context.getContextParams().put(DATA_PERMISSION_ENABLE_FLAG_KEY, flag);
    }

    public static final boolean getEnableFlag(ISqlBuildContext context) {
        if (!context.getContextParams().containsKey(DATA_PERMISSION_ENABLE_FLAG_KEY)) {
            return false;
        }
        return (boolean)context.getContextParams().getOrDefault(DATA_PERMISSION_ENABLE_FLAG_KEY, false);
    }

    /**
     * 拦截器
     *
     * @param compose
     * @param sqlContext
     * @return
     */
    @Override
    public WhereCompose intercept(WhereCompose compose, ISqlBuildContext sqlContext) {
        var flag = getEnableFlag(sqlContext);
        if (!flag) {
            log.debug("数据权限拦截器: 禁用");
            return compose;
        }
        Object entity = sqlContext.getSqlEntity();
        if (entity == null) {
            return compose;
        }
        var entityClass = sqlContext.getEntityClass();
        if (BeanUtil.isBaseFrom(entityClass, IQueryCriteria.class)) {
            entityClass = ((IQueryCriteria)entity).getSelectSQLInfo().getEntityClass();
        }

        boolean createByEntity = BeanUtil.isBaseFrom(entityClass, ICommonCreateAuditable.class);

        boolean depBelongEntity = BeanUtil.isBaseFrom(entityClass, IDeptBelongEntity.class);

        boolean customBelongEntity = BeanUtil.isBaseFrom(entityClass, ICustomDataScopeEntity.class);

        if (!depBelongEntity && !customBelongEntity && !createByEntity) {
            log.debug("数据权限拦截器: 不是部门关联实体类也不是自定义数据权限处理实体，不处理");
            return compose;
        }
        var user = AuthUtil.getInstance().getCurrentUser();

        if (!PreAuthUtils.isAccountActived(user)) {
            log.debug("数据权限拦截器: 账户未激活");
            compose.getChildren()
                .add(WhereNode.createInfoNode(ConnectorEnum.AND, WhereInfo.builder().rawSql("1=0").build()));
            return compose;
        }

        var u = CollUtil.of(SpringUtil.getBean(ISystemUserService.class).queryByUserName(user.getUsername()))
            .findFirstOrNull();
        if (u == null) {
            log.warn("数据权限拦截器: 用户不存在 {}", user.getUsername());
            compose.getChildren()
                .add(WhereNode.createInfoNode(ConnectorEnum.AND, WhereInfo.builder().rawSql("1=0").build()));
            return compose;
        }
        var roles = SpringUtil.getBean(IUserRoleRelationService.class).queryUserRoles(u.getId());
        if (CollUtil.isEmpty(roles)) {
            compose.getChildren()
                .add(WhereNode.createInfoNode(ConnectorEnum.AND, WhereInfo.builder().rawSql("1=0").build()));
            return compose;
        }

        var userDepts =
            SpringUtil.getBean(IDepartmentUserRelationService.class).queryUserDepartments(u.getId()).stream()
                .map(d -> d.getId()).collect(Collectors.toList());

        var maxDataPermission = getMaxDataPermission(roles);
        switch (EnumUtil.valueOf(SystemDataScopeEnum.class, maxDataPermission)) {
            case ALL:
                log.info("数据权限拦截器：用户拥有最高权限，不做特殊拦截");
                break;
            case DEPT:
                if (deptLevel(compose, depBelongEntity, entityClass, userDepts)) {
                    return compose;
                }
                break;
            case SELF:
                if (selfLevel(compose, createByEntity, entityClass, u)) {
                    return compose;
                }
                break;

            default:
                log.error("数据权限拦截器: 无效的权限类型");
                break;
        }

        return compose;
    }

    private static boolean deptLevel(WhereCompose compose, boolean depBelongEntity, Class<?> entityClass,
        List<Long> userDepts) {
        if (!depBelongEntity) {
            log.warn("数据权限拦截器: 非部门关联实体类，不处理");
            return true;
        }
        if (CollUtil.isEmpty(userDepts)) {
            log.warn("数据权限拦截器: 部门为空");
            compose.getChildren()
                .add(WhereNode.createInfoNode(ConnectorEnum.AND, WhereInfo.builder().rawSql("1=0").build()));
        }
        Field f = DEPT_FIELD_CACHE.computeIfAbsent(entityClass, clazz -> {
            try {
                return clazz.getDeclaredField("deptId");
            } catch (Exception e) {
                log.error("数据权限拦截器: 获取部门字段失败", e);
                return null;
            }
        });
        if (f == null) {
            log.error("数据权限拦截器: 获取部门字段失败");
            return true;
        }
        var fieldName = SqlUtils.getFieldNameWithMapping(f);

        compose.getChildren().add(WhereNode.createInfoNode(ConnectorEnum.AND,
            WhereInfo.builder().fieldName(fieldName).contrast(ContrastEnum.IN).fieldValue(userDepts).build()));
        return false;
    }

    private static boolean selfLevel(WhereCompose compose, boolean createByEntity, Class<?> entityClass,
        SystemUserEntity u) {
        if (!createByEntity) {
            log.warn("数据权限拦截器: 非创建人关联实体类，不处理");
            return true;
        }
        Field f1 = CREATE_BY_FIELD_CACHE.computeIfAbsent(entityClass, clazz -> {
            try {
                return clazz.getDeclaredField("createBy");
            } catch (Exception e) {
                log.error("数据权限拦截器: 获取创建人字段失败", e);
                return null;
            }
        });
        if (f1 == null) {
            log.error("数据权限拦截器: 获取创建人字段失败");
            return true;
        }
        var fieldName1 = SqlUtils.getFieldNameWithMapping(f1);
        compose.getChildren().add(WhereNode.createInfoNode(ConnectorEnum.AND,
            WhereInfo.builder().fieldName(fieldName1).contrast(ContrastEnum.EQUAL).fieldValue(u.getId()).build()));
        return false;
    }

    private static Integer getMaxDataPermission(List<SystemRoleEntity> roles) {
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
        return maxDataPermission;
    }
}
