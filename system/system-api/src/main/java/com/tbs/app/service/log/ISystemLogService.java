package com.tbs.app.service.log;

import com.tbs.app.service.log.model.LogOperateTypeView;
import com.tbs.logging.form.LoggingRecordExtractForm;
import com.tbs.logging.vo.LogRecordVO;
import com.tbs.common.data.PageVO;
import com.tbs.common.provider.annotation.RoutingService;

import java.util.List;

/**
 * 系统操作日志服务
 *
 *
 * @author abstergo*/
@RoutingService
public interface ISystemLogService {

    /**
     * 获取操作了行的数据报表
     *
     * @param lastDay 统计天数
     * @return
     */
    List<LogOperateTypeView> getLogOperateTypeList(int lastDay);

    /**
     * 获取所有不重复的操作类型列表(用于下拉框搜索)
     *
     * @param keyword 关键词,支持模糊搜索(可选)
     * @return 操作类型列表
     */
    List<String> getOperateTypeList(String keyword);

    /**
     * 获取操作日志分页列表
     *
     * @param form 查询条件
     * @return 分页结果
     */
    PageVO<LogRecordVO> getLogRecordList(LoggingRecordExtractForm form);

    /**
     * 按主键获取单条日志全量详情（含参数/结果/异常等大字段）。
     *
     * @param id 日志主键
     * @return 全量详情；不存在时返回 null
     */
    LogRecordVO getLogDetail(Long id);
}
