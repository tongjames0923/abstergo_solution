package com.tbs.view.route.impl;

import com.tbs.app.service.user.ISystemUserService;
import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.shared.mapper.ViewRouteConfigrationMapper;
import com.tbs.shared.mapper.ViewRouteMetaMapper;
import com.tbs.shared.mapper.ViewRouteRoleMapper;
import com.tbs.shared.mapper.ViewRouteVersionMapper;
import com.tbs.shared.pojo.entity.ViewRouteConfigrationEntity;
import com.tbs.shared.pojo.entity.ViewRouteMetaEntity;
import com.tbs.shared.pojo.entity.ViewRouteRoleEntity;
import com.tbs.shared.pojo.entity.ViewRouteVersionEntity;
import com.tbs.view.route.api.IViewRouteService;
import com.tbs.view.route.model.ViewRouteMetaModel;
import com.tbs.view.route.model.ViewRouteModel;
import com.tbs.view.route.model.ViewRouteVersionModel;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于数据库的视图路由服务实现类
 * <p>
 * 该实现从数据库加载路由配置，支持动态编辑、热更新与版本控制。
 * 版本控制语义：版本表为唯一事实来源，路由行挂靠版本ID，历史不可变，
 * 一切变更（保存/回滚/清空分组）都产生新的版本记录（append-only）。
 * 某分组的当前版本即该组 MAX(id) 的版本记录，全局视图版本号即版本表 MAX(id)。
 * </p>
 *
 * @author abstergo
 */
@Slf4j
@Service
public class DatabaseViewRouteServiceImpl implements IViewRouteService, IStartup {

    @Autowired
    private ViewRouteConfigrationMapper viewRouteConfigrationMapper;

    @Autowired
    private ViewRouteMetaMapper viewRouteMetaMapper;

    @Autowired
    private ViewRouteRoleMapper viewRouteRoleMapper;

    @Autowired
    private ViewRouteVersionMapper viewRouteVersionMapper;

    /**
     * 路由配置缓存，按分组存储（仅缓存每组当前版本的路由）
     */
    private Map<String, List<ViewRouteModel>> routeCache = new HashMap<>(16);

    @Override
    public List<String> getAllViewRouteGroups() {
        return new ArrayList<>(routeCache.keySet());
    }

