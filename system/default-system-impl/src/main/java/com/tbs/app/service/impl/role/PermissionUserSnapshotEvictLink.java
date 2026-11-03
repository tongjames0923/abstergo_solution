package com.tbs.app.service.impl.role;

import com.tbs.app.service.role.ISystemRoleGroupService;
import com.tbs.cache.cascade.ICacheEvictLink;
import com.tbs.common.util.base.CollUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.tbs.app.service.impl.CacheNamesConstants.ROLE_GROUP;
import static com.tbs.app.service.impl.CacheNamesConstants.ROLE_GROUP_USER_ID;

/**
 * 用户身份快照的关联失效声明
 * <p>
 * 声明「组权限缓存被清除时，连带清除组下全部用户的身份快照缓存」。这两份缓存的键维度不同
 * （源按 groupId、目标按 userId），注解表达式无法反推，故由本声明覆写
 * {@link #translate(Object)} 完成反查；查询走业务服务，缓存框架自身不含任何表关系知识。
 * </p>
 * <p>
 * 有了本声明，权限组的任何写入点（含将来新增的）只要清除了组权限缓存，派生的用户身份快照就会被连带
 * 清除，不需要在每个写入点重复记得清第二份缓存——这正是本功能最初的缺陷所在。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
public class PermissionUserSnapshotEvictLink implements ICacheEvictLink {

    @Resource
    private ISystemRoleGroupService roleGroupService;

    @Override
    public String getName() {
        return "权限组变更连带清除用户身份快照";
    }

    @Override
    public String source() {
        return ROLE_GROUP;
    }

    @Override
    public String target() {
        return ROLE_GROUP_USER_ID;
    }

    /**
     * 把组标识展开为该组当前绑定的用户标识集合
     * <p>
     * 只读查询，走 {@code system_role_group_user_relation} 的 user 关联索引；
     * 抛出的异常由级联执行方捕获记录，不影响已完成的业务写入。
     * </p>
     *
     * @param sourceKey 被清除的组权限缓存键，即权限组标识
     * @return 该组绑定的用户标识集合
     */
    @Override
    public Collection<Object> translate(Object sourceKey) {
        if (!(sourceKey instanceof Long groupId)) {
            log.warn("关联失效声明[{}]收到非组标识的键:{}，跳过连带清除", getName(), sourceKey);
            return Collections.emptyList();
        }
        List<Long> userIds = roleGroupService.boundUserIds(groupId);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(userIds);
    }
}
