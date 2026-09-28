package com.tbs.app.controller.privates.dev;

import com.tbs.app.service.identity.IThirdPartyAppService;
import com.tbs.app.web.AbstractCrudController;
import com.tbs.common.data.PageVO;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.criteria.CommonThirdPartyAppCriteria;
import com.tbs.shared.pojo.entity.ThirdPartyAppEntity;
import com.tbs.web.result.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 第三方应用管理控制器
 * <p>
 * 提供第三方应用接出登记的增删改查能力。主体 CRUD 能力继承自 {@link AbstractCrudController}，
 * 本类仅负责接口的 HTTP 暴露与参数声明。接口位于私有分区（路径前缀 private），
 * 各写操作均要求账号处于已激活状态（{@code @PreAuthorize("@preAuthUtils.accountActived")}）。
 * 业务校验（代理用户存在性/激活态、授权类型合法性、应用标识唯一性、核心字段不可直接更新等）
 * 全部下沉至 {@link IThirdPartyAppService} 的实现类中，控制器不做规则判断。
 * </p>
 *
 * @author abstergo
 */
@RestController
@RequestMapping("private/admin/third-party")
@Tag(name = "第三方应用管理接口", description = "第三方应用接出登记管理相关操作接口")
public class ThirdPartyController
    extends AbstractCrudController<ThirdPartyAppEntity, Long, CommonThirdPartyAppCriteria> {

    @Resource
    private IThirdPartyAppService thirdPartyAppService;

    /**
     * 获取第三方应用服务实例
     * <p>基类 {@link AbstractCrudController} 的 CRUD 接口通过该实例执行具体业务操作。</p>
     *
     * @return 第三方应用服务实例
     */
    @Override
    public IServiceTemplate<ThirdPartyAppEntity, Long> getService() {
        return thirdPartyAppService;
    }

    /**
     * 根据 ID 获取第三方应用信息
     *
     * @param id 第三方应用 ID
     * @return 第三方应用详情
     */
    @Override
    @GetMapping("get/{id}")
    @AjaxResponseType(ThirdPartyAppEntity.class)
    @Operation(summary = "获取第三方应用", description = "根据 ID 获取第三方应用详情")
    @ApiResponse(responseCode = "200", description = "成功获取第三方应用",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult get(@Parameter(name = "id", description = "第三方应用ID", required = true) @PathVariable Long id) {
        return super.get(id);
    }

    /**
     * 保存第三方应用
     * <p>
     * 实体携带 ID 时按主键更新（服务层默认拒绝直接更新），未携带 ID 时按新增登记处理。
     * </p>
     *
     * @param entity 第三方应用实体
     * @return 保存后的第三方应用信息
     */
    @Override
    @PostMapping("save")
    @AjaxResponseType(ThirdPartyAppEntity.class)
    @Operation(summary = "保存第三方应用", description = "创建或更新第三方应用")
    @ApiResponse(responseCode = "200", description = "成功保存第三方应用",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult save(@Parameter(name = "entity", description = "第三方应用信息",
        required = true) @RequestBody ThirdPartyAppEntity entity) {
        return super.save(entity);
    }

    /**
     * 删除第三方应用
     * <p>采用逻辑删除（状态置为已删除），物理记录不移除。</p>
     *
     * @param id 第三方应用 ID
     * @return 删除结果
     */
    @Override
    @PostMapping("delete/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "删除第三方应用", description = "根据 ID 删除第三方应用")
    @ApiResponse(responseCode = "200", description = "成功删除第三方应用",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult delete(
        @Parameter(name = "id", description = "第三方应用ID", required = true) @PathVariable Long id) {
        return super.delete(id);
    }

    /**
     * 查询第三方应用列表
     * <p>
     * 分页参数走 query，查询条件走请求体；查询条件类标注了逻辑删除过滤，
     * 结果中不包含已删除的第三方应用记录。
     * </p>
     *
     * @param criteria 查询条件
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 第三方应用列表分页结果
     */
    @Override
    @PostMapping("query")
    @AjaxResponseType(value = PageVO.class, genericArgs = {ThirdPartyAppEntity.class})
    @Operation(summary = "查询第三方应用列表", description = "根据条件分页查询第三方应用列表")
    @ApiResponse(responseCode = "200", description = "成功查询第三方应用列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult query(@Parameter(name = "criteria", description = "查询条件") @RequestBody(
        required = false) CommonThirdPartyAppCriteria criteria,
        @Parameter(name = "pageNum", description = "页码") @RequestParam(required = false,
            defaultValue = "1") Long pageNum,
        @Parameter(name = "pageSize", description = "每页大小") @RequestParam(required = false,
            defaultValue = "10") Long pageSize) {
        return super.query(criteria, pageNum, pageSize);
    }

    /**
     * 获取当前控制器的业务名称
     *
     * @return 业务名称，用于日志/审计标识
     */
    @Override
    public String getBusinessName() {
        return "第三方应用管理";
    }
}
