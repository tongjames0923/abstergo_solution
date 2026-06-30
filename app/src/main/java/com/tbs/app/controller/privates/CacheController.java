package com.tbs.app.controller.privates;

import com.tbs.app.form.CacheForm;
import com.tbs.app.service.CacheListenner;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.constant.RoleConstants;
import com.tbs.web.BaseController;
import com.tbs.web.exception.NotFoundTokenException;
import com.tbs.web.result.AjaxResult;
import com.tbs.knife4j.annotation.AjaxResponseType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 缓存管理控制器
 * <p>
 * 该控制器用于管理系统缓存，提供缓存名称查询、缓存数据获取和缓存清理等功能
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("private/cache")
@PreAuthorize("hasAuthority('" + RoleConstants.DEVELOPER_ROLE_NAME + "')")
@Tag(name = "缓存管理接口", description = "缓存管理相关操作接口")
public class CacheController extends BaseController {

    @Resource
    private AbstractCacheManager cacheManager;

    @Resource
    private CacheListenner cacheListenner;

    /**
     * 获取所有缓存名称
     *
     * @return 所有缓存名称列表
     */
    @GetMapping("names")
    @AjaxResponseType(value = java.util.List.class, genericArgs = {String.class})
    @Operation(summary = "获取缓存名称列表", description = "获取系统中所有缓存的名称")
    @ApiResponse(responseCode = "200", description = "成功获取缓存名称列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult getCacheNames() {
        var cacheNames = cacheListenner.getCacheNames();
        return successResult(cacheNames);
    }

    /**
     * 获取缓存键列表
     *
     * @param form 缓存查询表单，包含缓存名称
     * @return 缓存键列表
     */
    @PostMapping("keys")
    @AjaxResponseType(value = java.util.List.class, genericArgs = {String.class})
    @Operation(summary = "获取缓存键列表", description = "根据缓存名称获取缓存键列表")
    @ApiResponse(responseCode = "200", description = "成功获取缓存键列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult getCacheKeys(
        @Parameter(name = "form", description = "缓存查询表单", required = true) @RequestBody CacheForm form) {
        if (form == null) {
            return successResult(null);
        }
        AssertUtil.isTrue(StrUtil.isEmpty(form.getCacheName()), () -> new NotFoundTokenException("未找到指定的缓存"));
        var keys = cacheListenner.getKeys(form.getCacheName());
        return successResult(keys);
    }

    /**
     * 根据缓存名称和键获取缓存值
     *
     * @param form 缓存查询表单，包含缓存名称和键
     * @return 缓存值，如果未找到则返回null
     */
    @PostMapping("get")
    @AjaxResponseType(Object.class)
    @Operation(summary = "获取缓存值", description = "根据缓存名称和键获取对应的缓存值")
    @ApiResponse(responseCode = "200", description = "成功获取缓存值",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult getCache(@Parameter(name = "form", description = "缓存查询表单", required = true) @RequestBody CacheForm form) {
        if (form == null) {
            return successResult(null);
        }
        AssertUtil.isTrue(StrUtil.isEmpty(form.getCacheName()) || form.getKey() == null,
            () -> new NotFoundTokenException("未找到指定的缓存"));
        var cache = cacheManager.getCache(form.getCacheName());
        var value = cache.get(form.getKey());
        if (value == null) {
            return successResult(null);
        }
        return successResult(value.get());
    }

    /**
     * 清除指定缓存
     *
     * @param form 缓存清除表单，包含缓存名称和可选的键
     */
    @PostMapping("evict")
    @AjaxResponseType(void.class)
    @Operation(summary = "清除缓存", description = "根据缓存名称清除缓存，如果指定键则只清除该键对应的缓存")
    @ApiResponse(responseCode = "200", description = "成功清除缓存",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult evictCache(@Parameter(name = "form", description = "缓存清除表单", required = true) @RequestBody CacheForm form) {
        AssertUtil.isTrue(StrUtil.isEmpty(form.getCacheName()), () -> new NotFoundTokenException("未找到指定的缓存"));
        if (form.getKey() == null) {
            cacheManager.getCache(form.getCacheName()).clear();
        } else {
            cacheManager.getCache(form.getCacheName()).evictIfPresent(form.getKey());
        }
        return successResult(null);
    }

    /**
     * 获取业务名称
     *
     * @return 业务名称
     */
    @Override
    public String getBusinessName() {
        return "缓存控制器";
    }
}
