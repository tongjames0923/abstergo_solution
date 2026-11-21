package com.tbs.app.service.getter;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.impl.web.PreAuthUtils;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.service.user.IUserIdentityService;
import com.tbs.app.service.user.model.ISystemUserDetails;
import com.tbs.app.service.user.model.UserIdentity;
import com.tbs.script.api.expressible.vo.EnumValue;
import com.tbs.script.api.expressible.vo.param.ParamType;
import com.tbs.script.api.expressible.vo.param.ParamUiMeta;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import com.tbs.script.api.expressible.vo.result.ResultValue;
import com.tbs.web.uitl.AuthUtil;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 用户权限取值器。
 *
 * <p>判断指定用户是否拥有指定权限编码，支持接口权限与菜单权限两类；用户名留空时表示当前登录用户。
 * 用户不存在、未登录、权限编码为空或取值异常时返回 false，不中断脚本执行。</p>
 *
 * @author abstergo
 */
@Component
public class UserPermissionGetter extends AbstractBooleanValueGetter {

    /**
     * 权限类型枚举值：接口权限
     */
    private static final String KIND_API = "api";

    /**
     * 权限类型枚举值：菜单权限
     */
    private static final String KIND_MENU = "menu";

    /**
     * 文案 key 前缀
     */
    private static final String I18N_PREFIX = "value.getter.user-permission.";

    /**
     * 认证工具类
     */
    @Resource
    private AuthUtil authUtil;

    /**
     * 系统用户服务，用于按用户名反查用户 ID
     */
    @Resource
    private ISystemUserService systemUserService;

    /**
     * 用户身份聚合服务，用于读取任意用户的权限集合
     */
    @Resource
    private IUserIdentityService userIdentityService;

    /**
     * 系统设置，用于复用 API 权限总开关语义
     */
    @Resource
    private AppSettingProperty appSettingProperty;

    /**
     * 获取取值器名称。
     *
     * @return 国际化后的取值器名称
     */
    @Override
    public String getGetterName() {
        return text(I18N_PREFIX + "name");
    }

    /**
     * 获取取值器 ID。
     *
     * @return 取值器 ID
     */
    @Override
    public String getGetterId() {
        return "UserPermissionGetter";
    }

    /**
     * 获取取值器描述。
     *
     * @return 国际化后的取值器描述
     */
    @Override
    public String getDescription() {
        return text(I18N_PREFIX + "description");
    }

    /**
     * 声明参数定义：权限编码与权限类型。
     *
     * @return 参数定义列表
     */
    @Override
    public List<ParamType> requestParamTypes() {
        List<EnumValue> kinds = List.of(
            EnumValue.builder().value(KIND_API).label(text(I18N_PREFIX + "type.api")).build(),
            EnumValue.builder().value(KIND_MENU).label(text(I18N_PREFIX + "type.menu")).build());
        return List.of(
            ParamType.builder().type("string").name(text(I18N_PREFIX + "param.code.name"))
                .description(text(I18N_PREFIX + "param.code.description")).required(true).sort(1)
                .ui(ParamUiMeta.builder().inputType("text").maxLength(128)
                    .placeholder(text(I18N_PREFIX + "param.code.placeholder")).build()).build(),
            ParamType.builder().isEnum(true).type("string").name(text(I18N_PREFIX + "param.kind.name"))
                .description(text(I18N_PREFIX + "param.kind.description")).enumValues(kinds).sort(2)
                .ui(ParamUiMeta.builder().inputType("select").build()).build(),
            ParamType.builder().type("string").name(text(I18N_PREFIX + "param.username.name"))
                .description(text(I18N_PREFIX + "param.username.description")).sort(3)
                .ui(ParamUiMeta.builder().inputType("text").maxLength(128)
                    .placeholder(text(I18N_PREFIX + "param.username.placeholder")).build()).build());
    }

    /**
     * 判断当前用户是否拥有指定权限。
     *
     * @param paramValues 参数值集合，依次为权限编码、权限类型、用户名（可为空）
     * @return 拥有权限返回 true，否则返回 false
     */
    @Override
    public ResultValue getValue(Collection<ParamValue> paramValues) {
        String code = paramString(paramValues, 0);
        if (code == null || code.isBlank()) {
            return booleanResult(false);
        }
        String kind = paramString(paramValues, 1);
        String username = paramString(paramValues, 2);
        try {
            if (KIND_MENU.equals(kind)) {
                return booleanResult(hasMenuPermission(code, username));
            }
            return booleanResult(hasApiPermission(code, username));
        } catch (Exception e) {
            return booleanResult(false);
        }
    }

    /**
     * 判断指定用户是否拥有接口权限，用户名留空时沿用当前登录用户语义。
     *
     * @param code     权限编码
     * @param username 用户名，可为空
     * @return 是否拥有接口权限
     */
    private boolean hasApiPermission(String code, String username) {
        UserIdentity identity = UserIdentityLookup.resolve(systemUserService, userIdentityService, username);
        if (identity != null) {
            return UserIdentityLookup.isActive(identity)
                && (!appSettingProperty.isEnableApiPermission() || identity.getApiPermissions().contains(code));
        }
        return PreAuthUtils.hasPermission(code);
    }

    /**
     * 判断指定用户是否拥有菜单权限，用户名留空时读取当前用户快照。
     *
     * @param code     权限编码
     * @param username 用户名，可为空
     * @return 是否拥有菜单权限
     */
    private boolean hasMenuPermission(String code, String username) {
        UserIdentity identity = UserIdentityLookup.resolve(systemUserService, userIdentityService, username);
        if (identity != null) {
            return UserIdentityLookup.isActive(identity) && identity.getMenuPermissions().contains(code);
        }
        UserDetails user = authUtil.getCurrentUser();
        if (!(user instanceof ISystemUserDetails details)) {
            return false;
        }
        Set<String> permissions = details.getMenuPermissions();
        return permissions != null && permissions.contains(code);
    }
}
