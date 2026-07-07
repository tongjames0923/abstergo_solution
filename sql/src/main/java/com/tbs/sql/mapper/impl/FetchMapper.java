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

/**
 * 查询结果获取Mapper类，负责执行SQL查询并转换结果。
 *
 * <p>该类提供了执行SQL查询并获取结果的方法，支持查询列表和统计总数。
 * 内部使用 {@link IRawSqlSelectMapper} 执行SQL，并将结果转换为指定类型的对象。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>执行SQL查询并返回对象列表 {@link #fetch}</li>
 *   <li>执行SQL统计并返回总数 {@link #count}</li>
 *   <li>自动将Map结果转换为实体对象</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在SQL查询引擎中执行查询</li>
 *   <li>将SQL结果转换为实体对象</li>
 *   <li>执行分页查询</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 执行查询
 * FetchMapper fetchMapper = new FetchMapper();
 * List<User> users = fetchMapper.fetch(context, driver, User.class);
 *
 * // 执行统计
 * Long count = fetchMapper.count(context, driver);
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>需要注入 {@link IRawSqlSelectMapper}</li>
 *   <li>确保SQL语句和参数正确</li>
 *   <li>结果转换可能会丢失部分字段</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see IRawSqlSelectMapper
 * @see SqlBuildContext
 * @see SqlDriver
 */
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

    public <T> Integer count(SqlBuildContext context, SqlDriver driver) {
        return mapper.count(QueryEngine.getInstance().driveSql(context, driver), context.getSqlParams());
    }
}
