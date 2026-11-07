-- ============================================================================
-- 泛化API配置（新版）页面的路由登记脚本
--
-- 说明：视图路由是「版本化」的（view_route_version + view_route_configration.version），
--       直接硬插一行会插到某个历史版本上而不生效。因此本脚本以当前生效版本为锚点，
--       并把新页面挂在旧「泛化API管理」的同级节点下，重复执行不会产生重复行。
--
-- 更推荐的做法：在「路由管理」界面复制 generic-api 节点新增一个同名分组下的节点，
--   path = generic-api-config，component = /admin/generic-api-config/index.vue，
--   name = view.menu.develop.generic.api.config（菜单文案 key 已在 messages-app*.properties 登记）。
--   界面登记会自动处理版本号与角色关联，本脚本只是给不便进界面时的等价路径。
-- ============================================================================

SET @route_group = 'default';

-- 当前生效的路由版本
SELECT id INTO @version_id
FROM view_route_version
WHERE route_group = @route_group AND flag = 1
ORDER BY version_no DESC LIMIT 1;

-- 以旧泛化API页面为锚点，取父节点与排序位置
SELECT parent_id, sort_order INTO @parent_id, @sibling_sort
FROM view_route_configration
WHERE route_group = @route_group AND path = 'generic-api' AND version = @version_id
ORDER BY sort_order LIMIT 1;

-- 新增页面路由（紧跟旧页面之后）
INSERT INTO view_route_configration (`id`, `route_group`, `path`, `component`, `name`, `redirect`, `hidden`,
    `always_show`, `parent_id`, `sort_order`, `flag`, `create_time`, `update_time`, `create_by`, `update_by`, `version`)
SELECT 822000000000000001, @route_group, 'generic-api-config', '/admin/generic-api-config/index.vue',
    'view.menu.develop.generic.api.config', NULL, 0, 0, @parent_id, @sibling_sort + 1, 1, NOW(), NOW(), NULL, NULL,
    @version_id
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM view_route_configration
    WHERE route_group = @route_group AND path = 'generic-api-config' AND version = @version_id) t);

-- 路由元数据（菜单标题与图标）
INSERT INTO view_route_meta (`id`, `route_id`, `route_group`, `title`, `icon`, `breadcrumb`, `active_menu`, `flag`,
    `create_time`, `update_time`)
SELECT 822000000000000002, 822000000000000001, @route_group, 'view.menu.develop.generic.api.config',
    'el-icon-connection', 1, NULL, 1, NOW(), NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM view_route_meta WHERE route_id = 822000000000000001) t);

-- 角色可见性：沿用旧泛化API页面的角色授权，未授权过则不新增（超级管理员默认可见全部菜单）
INSERT INTO view_route_role (`id`, `route_id`, `role_code`, `flag`, `create_time`, `update_time`)
SELECT 822000000000000003 + ROW_NUMBER() OVER (ORDER BY r.role_code), 822000000000000001, r.role_code, 1, NOW(), NULL
FROM view_route_role r
WHERE r.route_id = (SELECT id FROM view_route_configration
    WHERE route_group = @route_group AND path = 'generic-api' AND version = @version_id ORDER BY sort_order LIMIT 1)
    AND r.flag = 1
    AND NOT EXISTS (SELECT 1 FROM (SELECT 1 FROM view_route_role x WHERE x.route_id = 822000000000000001) t);
