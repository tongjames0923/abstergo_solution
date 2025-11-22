package com.tbs.view.route.impl;

import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
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
        return viewRouteModelMap.getOrDefault(group, Collections.emptyList());
    }

    @Override
    public void saveViewRoute(String group, List<ViewRouteModel> viewRouteModel) {
        AssertUtil.isTrue(false, "功能尚未实现");
    }

    @Override
    public void deleteViewRoute(String group) {
        AssertUtil.isTrue(false, "功能尚未实现");
    }

    @Override
    public void startUp() throws RuntimeException {
        try {
            // 从 resource/view-route.yml 中加载路由信息
            ClassPathResource resource = new ClassPathResource("view-route.yml");
            if (!resource.exists()) {
                System.err.println("警告: view-route.yml 文件不存在，跳过路由加载");
                return;
            }

            Yaml yaml = new Yaml();
            try (InputStream inputStream = resource.getInputStream()) {
                // 解析 YAML 文件
                Map<String, Object> yamlData = yaml.load(inputStream);

                if (yamlData != null && yamlData.containsKey("routes")) {
                    @SuppressWarnings("unchecked")
                    Map<String, List<Map<String, Object>>> routesMap = (Map<String, List<Map<String, Object>>>) yamlData.get("routes");

                    // 清空现有缓存
                    viewRouteModelMap.clear();

                    // 遍历所有路由分组
                    for (Map.Entry<String, List<Map<String, Object>>> entry : routesMap.entrySet()) {
                        String group = entry.getKey();
                        List<Map<String, Object>> routeList = entry.getValue();

                        List<ViewRouteModel> viewRoutes = new ArrayList<>();

                        // 转换每个路由配置为 ViewRouteModel 对象
                        for (Map<String, Object> routeConfig : routeList) {
                            ViewRouteModel route = new ViewRouteModel();
                            route.setPath((String) routeConfig.get("path"));
                            route.setComponent((String) routeConfig.get("component"));
                            
                            // 设置元数据
                            ViewRouteMetaModel meta = new ViewRouteMetaModel();
                            meta.setRouteGroup((String) routeConfig.get("routeGroup"));
                            meta.setTitle((String) routeConfig.get("title"));
                            meta.setIcon((String) routeConfig.get("icon"));
                            
                            // 处理 roles 字段
                            Object rolesObj = routeConfig.get("roles");
                            if (rolesObj instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<String> roles = (List<String>) rolesObj;
                                meta.setRoles(new ArrayList<>(roles));
                            }
                            
                            // 处理 breadcrumb 字段
                            Object breadcrumbObj = routeConfig.get("breadcrumb");
                            if (breadcrumbObj != null) {
                                meta.setBreadcrumb((Boolean) breadcrumbObj);
                            }
                            
                            meta.setActiveMenu((String) routeConfig.get("activeMenu"));
                            route.setMeta(meta);

                            viewRoutes.add(route);
                        }

                        // 将路由信息保存到缓存中
                        viewRouteModelMap.put(group, viewRoutes);
                    }

                    System.out.println("成功加载路由配置，共 " + viewRouteModelMap.size() + " 个分组");
                } else {
                    System.err.println("警告: view-route.yml 文件中未找到有效的路由配置");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("加载路由配置失败: " + e.getMessage(), e);
        }
    }
}