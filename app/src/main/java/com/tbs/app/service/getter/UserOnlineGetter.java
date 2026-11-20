package com.tbs.app.service.getter;

import com.tbs.script.api.expressible.vo.ParamType;
import com.tbs.script.api.expressible.vo.ParamUiMeta;
import com.tbs.script.api.expressible.vo.ParamValue;
import com.tbs.script.api.expressible.vo.ResultValue;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserLoginInfoService;
import com.tbs.web.security.user.vo.LoginSessionVO;
import com.tbs.web.uitl.AuthUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 用户在线状态取值器。
 *
 * <p>判断指定用户是否存在有效登录会话；用户名留空时判断当前登录用户。
 * 未登录、用户名无法解析或取值异常时返回 false，不中断脚本执行。</p>
 *
 * @author abstergo
 */
@Component
public class UserOnlineGetter extends AbstractBooleanValueGetter {

    /**
     * 文案 key 前缀
     */
    private static final String I18N_PREFIX = "value.getter.user-online.";

    /**
     * 登录会话服务
     */
    @Resource
    private IUserLoginInfoService userLoginInfoService;

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
        return "UserOnlineGetter";
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
     * 声明参数定义：用户名，可留空表示当前用户。
     *
     * @return 参数定义列表
     */
    @Override
    public List<ParamType> requestParamTypes() {
        return List.of(ParamType.builder().type("string").name(text(I18N_PREFIX + "param.username.name"))
            .description(text(I18N_PREFIX + "param.username.description")).sort(1)
            .ui(ParamUiMeta.builder().inputType("text").maxLength(128)
                .placeholder(text(I18N_PREFIX + "param.username.placeholder")).build()).build());
    }

    /**
     * 判断用户是否在线。
     *
     * @param paramValues 参数值集合，首个参数为用户名，可为空
     * @return 存在有效会话返回 true，否则返回 false
     */
    @Override
    public ResultValue getValue(Collection<ParamValue> paramValues) {
        String username = paramString(paramValues, 0);
        if (username == null || username.isBlank()) {
            username = currentUsername();
        }
        if (username == null || username.isBlank()) {
            return booleanResult(false);
        }
        try {
            List<LoginSessionVO> sessions = userLoginInfoService.listUserSessions(username);
            return booleanResult(sessions != null && sessions.stream().anyMatch(LoginSessionVO::isOnline));
        } catch (Exception e) {
            return booleanResult(false);
        }
    }

    /**
     * 安全读取当前登录用户名。
     *
     * @return 用户名；未登录或读取失败时返回 null
     */
    private String currentUsername() {
        try {
            ITokenModel token = authUtil.getCurrentTokenModel();
            return token == null ? null : token.getUserName();
        } catch (Exception e) {
            return null;
        }
    }
}
