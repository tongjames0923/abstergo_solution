package com.tbs.sql.mapper.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import com.tbs.common.util.base.BeanUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.sql.builder.SqlBuildContext;
import com.tbs.sql.builder.SqlDriver;
import com.tbs.sql.mapper.IRawSqlSelectMapper;
import com.tbs.sql.service.QueryEngine;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FetchMapper {
    @Resource
    private IRawSqlSelectMapper mapper;

    public <T> List<T> fetch(SqlBuildContext context, SqlDriver driver, Class<T> tClass) {
        List<Map<String, Object>> val = mapper.fetch(QueryEngine.getInstance().driveSql(context, driver), context.getSqlParams());
        if (CollUtil.isNotEmpty(val)) {
            List<T> list = new ArrayList<>(val.size());
            for (Map<String, Object> map : val) {
                list.add(BeanUtil.<T>mapToBean(map, tClass, true, CopyOptions.create()));
            }
            return list;
        }
        return new ArrayList<>();
    }

    public <T> Long count(SqlBuildContext context, SqlDriver driver) {
        return mapper.count(QueryEngine.getInstance().driveSql(context, driver), context.getSqlParams());
    }
}
