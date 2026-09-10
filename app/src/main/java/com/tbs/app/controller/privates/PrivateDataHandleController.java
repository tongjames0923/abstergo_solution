package com.tbs.app.controller.privates;

import com.tbs.app.controller.constant.AppI18nContant;
import com.tbs.app.data.HandlerOptionVO;
import com.tbs.app.data.HandlerValidateVO;
import com.tbs.app.data.TableDataHandlerRegistry;
import com.tbs.common.data.ITableDataHandler;
import com.tbs.common.data.impl.CsvTableDataAdapter;
import com.tbs.common.data.structs.TableData;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.logging.annotation.NoLog;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据处理控制器
 * <p>
 * 提供 CSV 文件上传、解析与处理能力。解析后的 {@link TableData} 交由
 * {@link ITableDataHandler} 处理，并返回结构化 JSON 响应。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "数据处理")
@RestController
@RequestMapping("/private/data")
public class PrivateDataHandleController extends BaseController {

    /**
     * 处理器注册中心（支持本地 Spring 与 Dubbo 远程双模式查找）
     */
    @Resource
    private TableDataHandlerRegistry handlerRegistry;

    /**
     * CSV 适配器
     */
    @Resource
    private CsvTableDataAdapter csvAdapter;

    /**
     * 获取处理器选项列表
     * <p>
     * 返回本地 Spring 容器中注册的全部 {@link ITableDataHandler} 选项（ID 与名称），
     * 供前端下拉列表展示。Dubbo 远程处理器为按需发现，不在此列。
     * </p>
     *
     * @return 处理器选项列表
     */
    @NoLog
    @AjaxResponseType(value = List.class, genericArgs = {HandlerOptionVO.class})
    @PreAuthorize("@preAuthUtils.accountActived")
    @Operation(summary = "获取处理器选项列表", description = "返回本地注册的数据处理器选项列表，供前端下拉选择")
    @ApiResponse(responseCode = "200", description = "处理器选项列表")
    @GetMapping("/handler/list")
    public AjaxResult listHandlers() {
        List<HandlerOptionVO> options = handlerRegistry.getAllHandlers().stream()
            .map(handler -> new HandlerOptionVO(handler.getId(), handler.getName()))
            .collect(Collectors.toList());
        return successResult(options);
    }

    /**
     * 校验处理器ID有效性
     * <p>
     * 用于前端"可选可输入"模式下校验自定义输入的 handlerId：
     * 按"本地优先 → Dubbo 远程"顺序查找，返回可用性与命中来源。
     * </p>
     *
     * @param handlerId 处理器ID
     * @return 校验结果（available + source）
     */
    @NoLog
    @AjaxResponseType(HandlerValidateVO.class)
    @PreAuthorize("@preAuthUtils.accountActived")
    @Operation(summary = "校验处理器ID有效性", description = "校验自定义输入的处理器ID是否可用，返回可用性与命中来源（local/remote）")
    @ApiResponse(responseCode = "200", description = "校验结果")
    @GetMapping("/handler/validate/{handlerId}")
    public AjaxResult validateHandler(
        @Parameter(name = "handlerId", description = "处理器ID", required = true) @PathVariable String handlerId) {

        AssertUtil.isTrue(StrUtil.isEmpty(handlerId))
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(AppI18nContant.DATA_HANDLE_HANDLER_ID_EMPTY)
            .throwIt();

        ITableDataHandler handler = handlerRegistry.getHandler(handlerId);
        if (handler == null) {
            return successResult(new HandlerValidateVO(false, null));
        }
        boolean isLocal = handlerRegistry.getAllHandlers().stream()
            .anyMatch(item -> item.getId().equals(handlerId));
        return successResult(new HandlerValidateVO(true, isLocal ? "local" : "remote"));
    }

    /**
     * CSV 文件上传与解析
     * <p>
     * 接收 CSV 文件，通过 {@link CsvTableDataAdapter} 解析为 {@link TableData}，
     * 交由 {@code handlerId} 指定的 {@link ITableDataHandler} 处理后返回结构化数据。
     * </p>
     *
     * @param handlerId 数据处理器ID
     * @param file     CSV 文件
     * @return 解析后的表格数据
     */
    @NoLog
    @AjaxResponseType(TableData.class)
    @PreAuthorize("@preAuthUtils.accountActived")
    @Operation(summary = "CSV文件上传解析", description = "上传CSV文件，解析为表格数据并交由指定处理器处理")
    @ApiResponse(responseCode = "200", description = "解析后的表格数据")
    @PostMapping("/handle/csv/{handlerId}")
    public AjaxResult handleCsv(
        @Parameter(name = "handlerId", description = "数据处理器ID", required = true) @PathVariable String handlerId,
        @Parameter(name = "file", description = "CSV文件", required = true) @RequestParam("file") MultipartFile file) {

        AssertUtil.isTrue(file == null || file.isEmpty())
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(AppI18nContant.DATA_HANDLE_FILE_EMPTY)
            .throwIt();

        AssertUtil.isTrue(StrUtil.isEmpty(handlerId))
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(AppI18nContant.DATA_HANDLE_HANDLER_ID_EMPTY)
            .throwIt();

        ITableDataHandler handler = handlerRegistry.getHandler(handlerId);
        AssertUtil.isTrue(handler == null)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(AppI18nContant.DATA_HANDLE_HANDLER_NOT_FOUND, handlerId)
            .throwIt();

        try {
            TableData tableData = csvAdapter.adapt(file.getInputStream());
            handler.handle(tableData);
            return successResult(tableData);
        } catch (IOException e) {
            throw AssertUtil.isTrue(true)
                .errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
                .i18n("table.adapter.csv.parse.error", e.getMessage())
                .cause(e)
                .build();
        }
    }

    /**
     * 获取当前控制器的业务名称
     *
     * @return 业务名称，用于日志/审计标识
     */
    @Override
    public String getBusinessName() {
        return "数据处理操作";
    }
}
