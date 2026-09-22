package com.tbs.app.controller.publics;

import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.access.grant.LoginGrantDescriptor;
import com.tbs.web.security.user.access.grant.LoginGrantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 公开登录授权控制器
 * <p>
 * 统一承载所有登录方式的 HTTP 接入：按授权类型分发到对应
 * {@code ILoginGrantProvider} 并签发访问令牌；同时导出当前启用的登录方式元数据。
 * 新增登录方式由 Provider SPI 注册，本控制器零改动（消除一码一接口的控制器膨胀）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/public/user")
@Tag(name = "公开登录授权控制器", description = "统一登录授权端点与启用登录方式下发")
@Slf4j
public class PublicLoginGrantController extends BaseController {

    @Resource
    LoginGrantService loginGrantService;

    /**
     * 统一登录端点
     * <p>
     * 按授权类型分发登录方式（如 password / wecom），凭证请求体由门面反序列化为
     * 各方式声明的强类型表单后委托签发令牌。
     * </p>
     *
     * @param grantType 授权类型标识，与前端登录方式 Provider id 对齐
     * @param payload   原始凭证请求体（各方式字段不同，由 Provider 表单契约约定）
     * @return 登录成功后的令牌信息
     */
    @PostMapping("/login/{grantType}")
    @AjaxResponseType(ITokenModel.class)
    @Operation(summary = "统一登录", description = "按授权类型分发登录方式并签发访问令牌")
    @ApiResponse(responseCode = "200", description = "成功登录",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult login(@Parameter(description = "授权类型标识，如 password / wecom") @PathVariable("grantType") String grantType,
        @Parameter(description = "凭证请求体，字段由各授权方式表单契约约定（如 refresh 授权类型无需请求体）") @RequestBody(required = false) Map<String, Object> payload) {
        ITokenModel tokenModel = loginGrantService.requestToken(grantType, payload);
        return successResult(tokenModel);
    }

    /**
     * 启用登录方式元数据下发
     * <p>
     * 导出当前环境启用的登录授权方式（授权类型、展示文案 key、公开参数），
     * 支撑前端登录方式清单由静态配置向后端下发演进。
     * </p>
     *
     * @return 启用登录方式元数据列表
     */
    @GetMapping("/login-grants")
    @AjaxResponseType(LoginGrantDescriptor.class)
    @Operation(summary = "启用登录方式清单", description = "导出当前环境启用的登录方式与公开参数")
    @ApiResponse(responseCode = "200", description = "成功导出",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult loginGrants() {
        List<LoginGrantDescriptor> grants = loginGrantService.listEnabledGrants();
        return successResult(grants);
    }

    /**
     * 获取当前控制器的业务名称
     * <p>
     * 实现 BaseController 接口的方法，返回当前控制器的业务名称
     * </p>
     *
     * @return 业务名称
     */
    @Override
    public String getBusinessName() {
        return "公开登录授权控制器";
    }
}
