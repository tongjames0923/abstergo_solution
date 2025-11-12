package com.tbs.app.service.log;

import com.tbs.app.service.log.model.LogOperateTypeView;

import java.util.List;

public interface ISystemLogService {

    /**
     * 获取操作了行的数据报表
     *
     * @param lastDay 统计天数
     * @return
     */
    List<LogOperateTypeView> getLogOperateTypeList(int lastDay);
}
