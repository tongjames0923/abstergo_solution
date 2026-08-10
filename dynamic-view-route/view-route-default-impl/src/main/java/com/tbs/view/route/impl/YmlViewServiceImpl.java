package com.tbs.view.route.impl;

import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.view.route.api.IViewRouteService;
import com.tbs.view.route.model.ViewRouteMetaModel;
import com.tbs.view.route.model.ViewRouteModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

/**
 * 路由服务实现类，用于加载和管理视图路由信息，支持从 YAML 文件中动态加载路由配置信息
 * @author tongj
 */
@Slf4j
public class YmlViewServiceImpl implements IViewRouteService, IStartup {

    private Map<String, List<ViewRouteModel>> viewRouteModelMap = new HashMap<>(16);

    @Override
    public List<String> getAllViewRouteGroups() {
        return new ArrayList<>(viewRouteModelMap.keySet());
    }

    @Override
    public List<ViewRouteModel> getViewRoutesByGroup(String group) {
        return new ArrayList<>(viewRouteModelMap.getOrDefault(group, Collections.emptyList()));
    }

    @Override
    public void saveViewRoute(String group, List<ViewRouteModel> viewRouteModel) {
        AssertUtil.isTrue(true, "功能尚未实现");
    }

    @Override
    public void deleteViewRoute(String group) {
        AssertUtil.isTrue(true, "功能尚未实现");
    }

    @Override
    public void startUp() throws RuntimeException {
        try {
            // 加载 YAML 文件
            ClassPathResource resource = new ClassPathResource("view-route.yml");
            if (!resource.exists()) {
                log.warn("view-route.yml 文件不存在，跳过路由加载");
                return;
            }

            InputStream inputStream = resource.getInputStream();
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(inputStream);

            // 解析 YAML 数据并转换为 ViewRouteModel
            for (Map.Entry<String, Object> entry : yamlData.entrySet()) {
                String group = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof List) {
                    List<ViewRouteModel> routeModels = parseRouteList((List<Map<String, Object>>) value);
                    viewRouteModelMap.put(group, routeModels);
                    log.info("成功加载路由分组: {}，包含 {} 个路由", group, routeModels.size());
                }
            }

            inputStream.close();
            log.info("路由配置加载完成，共加载 {} 个分组", viewRouteModelMap.size());

        } catch (Exception e) {
            log.error("加载路由配置失败", e);
            AssertUtil.isTrue(true).cause(e).errorLevel(ErrorLevelEnum.SYSTEM_ERROR).message("加载路由配置失败")
                .throwIt();
        }
    }

    /**
     * 解析路由列表
     */
    private List<ViewRouteModel> parseRouteList(List<Map<String, Object>> routeList) {
        List<ViewRouteModel> result = new ArrayList<>();

        for (Map<String, Object> routeData : routeList) {
            ViewRouteModel routeModel = parseRouteModel(routeData);
            if (routeModel != null) {
                result.add(routeModel);
            }
        }

        return result;
    }

    /**
     * 解析单个路由模型
     */
    private ViewRouteModel parseRouteModel(Map<String, Object> routeData) {
        try {
            ViewRouteModel routeModel = new ViewRouteModel();

            // 设置基本属性
            routeModel.setPath((String) routeData.get("path"));
            routeModel.setComponent((String) routeData.get("component"));
            routeModel.setName((String) routeData.get("name"));
            routeModel.setRedirect((String) routeData.get("redirect"));

            // 设置隐藏属性
            if (routeData.containsKey("hidden")) {
                routeModel.setHidden((Boolean) routeData.get("hidden"));
            }

            // 设置始终显示属性
            if (routeData.containsKey("alwaysShow")) {
                routeModel.setAlwaysShow((Boolean) routeData.get("alwaysShow"));
            }

            // 解析元数据
            if (routeData.containsKey("meta")) {
                Object metaData = routeData.get("meta");
                if (metaData instanceof Map) {
                    ViewRouteMetaModel metaModel = parseMetaModel((Map<String, Object>) metaData);
                    routeModel.setMeta(metaModel);
                }
            }

            // 解析子路由
            if (routeData.containsKey("children")) {
                Object childrenData = routeData.get("children");
                if (childrenData instanceof List) {
                    List<ViewRouteModel> children = parseRouteList((List<Map<String, Object>>) childrenData);
                    routeModel.setChildren(children);
                }
            }

            return routeModel;
        } catch (Exception e) {
            log.error("解析路由数据失败: {}", routeData, e);
            return null;
        }
    }

    /**
     * 解析元数据模型
     */
    private ViewRouteMetaModel parseMetaModel(Map<String, Object> metaData) {
        ViewRouteMetaModel metaModel = new ViewRouteMetaModel();

        metaModel.setRouteGroup((String) metaData.get("routeGroup"));
        metaModel.setTitle((String) metaData.get("title"));
        metaModel.setIcon((String) metaData.get("icon"));

        // 解析权限角色
        if (metaData.containsKey("roles")) {
            Object rolesData = metaData.get("roles");
            if (rolesData instanceof List) {
                metaModel.setRoles((List<String>) rolesData);
            }
        }

        // 设置面包屑显示
        if (metaData.containsKey("breadcrumb")) {
            metaModel.setBreadcrumb((Boolean) metaData.get("breadcrumb"));
        }

        // 设置激活菜单
        if (metaData.containsKey("activeMenu")) {
            metaModel.setActiveMenu((String) metaData.get("activeMenu"));
        }

        return metaModel;
    }
}
