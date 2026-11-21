package com.tbs.app.controller.privates.admin;

import com.tbs.app.form.DisableTokenForm;
import com.tbs.app.service.event.EventReportSupport;
import com.tbs.app.service.event.reporter.BusinessEventReporter;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.web.AbstractCrudController;
import com.tbs.common.data.PageVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.criteria.CommonSystemUserQueryCriteria;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.token.ITokenRemover;
import com.tbs.web.security.user.IUserLoginInfoService;
import com.tbs.web.security.user.vo.LoginSessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 管理员管理用户数据控制器
 * <p>
 * 该控制器用于管理员管理用户数据，提供用户的基本增删改查操作
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/private/admin/user")
@Tag(name = "管理员管理用户数据接口", description = "管理员管理用户数据接口")
public class AdminUserController extends AbstractCrudController<SystemUserEntity, Long, CommonSystemUserQueryCriteria> {
    @Resource
    private ISystemUserService service;

    @Resource
    IUserLoginInfoService loginInfoService;

    @Resource
    private ITokenIssuanceCenter center;

    @Resource
    private ITokenRemover tokenRemover;

    @Resource
    private EventReportSupport eventReportSupport;

    @Override
    public IServiceTemplate<SystemUserEntity, Long> getService() {
        return service;
    }

    /**
     * 根据ID获取用户数据信息
     *
     * <p>覆写父类实现，增加用户存在性校验：用户不存在时抛出 i18n 业务异常，
     * 避免前端详情页拿到 data 为 null 的成功响应。</p>
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Override
    @GetMapping("get/{id}")
    @AjaxResponseType(SystemUserEntity.class)
    @Operation(summary = "获取用户数据", description = "根据用户ID获取用户数据详细信息")
    @ApiResponse(responseCode = "200", description = "成功获取用户数据",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.QUERY.USER')")
    public AjaxResult get(@Parameter(name = "id", description = "用户ID", required = true) @PathVariable Long id) {
        var user = getService().queryById(id);
        AssertUtil.isTrue(user == null).i18n("user.not.exists").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        return successResult(user);
    }

    /**
     * 保存用户数据信息
     *
     * @param entity 用户实体对象
     * @return 保存后的用户信息
     */
    @Override
    @PostMapping("save")
    @AjaxResponseType(SystemUserEntity.class)
    @Operation(summary = "保存用户数据", description = "创建或更新用户数据信息")
    @ApiResponse(responseCode = "200", description = "成功保存用户数据",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.SAVE.USER')")
    public AjaxResult save(
        @Parameter(name = "entity", description = "用户信息", required = true) @RequestBody SystemUserEntity entity) {
        boolean created = entity != null && entity.getId() == null;
        AjaxResult result = super.save(entity);
        reportUserEvent(created ? BusinessEventReporter.EVENT_USER_CREATED : BusinessEventReporter.EVENT_USER_UPDATED,
            entity == null ? null : entity.getId(), entity == null ? null : entity.getUserName());
        return result;
    }

    /**
     * 删除用户数据
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @Override
    @PostMapping("delete/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "删除用户数据", description = "根据用户ID删除用户数据")
    @ApiResponse(responseCode = "200", description = "成功删除用户数据",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.SAVE.USER')")
    public AjaxResult delete(@Parameter(name = "id", description = "用户ID", required = true) @PathVariable Long id) {
        SystemUserEntity user = service.queryById(id);
        AjaxResult result = super.delete(id);
        reportUserEvent(BusinessEventReporter.EVENT_USER_DELETED, id, user == null ? null : user.getUserName());
        return result;
    }

    /**
     * 上报用户事件。
     *
     * <p>埋点失败的异常由上报门面吞掉，不影响主流程返回。</p>
     *
     * @param eventCode 事件编码
     * @param userId    用户ID
     * @param userName  用户名
     */
    private void reportUserEvent(String eventCode, Long userId, String userName) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(BusinessEventReporter.PARAM_USER_ID, userId);
        params.put(BusinessEventReporter.PARAM_USER_NAME, userName);
        eventReportSupport.reportQuietly(BusinessEventReporter.REPORTER_ID, eventCode, params);
    }

