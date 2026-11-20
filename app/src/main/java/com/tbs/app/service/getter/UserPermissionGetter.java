package com.tbs.app.service.getter;

import com.tbs.app.service.impl.web.PreAuthUtils;
import com.tbs.app.service.user.model.ISystemUserDetails;
import com.tbs.script.api.expressible.vo.*;
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
 * <p>判断当前登录用户是否拥有指定权限编码，支持接口权限与菜单权限两类。
 * 未登录、权限编码为空或取值异常时返回 false，不中断脚本执行。</p>
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
                .ui(ParamUiMeta.builder().inputType("select").build()).build());
    }

    /**
     * 判断当前用户是否拥有指定权限。
     *
     * @param paramValues 参数值集合，首个参数为权限编码，第二个参数为权限类型
     * @return 拥有权限返回 true，否则返回 false
     */
    @Override
    public ResultValue getValue(Collection<ParamValue> paramValues) {
        String code = paramString(paramValues, 0);
        if (code == null || code.isBlank()) {
            return booleanResult(false);
        }
        try {
            if (KIND_MENU.equals(paramString(paramValues, 1))) {
                return booleanResult(hasMenuPermission(code));
            }
            return booleanResult(PreAuthUtils.hasPermission(code));
        } catch (Exception e) {
            return booleanResult(false);
        }
    }

    /**
     * 从当前用户快照中判断菜单权限。
     *
     * @param code 权限编码
     * @return 是否拥有菜单权限
     */
    private boolean hasMenuPermission(String code) {
        UserDetails user = authUtil.getCurrentUser();
        if (!(user instanceof ISystemUserDetails details)) {
            return false;
        }
        Set<String> permissions = details.getMenuPermissions();
        return permissions != null && permissions.contains(code);
    }
}
