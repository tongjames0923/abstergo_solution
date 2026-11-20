package com.tbs.app.service.getter;

import com.tbs.app.service.user.model.ISystemUserDetails;
import com.tbs.app.vo.DepartmentInfoVO;
import com.tbs.script.api.expressible.vo.*;
import com.tbs.web.uitl.AuthUtil;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * 用户部门取值器。
 *
 * <p>判断当前登录用户是否属于指定部门，或当前部门是否为指定部门。
 * 未登录、部门 ID 非法或取值异常时返回 false，不中断脚本执行。</p>
 *
 * @author abstergo
 */
@Component
public class UserDepartmentGetter extends AbstractBooleanValueGetter {

    /**
     * 匹配范围枚举值：所属任一部门
     */
    private static final String SCOPE_ANY = "any";

    /**
     * 匹配范围枚举值：当前部门
     */
    private static final String SCOPE_CURRENT = "current";

    /**
     * 文案 key 前缀
     */
    private static final String I18N_PREFIX = "value.getter.user-department.";

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
        return "UserDepartmentGetter";
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
     * 声明参数定义：部门 ID 与匹配范围。
     *
     * @return 参数定义列表
     */
    @Override
    public List<ParamType> requestParamTypes() {
        List<EnumValue> scopes = List.of(
            EnumValue.builder().value(SCOPE_ANY).label(text(I18N_PREFIX + "type.any")).build(),
            EnumValue.builder().value(SCOPE_CURRENT).label(text(I18N_PREFIX + "type.current")).build());
        return List.of(
            ParamType.builder().type("long").name(text(I18N_PREFIX + "param.id.name"))
                .description(text(I18N_PREFIX + "param.id.description")).required(true).sort(1)
                .ui(ParamUiMeta.builder().inputType("number").min(BigDecimal.ONE)
                    .step(BigDecimal.ONE).build()).build(),
            ParamType.builder().isEnum(true).type("string").name(text(I18N_PREFIX + "param.scope.name"))
                .description(text(I18N_PREFIX + "param.scope.description")).enumValues(scopes).sort(2)
                .ui(ParamUiMeta.builder().inputType("select").build()).build());
    }

    /**
     * 判断当前用户是否匹配指定部门。
     *
     * @param paramValues 参数值集合，首个参数为部门 ID，第二个参数为匹配范围
     * @return 匹配返回 true，否则返回 false
     */
    @Override
    public ResultValue getValue(Collection<ParamValue> paramValues) {
        Long departmentId = parseDepartmentId(paramString(paramValues, 0));
        if (departmentId == null) {
            return booleanResult(false);
        }
        try {
            UserDetails user = authUtil.getCurrentUser();
            if (!(user instanceof ISystemUserDetails details)) {
                return booleanResult(false);
            }
            if (SCOPE_CURRENT.equals(paramString(paramValues, 1))) {
                DepartmentInfoVO current = details.getCurrentDepartment();
                return booleanResult(current != null && departmentId.equals(current.getDepartmentId()));
            }
            List<DepartmentInfoVO> departments = details.getDepartments();
            return booleanResult(departments != null
                && departments.stream().anyMatch(dept -> departmentId.equals(dept.getDepartmentId())));
        } catch (Exception e) {
            return booleanResult(false);
        }
    }

    /**
     * 解析部门 ID。
     *
     * @param raw 原始字符串
     * @return 部门 ID；非法时返回 null
     */
    private Long parseDepartmentId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
