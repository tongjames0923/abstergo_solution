package com.tbs.app.controller.privates;

import com.tbs.app.controller.constant.AppI18nContant;
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