    /**
     * 查询用户数据列表
     *
     * @param criteria 查询条件
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 用户列表分页结果
     */
    @Override
    @PostMapping("query")
    @AjaxResponseType(PageVO.class)
    @Operation(summary = "查询用户数据", description = "根据条件查询用户数据列表，支持分页")
    @ApiResponse(responseCode = "200", description = "成功查询用户数据列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.QUERY.USER')")
    public AjaxResult query(
        @Parameter(name = "criteria", description = "查询条件") @RequestBody CommonSystemUserQueryCriteria criteria,
        @Parameter(name = "pageNum", description = "页码") @RequestParam(required = false) Long pageNum,
        @Parameter(name = "pageSize", description = "每页大小") @RequestParam(required = false) Long pageSize) {
        return super.query(criteria, pageNum, pageSize);
    }

    /**
     * 获取用户登录会话列表
     *
     * @param id 用户ID
     * @return 用户登录会话列表
     */
    @GetMapping("sessions/{id}")
    @Operation(summary = "获取用户登录会话列表", description = "根据用户ID获取用户登录会话列表")
    @ApiResponse(responseCode = "200", description = "成功获取用户登录会话列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginSessionVO.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.QUERY.LOGIN')")
    public List<LoginSessionVO> getUserSessions(
        @Parameter(name = "id", description = "用户ID", required = true) @PathVariable Long id) {
        var user = getService().queryById(id);
        AssertUtil.isTrue(user == null).message("用户不存在").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        return loginInfoService.listUserSessions(user.getUserName());
    }

    /**
     * 获取用户登录会话列表的Token数据
     *
     * @param sessionId 用户登录会话ID
     * @return 用户登录会话列表
     */
    @GetMapping("tokens/{sessionId}")
    @Operation(summary = "获取用户登录会话的Token数据", description = "根据会话ID获取用户登录会话的Token数据")
    @ApiResponse(responseCode = "200", description = "成功获取用户登录会话的Token数据",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ITokenModel.class)))
    @PreAuthorize("@preAuthUtils.hasPermission('API.QUERY.LOGIN')")
    public List<ITokenModel> listTokensBySessionId(@Parameter(name = "sessionId", description = "用户登录会话ID",
        required = true) @PathVariable String sessionId) {
        return center.getTokensBySessionId(sessionId).stream().collect(Collectors.toList());
    }

    /**
     * 禁用用户登录会话的Token数据
     *
     * @param token 用户登录会话的Token数据
     * @return 用户登录会话列表
     */
    @PostMapping("token/disable/")
    @AjaxResponseType(void.class)
    @PreAuthorize("@preAuthUtils.hasPermission('API.OPERATE.LOGIN')")
    public AjaxResult tokenDisable(@RequestBody @Valid DisableTokenForm form) {
        var user = getService().queryById(form.getUserId());
        AssertUtil.isTrue(user == null).message("用户不存在").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        var tokenModel = center.getTokenByToken(form.getToken());
        AssertUtil.isTrue(tokenModel == null).message("Token不存在").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        AssertUtil.isTrue(!Objects.equals(user.getUserName(), tokenModel.getUserName())).message("Token不属于该用户")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        // 经令牌移除器吊销而非直写状态：吊销后回调 ITokenCleanner 连带失效会话快照等派生缓存，
        // 否则 TTL 窗口内管理面板计数不更新；重复吊销已由移除器校验为“令牌已失效”
        tokenRemover.revokeToken(tokenModel);
        return successResult(null);
    }

    @Override
    public String getBusinessName() {
        return "管理员管理用户数据";
    }
}
