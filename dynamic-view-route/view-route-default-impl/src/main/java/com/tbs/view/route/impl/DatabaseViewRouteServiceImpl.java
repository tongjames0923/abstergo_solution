package com.tbs.view.route.impl;

import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.shared.mapper.ViewRouteConfigrationMapper;
import com.tbs.shared.mapper.ViewRouteMetaMapper;
import com.tbs.shared.mapper.ViewRouteRoleMapper;
import com.tbs.shared.pojo.entity.ViewRouteConfigrationEntity;
import com.tbs.shared.pojo.entity.ViewRouteMetaEntity;
import com.tbs.shared.pojo.entity.ViewRouteRoleEntity;
import com.tbs.view.route.api.IViewRouteService;
import com.tbs.view.route.model.ViewRouteMetaModel;
import com.tbs.view.route.model.ViewRouteModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于数据库的视图路由服务实现类
 * <p>
 * 该实现从数据库加载路由配置，支持动态编辑和热更新
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

    /**
     * 路由配置缓存，按分组存储
     */
    private Map<String, List<ViewRouteModel>> routeCache = new HashMap<>(16);

    /**
     * 路由配置版本号，用于热更新检测
     */
    private volatile long configVersion = 0;

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
    public void saveViewRoute(String group, List<ViewRouteModel> viewRouteModels) {
        AssertUtil.isTrue(viewRouteModels == null || viewRouteModels.isEmpty()).message("路由配置不能为空")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

        try {
            // 1. 删除该分组下的所有现有路由配置
            deleteViewRoute(group);

            // 2. 保存新的路由配置
            for (ViewRouteModel routeModel : viewRouteModels) {
                saveRouteModel(routeModel, null, group);
            }

            // 3. 更新缓存
            routeCache.put(group, viewRouteModels);

            // 4. 更新版本号，触发热更新
            configVersion++;

            log.info("成功保存路由分组: {}，包含 {} 个路由", group, viewRouteModels.size());
        } catch (Exception e) {
            log.error("保存路由配置失败: {}", group, e);
            AssertUtil.isTrue(true).cause(e).errorLevel(ErrorLevelEnum.SYSTEM_ERROR).message("保存路由配置失败")
                .throwIt();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteViewRoute(String group) {
        try {
            // 1. 查询该分组下的所有路由ID
            List<ViewRouteConfigrationEntity> existingRoutes = viewRouteConfigrationMapper.findByRouteGroup(group);
            if (existingRoutes.isEmpty()) {
                return;
            }

            List<Long> routeIds =
                existingRoutes.stream().map(ViewRouteConfigrationEntity::getId).collect(Collectors.toList());

            // 2. 删除路由权限关联
            for (Long routeId : routeIds) {
                ViewRouteRoleEntity roleQuery = new ViewRouteRoleEntity();
                roleQuery.setRouteId(routeId);
                viewRouteRoleMapper.delete(roleQuery);
            }

            // 3. 删除路由元数据
            for (Long routeId : routeIds) {
                ViewRouteMetaEntity metaQuery = new ViewRouteMetaEntity();
                metaQuery.setRouteId(routeId);
                viewRouteMetaMapper.delete(metaQuery);
            }

            // 4. 删除路由配置
            for (ViewRouteConfigrationEntity route : existingRoutes) {
                viewRouteConfigrationMapper.delete(route);
            }

            // 5. 清除缓存
            routeCache.remove(group);

            // 6. 更新版本号
            configVersion++;

            log.info("成功删除路由分组: {}", group);
        } catch (Exception e) {
            log.error("删除路由配置失败: {}", group, e);
            AssertUtil.isTrue(true).cause(e).errorLevel(ErrorLevelEnum.SYSTEM_ERROR).message("删除路由配置失败")
                .throwIt();
        }
    }

    @Override
    public void startUp() throws RuntimeException {
        try {
            // 从数据库加载所有路由配置
            loadAllRoutesFromDatabase();
            log.info("从数据库加载路由配置完成，共加载 {} 个分组", routeCache.size());
        } catch (Exception e) {
            log.error("从数据库加载路由配置失败", e);
            AssertUtil.isTrue(true).cause(e).errorLevel(ErrorLevelEnum.SYSTEM_ERROR).message("从数据库加载路由配置失败")
                .throwIt();
        }
    }

    /**
     * 从数据库加载所有路由配置
     */
    private void loadAllRoutesFromDatabase() {
        // 清空缓存
        routeCache.clear();

        // 查询所有启用的路由配置
        List<ViewRouteConfigrationEntity> allRoutes = viewRouteConfigrationMapper.findAllEnabled();

        // 按分组分组
        Map<String, List<ViewRouteConfigrationEntity>> groupedRoutes =
            allRoutes.stream().collect(Collectors.groupingBy(ViewRouteConfigrationEntity::getRouteGroup));

        // 转换为ViewRouteModel
        for (Map.Entry<String, List<ViewRouteConfigrationEntity>> entry : groupedRoutes.entrySet()) {
            String group = entry.getKey();
            List<ViewRouteConfigrationEntity> routeEntities = entry.getValue();

            // 构建路由树
            List<ViewRouteModel> routeModels = buildRouteTree(routeEntities, null);
            routeCache.put(group, routeModels);
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
        }).sorted(Comparator.comparingInt(ViewRouteConfigrationEntity::getSortOrder)).collect(Collectors.toList());

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
     */
    private void saveRouteModel(ViewRouteModel routeModel, Long parentId, String routeGroup) {
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
        routeEntity.setSortOrder(0); // 默认排序
        routeEntity.setFlag(1); // 启用状态

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
                saveRouteModel(child, routeEntity.getId(), routeGroup);
            }
        }
    }

    /**
     * 获取配置版本号，用于热更新检测
     *
     * @return 配置版本号
     */
    public long getConfigVersion() {
        return configVersion;
    }

    /**
     * 刷新路由缓存
     * <p>
     * 从数据库重新加载所有路由配置
     * </p>
     */
    public void refreshCache() {
        loadAllRoutesFromDatabase();
        configVersion++;
        log.info("路由缓存已刷新，当前版本: {}", configVersion);
    }
}
