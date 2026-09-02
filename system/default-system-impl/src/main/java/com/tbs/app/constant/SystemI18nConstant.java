package com.tbs.app.constant;

/**
 * 系统模块国际化常量
 * <p>
 * 集中收拢 system 模块中面向用户的 i18n 资源 key，避免调用方散落硬编码字符串魔法值。
 * key 对应资源文件见 {@code system/default-system-impl/src/main/resources/messages-system.properties}（中文）
 * 与 {@code messages-system_en_US.properties}（英文），新增 key 须中英文两份同步维护。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class SystemI18nConstant {

    private SystemI18nConstant() {
    }

    /* ===================== 认证与用户 ===================== */

    /** 用户未激活提示：用户未激活 */
    public static final String AUTH_USER_NOT_ACTIVATED = "auth.user.not.activated";
    /** 用户未启用提示：用户未启用 */
    public static final String AUTH_USER_NOT_ENABLED = "auth.user.not.enabled";
    /** 用户名已存在提示：用户名已存在 */
    public static final String USER_NAME_ALREADY_EXISTS = "user.name.already.exists";
    /** 用户名非法提示：用户名非法 */
    public static final String USER_NAME_INVALID = "user.name.invalid";
    /** 用户不存在提示：用户不存在 */
    public static final String USER_NOT_EXISTS = "user.not.exists";
    /** 用户未找到提示：用户未找到 */
    public static final String USER_NOT_FOUND = "user.not.found";
    /** 用户未登录提示：用户未登录 */
    public static final String USER_NOT_LOGINED = "user.not.logined";
    /** 用户密码错误提示：用户密码错误 */
    public static final String USER_PASSWORD_INVALID = "user.password.invalid";
    /** 用户状态非法提示：用户状态非法 */
    public static final String USER_STATUS_INVALID = "user.status.invalid";
    /** 用户已在部门中提示：用户已在该部门中 */
    public static final String USER_ALREADY_IN_DEPARTMENT = "user.already.in.department";
    /** 用户不在部门中提示：用户不在该部门中 */
    public static final String USER_NOT_IN_DEPARTMENT = "user.not.in.department";

    /* ===================== 公司 ===================== */

    /** 公司为空校验提示：公司信息不能为空 */
    public static final String COMPANY_EMPTY = "company.empty";
    /** 公司名称为空校验提示：公司名称不能为空 */
    public static final String COMPANY_NAME_EMPTY = "company.name.empty";
    /** 公司税号为空校验提示：公司税号不能为空 */
    public static final String COMPANY_TAX_NUMBER_EMPTY = "company.tax.number.empty";
    /** 公司税号已存在提示：公司税号已存在 */
    public static final String COMPANY_TAX_NUMBER_EXISTS = "company.tax.number.exists";
    /** 公司存在部门不可删除提示：公司下存在部门，无法删除 */
    public static final String COMPANY_HAS_DEPARTMENTS_NOT_DELETE = "company.has.departments.not.delete";
    /** 公司删除失败提示：公司删除失败 */
    public static final String COMPANY_DELETE_FAILED = "company.delete.failed";
    /** 公司非法提示：公司非法 */
    public static final String INVALID_COMPANY = "invalid.company";

    /* ===================== 部门 ===================== */

    /** 部门为空校验提示：部门信息不能为空 */
    public static final String DEPARTMENT_EMPTY = "department.empty";
    /** 部门编码为空校验提示：部门编码不能为空 */
    public static final String DEPARTMENT_CODE_EMPTY = "department.code.empty";
    /** 部门名称为空校验提示：部门名称不能为空 */
    public static final String DEPARTMENT_NAME_EMPTY = "department.name.empty";
    /** 部门所属公司为空校验提示：部门所属公司不能为空 */
    public static final String DEPARTMENT_COMPANY_EMPTY = "department.company.empty";
    /** 部门未找到提示：部门未找到 */
    public static final String DEPARTMENT_NOT_FOUND = "department.not.found";
    /** 部门存在子部门不可删除提示：部门下存在子部门，无法删除 */
    public static final String DEPARTMENT_HAS_CHILDREN_NOT_DELETE = "department.has.children.not.delete";
    /** 部门存在用户不可删除提示：部门下存在用户，无法删除 */
    public static final String DEPARTMENT_HAS_USERS_NOT_DELETE = "department.has.users.not.delete";
    /** 部门删除失败提示：部门删除失败 */
    public static final String DEPARTMENT_DELETE_FAILED = "department.delete.failed";
    /** 部门非法提示：部门非法 */
    public static final String INVALID_DEPARTMENT = "invalid.department";

    /* ===================== 权限 ===================== */

    /** 权限类型为空校验提示：权限类型不能为空 */
    public static final String PERMISSION_TYPE_EMPTY = "permission.type.empty";
    /** 权限类型非法提示：权限类型非法 */
    public static final String PERMISSION_TYPE_INVALID = "permission.type.invalid";
    /** 权限名称为空校验提示：权限名称不能为空 */
    public static final String PERMISSION_NAME_EMPTY = "permission.name.empty";
    /** 权限编码为空校验提示：权限编码不能为空 */
    public static final String PERMISSION_CODE_EMPTY = "permission.code.empty";
    /** 权限编码已存在提示：权限编码已存在 */
    public static final String PERMISSION_CODE_EXISTS = "permission.code.exists";
    /** 权限不存在提示：权限不存在 */
    public static final String PERMISSION_NOT_EXISTS = "permission.not.exists";
    /** 权限未启用提示：权限未启用 */
    public static final String PERMISSION_NOT_ACTIVE = "permission.not.active";
    /** 权限状态非法提示：权限状态非法 */
    public static final String PERMISSION_STATUS_INVALID = "permission.status.invalid";
    /** 系统权限不可删除提示：系统权限不可删除 */
    public static final String PERMISSION_SYSTEM_NOT_DELETE = "permission.system.not.delete";
    /** 系统权限不可修改提示：系统权限不可修改 */
    public static final String PERMISSION_SYSTEM_NOT_MODIFY = "permission.system.not.modify";
    /** 系统权限不可通过API设置提示：系统权限不可通过API设置 */
    public static final String PERMISSION_SYSTEM_NOT_SET_BY_API = "permission.system.not.set.by.api";
    /** 权限使用中不可删除提示：权限使用中，无法删除 */
    public static final String PERMISSION_IN_USE_NOT_DELETE = "permission.in.use.not.delete";
    /** 权限数据范围非法提示：权限数据范围非法 */
    public static final String PERMISSION_DATA_SCOPE_INVALID = "permission.data.scope.invalid";
    /** 权限分组名称为空校验提示：权限分组名称不能为空 */
    public static final String PERMISSION_GROUP_NAME_EMPTY = "permission.group.name.empty";
    /** 权限分组不存在提示：权限分组不存在 */
    public static final String PERMISSION_GROUP_NOT_EXISTS = "permission.group.not.exists";
    /** 权限分组未激活提示：权限分组未激活 */
    public static final String PERMISSION_GROUP_NOT_ACTIVE = "permission.group.not.active";
    /** 权限分组状态非法提示：权限分组状态非法 */
    public static final String PERMISSION_GROUP_STATUS_INVALID = "permission.group.status.invalid";
    /** 权限分组角色不存在提示：权限分组角色不存在 */
    public static final String PERMISSION_GROUP_ROLE_NOT_EXISTS = "permission.group.role.not.exists";
    /** 权限分组用户已存在提示：权限分组用户已存在 */
    public static final String PERMISSION_GROUP_USER_EXISTS = "permission.group.user.exists";
    /** 权限分组用户不存在提示：权限分组用户不存在 */
    public static final String PERMISSION_GROUP_USER_NOT_EXISTS = "permission.group.user.not.exists";

    /* ===================== 通用接口（Generic API） ===================== */

    /** 通用接口ID为空校验提示：通用接口ID不能为空 */
    public static final String GENERIC_API_ID_REQUIRED = "generic.api.id.required";
    /** 通用接口名称为空校验提示：通用接口名称不能为空 */
    public static final String GENERIC_API_NAME_REQUIRED = "generic.api.name.required";
    /** 通用接口所属应用为空校验提示：通用接口所属应用名称不能为空 */
    public static final String GENERIC_API_APP_NAME_REQUIRED = "generic.api.app.name.required";
    /** 通用接口接口名为空校验提示：通用接口接口名不能为空 */
    public static final String GENERIC_API_INTERFACE_NAME_REQUIRED = "generic.api.interface.name.required";
    /** 通用接口方法名为空校验提示：通用接口方法名不能为空 */
    public static final String GENERIC_API_METHOD_NAME_REQUIRED = "generic.api.method.name.required";
    /** 通用接口ID重复提示：通用接口ID已存在 */
    public static final String GENERIC_API_ID_DUPLICATE = "generic.api.id.duplicate";
    /** 通用接口未找到提示：通用接口不存在 */
    public static final String GENERIC_API_NOT_FOUND = "generic.api.not.found";
    /** 通用接口预授权Bean不存在提示：通用接口预授权Bean不存在：{0} */
    public static final String GENERIC_API_PRE_AUTH_BEAN_NOT_EXISTS = "generic.api.pre.auth.bean.not.exists";
    /** 通用接口开发者预授权失败提示：通用接口开发者预授权失败 */
    public static final String GENERIC_API_AUTH_DEVELOPER_PRE_AUTH_FAIL = "generic.api.auth.developer.pre.auth.fail";
    /** 通用接口超级管理员预授权失败提示：通用接口超级管理员预授权失败 */
    public static final String GENERIC_API_AUTH_SUPERADMIN_PRE_AUTH_FAIL = "generic.api.auth.superadmin.pre.auth.fail";

    /* ===================== 兼容策略（Policy） ===================== */

    /** 策略不存在提示：兼容策略不存在 */
    public static final String POLICY_NOT_EXISTS = "policy.not.exists";
    /** 策略源应用Key为空校验提示：兼容策略源应用Key不能为空 */
    public static final String POLICY_SOURCE_APP_KEY_REQUIRED = "policy.source.app.key.required";
    /** 策略目标应用Key为空校验提示：兼容策略目标应用Key不能为空 */
    public static final String POLICY_TARGET_APP_KEY_REQUIRED = "policy.target.app.key.required";
    /** 策略最小兼容版本为空校验提示：兼容策略最小兼容版本不能为空 */
    public static final String POLICY_MIN_COMPAT_VERSION_REQUIRED = "policy.min.compat.version.required";
    /** 策略动作（行为）为空校验提示：兼容策略动作不能为空 */
    public static final String POLICY_ACTION_REQUIRED = "policy.action.required";
    /** 策略动作（行为）非法提示：兼容策略动作非法 */
    public static final String POLICY_ACTION_INVALID = "policy.action.invalid";
    /** 策略源应用不存在提示：兼容策略源应用不存在 */
    public static final String POLICY_SOURCE_APP_NOT_EXISTS = "policy.source.app.not.exists";
    /** 策略目标应用不存在提示：兼容策略目标应用不存在 */
    public static final String POLICY_TARGET_APP_NOT_EXISTS = "policy.target.app.not.exists";

    /* ===================== 应用包（Package） ===================== */

    /** 应用包版本ID为空校验提示：应用包版本ID不能为空 */
    public static final String PACKAGE_VERSION_ID_REQUIRED = "package.version.id.required";
    /** 应用包下载信息为空校验提示：应用包下载信息不能为空 */
    public static final String PACKAGE_DOWNLOAD_INFO_REQUIRED = "package.download.info.required";

    /* ===================== 应用版本（Version） ===================== */

    /** 应用版本不存在提示：应用版本不存在 */
    public static final String VERSION_NOT_EXISTS = "version.not.exists";
    /** 应用版本应用Key为空校验提示：应用版本应用Key不能为空 */
    public static final String VERSION_APP_KEY_REQUIRED = "version.app.key.required";
    /** 应用版本号为空校验提示：应用版本号不能为空 */
    public static final String VERSION_CODE_REQUIRED = "version.code.required";
    /** 应用版本号重复提示：应用版本号已存在 */
    public static final String VERSION_CODE_DUPLICATE = "version.code.duplicate";
    /** 应用版本名称为空校验提示：应用版本名称不能为空 */
    public static final String VERSION_NAME_REQUIRED = "version.name.required";
    /** 应用版本类型为空校验提示：应用版本类型不能为空 */
    public static final String VERSION_TYPE_REQUIRED = "version.type.required";
    /** 应用版本类型非法提示：应用版本类型非法 */
    public static final String VERSION_TYPE_INVALID = "version.type.invalid";
    /** 应用版本平台为空校验提示：应用版本平台不能为空 */
    public static final String VERSION_PLATFORM_REQUIRED = "version.platform.required";
    /** 仅草稿状态可编辑提示：仅草稿状态的应用版本可编辑 */
    public static final String VERSION_ONLY_DRAFT_CAN_EDIT = "version.only.draft.can.edit";
    /** 仅草稿状态可删除提示：仅草稿状态的应用版本可删除 */
    public static final String VERSION_ONLY_DRAFT_CAN_DELETE = "version.only.draft.can.delete";
    /** 应用版本状态非草稿提示：应用版本状态非草稿 */
    public static final String VERSION_STATUS_NOT_DRAFT = "version.status.not.draft";
    /** 应用版本状态非待发布提示：应用版本状态非待发布 */
    public static final String VERSION_STATUS_NOT_PENDING = "version.status.not.pending";
    /** 应用版本状态非已发布提示：应用版本状态非已发布 */
    public static final String VERSION_STATUS_NOT_RELEASE = "version.status.not.release";
    /** 应用版本状态非已下线提示：应用版本状态非已下线 */
    public static final String VERSION_STATUS_NOT_OFFLINE = "version.status.not.offline";


    /* ===================== 通用校验 ===================== */

    /** 标识位（flag）非法提示：标识位非法 */
    public static final String INVALID_FLAG = "invalid.flag";
}