    @Override
    public List<ViewRouteModel> getViewRoutesByGroup(String group) {
        return new ArrayList<>(routeCache.getOrDefault(group, Collections.emptyList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ViewRouteVersionModel saveViewRoute(String group, List<ViewRouteModel> viewRouteModels,
        String description) {
        AssertUtil.isTrue(viewRouteModels == null || viewRouteModels.isEmpty())
            .message(I18nUtil.get("view.route.config.cannot.be.empty"))
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

        try {
            Long userId = getCurrentUserId();

            // 1. 产生新的版本记录
            ViewRouteVersionEntity versionEntity = createVersionRecord(group, description, userId);

            // 2. 保存新的路由配置，全部挂靠新版本
            for (ViewRouteModel routeModel : viewRouteModels) {
                saveRouteModel(routeModel, null, group, versionEntity.getId(), userId);
            }

            // 3. 重建该分组缓存
            routeCache.put(group, viewRouteModels);

            log.info("成功保存路由分组: {}，包含 {} 个路由，版本: v{}", group, viewRouteModels.size(),
                versionEntity.getVersionNo());
            return toVersionModel(versionEntity);
        } catch (Exception e) {
            log.error("保存路由配置失败: {}", group, e);
            AssertUtil.isTrue(true).cause(e).errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
                .message(I18nUtil.get("view.route.save.failed"))
                .throwIt();
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteViewRoute(String group) {
        try {
            // 若分组不存在或当前版本已是空版本，无需重复清空
            Long latestVersionId = findLatestVersionId(group);
            if (latestVersionId == null) {
                return;
            }
            if (viewRouteConfigrationMapper.findByVersionId(latestVersionId).isEmpty()) {
                return;
            }

            // 插入一条不含任何路由行的版本记录（清空分组），历史数据原样保留
            createVersionRecord(group, I18nUtil.get("view.route.clear.group"), getCurrentUserId());

            // 清除缓存，该分组从列表中消失
            routeCache.remove(group);

            log.info("成功清空路由分组: {}", group);
        } catch (Exception e) {
            log.error("删除路由配置失败: {}", group, e);
            AssertUtil.isTrue(true).cause(e).errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
                .message(I18nUtil.get("view.route.delete.failed"))
                .throwIt();
        }
    }

    @Override
    public List<ViewRouteVersionModel> getVersionHistory(String group) {
        return viewRouteVersionMapper.findByGroupOrderByVersionNoDesc(group)
            .stream()
            .map(this::toVersionModel)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ViewRouteVersionModel rollbackToVersion(String group, Long versionId) {
        try {
            // 1. 校验目标版本存在且属于该分组
            ViewRouteVersionEntity targetVersion = viewRouteVersionMapper.selectByPrimaryKey(versionId);
            AssertUtil.isTrue(targetVersion == null || !group.equals(targetVersion.getRouteGroup()))
                .message(I18nUtil.get("view.route.version.not.found.or.not.in.group"))
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

            Long userId = getCurrentUserId();

            // 2. 产生新的版本记录（说明自动填"回滚自 vN"）
            ViewRouteVersionEntity newVersion =
                createVersionRecord(group, I18nUtil.get("view.route.rollback.from", targetVersion.getVersionNo()),
                    userId);

            // 3. 复制目标版本的路由数据到新版本（含元数据与权限）
            List<ViewRouteConfigrationEntity> oldRoutes = viewRouteConfigrationMapper.findByVersionId(versionId);
            copyRouteTree(oldRoutes, null, null, newVersion.getId(), userId);

            // 4. 从数据库重建该分组缓存
            rebuildGroupCache(group);

            log.info("路由分组 {} 已回滚自 v{}，生成新版本 v{}", group, targetVersion.getVersionNo(),
                newVersion.getVersionNo());
            return toVersionModel(newVersion);
        } catch (Exception e) {
            log.error("回滚路由配置失败: {} -> {}", group, versionId, e);
            AssertUtil.isTrue(true).cause(e).errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
                .message(I18nUtil.get("view.route.rollback.failed"))
                .throwIt();
            return null;
        }
    }

    @Override
    public long getViewVersion() {
        Long maxId = viewRouteVersionMapper.findMaxId();
        return maxId == null ? 0L : maxId;
    }

    @Override
    public void startUp() throws RuntimeException {
        try {
            // 从数据库加载所有路由配置
            loadAllRoutesFromDatabase();
            log.info("从数据库加载路由配置完成，共加载 {} 个分组", routeCache.size());
        } catch (Exception e) {
            log.error("从数据库加载路由配置失败", e);
            AssertUtil.isTrue(true).cause(e).errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
                .message(I18nUtil.get("view.route.load.failed"))
                .throwIt();
        }
    }

    /**
     * 从数据库加载所有路由配置
     * <p>
     * 两段式加载：先取每个分组的最新版本ID，再按版本查询路由行并构建树
     * </p>
     */
    private void loadAllRoutesFromDatabase() {
        // 清空缓存
        routeCache.clear();

        // 1. 计算每个分组的当前版本（按ID倒序遍历，每组第一条即最新版本）
        Map<String, Long> latestVersionByGroup = new LinkedHashMap<>();
        for (ViewRouteVersionEntity version : viewRouteVersionMapper.findAllOrderByIdDesc()) {
            latestVersionByGroup.putIfAbsent(version.getRouteGroup(), version.getId());
        }

        // 2. 按最新版本加载路由并构建树
        for (Map.Entry<String, Long> entry : latestVersionByGroup.entrySet()) {
            String group = entry.getKey();
            List<ViewRouteConfigrationEntity> routeEntities =
                viewRouteConfigrationMapper.findByVersionId(entry.getValue());
            if (!routeEntities.isEmpty()) {
                routeCache.put(group, buildRouteTree(routeEntities, null));
            }
        }
    }

    /**
     * 重建指定分组的缓存
     *
     * @param group 分组名称
     */
    private void rebuildGroupCache(String group) {
        Long latestVersionId = findLatestVersionId(group);
        if (latestVersionId == null) {
            routeCache.remove(group);
            return;
        }
        List<ViewRouteConfigrationEntity> routeEntities = viewRouteConfigrationMapper.findByVersionId(latestVersionId);
        if (routeEntities.isEmpty()) {
            routeCache.remove(group);
        } else {
            routeCache.put(group, buildRouteTree(routeEntities, null));
        }
    }

    /**
     * 查询指定分组的最新版本ID
     *
     * @param group 分组名称
     * @return 最新版本ID，分组不存在时返回null
     */
    private Long findLatestVersionId(String group) {
        List<ViewRouteVersionEntity> versions = viewRouteVersionMapper.findByGroupOrderByVersionNoDesc(group);
        return versions.isEmpty() ? null : versions.get(0).getId();
    }

    /**
     * 创建一条新的版本记录
     *
     * @param group       分组名称
     * @param description 版本说明
     * @param userId      操作人ID，可为null
     * @return 插入后的版本实体（含ID）
     */
    private ViewRouteVersionEntity createVersionRecord(String group, String description, Long userId) {
        ViewRouteVersionEntity versionEntity = new ViewRouteVersionEntity();
        versionEntity.setRouteGroup(group);
        versionEntity.setVersionNo(viewRouteVersionMapper.countByGroup(group) + 1);
        versionEntity.setDescription(description);
        versionEntity.setFlag(1);
        versionEntity.setCreateBy(userId);
        versionEntity.setCreateTime(new Date());
        viewRouteVersionMapper.insert(versionEntity);
        return versionEntity;
    }

    /**
     * 递归复制路由树（回滚用）
     * <p>
     * 将旧版本指定层级下的路由行复制为新版本的新行，并同步复制元数据与权限角色
     * </p>
     *
     * @param oldRoutes   旧版本的全部路由行
     * @param oldParentId 旧父路由ID（顶层为null）
     * @param newParentId 新父路由ID（顶层为null）
     * @param newVersionId 新版本ID
     * @param userId      操作人ID，可为null
     */
    private void copyRouteTree(List<ViewRouteConfigrationEntity> oldRoutes, Long oldParentId, Long newParentId,
        Long newVersionId, Long userId) {
        // 筛选出当前层级的旧路由行，按排序值排序
        List<ViewRouteConfigrationEntity> currentLevel = oldRoutes.stream().filter(route -> {
            if (oldParentId == null) {
                return route.getParentId() == null;
            }
            return oldParentId.equals(route.getParentId());
        }).sorted(Comparator.comparingInt(r -> r.getSortOrder() == null ? 0 : r.getSortOrder()))
            .collect(Collectors.toList());

        for (ViewRouteConfigrationEntity oldRoute : currentLevel) {
            // 复制路由配置行
            ViewRouteConfigrationEntity newRoute = new ViewRouteConfigrationEntity();
            newRoute.setRouteGroup(oldRoute.getRouteGroup());
            newRoute.setPath(oldRoute.getPath());
            newRoute.setComponent(oldRoute.getComponent());
            newRoute.setName(oldRoute.getName());
            newRoute.setRedirect(oldRoute.getRedirect());
            newRoute.setHidden(oldRoute.getHidden());
            newRoute.setAlwaysShow(oldRoute.getAlwaysShow());
            newRoute.setParentId(newParentId);
            newRoute.setSortOrder(oldRoute.getSortOrder());
            newRoute.setVersion(newVersionId);
            newRoute.setFlag(1);
            newRoute.setCreateTime(new Date());
            newRoute.setCreateBy(userId);
            viewRouteConfigrationMapper.insert(newRoute);

            // 复制元数据
            ViewRouteMetaEntity oldMeta = viewRouteMetaMapper.findByRouteId(oldRoute.getId());
            if (oldMeta != null) {
                ViewRouteMetaEntity newMeta = new ViewRouteMetaEntity();
                newMeta.setRouteId(newRoute.getId());
                newMeta.setRouteGroup(oldMeta.getRouteGroup());
                newMeta.setTitle(oldMeta.getTitle());
                newMeta.setIcon(oldMeta.getIcon());
                newMeta.setBreadcrumb(oldMeta.getBreadcrumb());
                newMeta.setActiveMenu(oldMeta.getActiveMenu());
                newMeta.setFlag(1);
                viewRouteMetaMapper.insert(newMeta);
            }

            // 复制权限角色
            List<ViewRouteRoleEntity> oldRoles = viewRouteRoleMapper.findByRouteId(oldRoute.getId());
            for (ViewRouteRoleEntity oldRole : oldRoles) {
                ViewRouteRoleEntity newRole = new ViewRouteRoleEntity();
                newRole.setRouteId(newRoute.getId());
                newRole.setRoleCode(oldRole.getRoleCode());
                newRole.setFlag(1);
                viewRouteRoleMapper.insert(newRole);
            }

            // 递归复制子路由
            copyRouteTree(oldRoutes, oldRoute.getId(), newRoute.getId(), newVersionId, userId);
        }
    }

    /**
     * 构建路由树
     *
     * @param routeEntities 路由配置实体列表
     * @param parentId      父路由ID
     * @return 路由模型列表
     */
    private List<ViewRouteModel> buildRouteTree(List<ViewRouteConfigrationEntity> routeEntities, Long parentId) {
        List<ViewRouteModel> result = new ArrayList<>();

        // 筛选出当前层级的路由
        List<ViewRouteConfigrationEntity> currentLevelRoutes = routeEntities.stream().filter(route -> {
            if (parentId == null) {
                return route.getParentId() == null;
            } else {
                return parentId.equals(route.getParentId());
            }
        }).sorted(Comparator.comparingInt(r -> r.getSortOrder() == null ? 0 : r.getSortOrder()))
            .collect(Collectors.toList());

        for (ViewRouteConfigrationEntity routeEntity : currentLevelRoutes) {
            ViewRouteModel routeModel = convertToRouteModel(routeEntity);

            // 递归构建子路由
            List<ViewRouteModel> children = buildRouteTree(routeEntities, routeEntity.getId());
            if (!children.isEmpty()) {
                routeModel.setChildren(children);
            }

            result.add(routeModel);
        }

        return result;
    }

    /**
     * 将实体转换为路由模型
     *
     * @param routeEntity 路由配置实体
     * @return 路由模型
     */
    private ViewRouteModel convertToRouteModel(ViewRouteConfigrationEntity routeEntity) {
        ViewRouteModel routeModel = new ViewRouteModel();
        routeModel.setPath(routeEntity.getPath());
        routeModel.setComponent(routeEntity.getComponent());
        routeModel.setName(routeEntity.getName());
        routeModel.setRedirect(routeEntity.getRedirect());

        // 设置隐藏属性
        if (routeEntity.getHidden() != null) {
            routeModel.setHidden(routeEntity.getHidden() == 1);
        }

        // 设置始终显示属性
        if (routeEntity.getAlwaysShow() != null) {
            routeModel.setAlwaysShow(routeEntity.getAlwaysShow() == 1);
        }

        // 设置排序顺序
        routeModel.setSortOrder(routeEntity.getSortOrder());

        // 获取元数据
        ViewRouteMetaEntity metaEntity = viewRouteMetaMapper.findByRouteId(routeEntity.getId());
        if (metaEntity != null) {
            ViewRouteMetaModel metaModel = new ViewRouteMetaModel();
            metaModel.setRouteGroup(metaEntity.getRouteGroup());
            metaModel.setTitle(metaEntity.getTitle());
            metaModel.setIcon(metaEntity.getIcon());

            // 设置面包屑显示
            if (metaEntity.getBreadcrumb() != null) {
                metaModel.setBreadcrumb(metaEntity.getBreadcrumb() == 1);
            }

            metaModel.setActiveMenu(metaEntity.getActiveMenu());

            // 获取权限角色
            List<ViewRouteRoleEntity> roleEntities = viewRouteRoleMapper.findByRouteId(routeEntity.getId());
            if (!roleEntities.isEmpty()) {
                List<String> roles =
                    roleEntities.stream().map(ViewRouteRoleEntity::getRoleCode).collect(Collectors.toList());
                metaModel.setRoles(roles);
            }

            routeModel.setMeta(metaModel);
        }

        return routeModel;
    }

    /**
     * 递归保存路由模型
     *
     * @param routeModel 路由模型
     * @param parentId   父路由ID
     * @param routeGroup 路由分组
     * @param versionId  所属版本ID
     * @param userId     操作人ID，可为null
     */
    private void saveRouteModel(ViewRouteModel routeModel, Long parentId, String routeGroup, Long versionId,
        Long userId) {
        // 1. 保存路由配置
        ViewRouteConfigrationEntity routeEntity = new ViewRouteConfigrationEntity();
        routeEntity.setRouteGroup(routeGroup);
        routeEntity.setPath(routeModel.getPath());
        routeEntity.setComponent(routeModel.getComponent());
        routeEntity.setName(routeModel.getName());
        routeEntity.setRedirect(routeModel.getRedirect());
        routeEntity.setHidden(routeModel.getHidden() != null && routeModel.getHidden() ? 1 : 0);
        routeEntity.setAlwaysShow(routeModel.getAlwaysShow() != null && routeModel.getAlwaysShow() ? 1 : 0);
        routeEntity.setParentId(parentId);
        routeEntity.setSortOrder(routeModel.getSortOrder() != null ? routeModel.getSortOrder() : 0); // 使用前端传入的排序值，默认为0
        routeEntity.setVersion(versionId);
        routeEntity.setFlag(1); // 启用状态
        routeEntity.setCreateTime(new Date());
        routeEntity.setCreateBy(userId);

        viewRouteConfigrationMapper.insert(routeEntity);

        // 2. 保存元数据
        if (routeModel.getMeta() != null) {
            ViewRouteMetaEntity metaEntity = new ViewRouteMetaEntity();
            metaEntity.setRouteId(routeEntity.getId());
            metaEntity.setRouteGroup(routeGroup);
            metaEntity.setTitle(routeModel.getMeta().getTitle());
            metaEntity.setIcon(routeModel.getMeta().getIcon());
            metaEntity.setBreadcrumb(
                routeModel.getMeta().getBreadcrumb() != null && routeModel.getMeta().getBreadcrumb() ? 1 : 0);
            metaEntity.setActiveMenu(routeModel.getMeta().getActiveMenu());
            metaEntity.setFlag(1);

            viewRouteMetaMapper.insert(metaEntity);

            // 3. 保存权限角色
            if (routeModel.getMeta().getRoles() != null && !routeModel.getMeta().getRoles().isEmpty()) {
                for (String roleCode : routeModel.getMeta().getRoles()) {
                    ViewRouteRoleEntity roleEntity = new ViewRouteRoleEntity();
                    roleEntity.setRouteId(routeEntity.getId());
                    roleEntity.setRoleCode(roleCode);
                    roleEntity.setFlag(1);

                    viewRouteRoleMapper.insert(roleEntity);
                }
            }
        }

        // 4. 递归保存子路由
        if (routeModel.getChildren() != null && !routeModel.getChildren().isEmpty()) {
            for (ViewRouteModel child : routeModel.getChildren()) {
                saveRouteModel(child, routeEntity.getId(), routeGroup, versionId, userId);
            }
        }
    }

    /**
     * 获取当前登录用户ID（审计用）
     * <p>
     * 沿用 AuthUtil + ISystemUserService 模式，取不到时返回 null 不阻断业务
     * </p>
     *
     * @return 当前用户ID，无法获取时返回null
     */
    private Long getCurrentUserId() {
        try {
            var systemUserService = SpringUtil.getBeanOptional(ISystemUserService.class);
            if (systemUserService.isEmpty()) {
                return null;
            }
            var currentUser = AuthUtil.getInstance().getCurrentUser();
            if (currentUser == null || currentUser.getUsername() == null) {
                return null;
            }
            return CollUtil.getFirst(systemUserService.get().queryUserIdsByUserName(currentUser.getUsername()));
        } catch (Exception e) {
            log.warn("获取当前登录用户ID失败，审计信息留空: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将版本实体转换为版本模型
     *
     * @param entity 版本实体
     * @return 版本模型
     */
    private ViewRouteVersionModel toVersionModel(ViewRouteVersionEntity entity) {
        ViewRouteVersionModel model = new ViewRouteVersionModel();
        model.setId(entity.getId());
        model.setRouteGroup(entity.getRouteGroup());
        model.setVersionNo(entity.getVersionNo());
        model.setDescription(entity.getDescription());
        model.setCreateBy(entity.getCreateBy());
        model.setCreateTime(entity.getCreateTime());
        return model;
    }

    /**
     * 刷新路由缓存
     * <p>
     * 从数据库重新加载所有路由配置
     * </p>
     */
    public void refreshCache() {
        loadAllRoutesFromDatabase();
        log.info("路由缓存已刷新，当前视图版本: {}", getViewVersion());
    }
}
