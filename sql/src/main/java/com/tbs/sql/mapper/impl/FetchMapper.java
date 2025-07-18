package com.tbs.sql.mapper.impl;

import cn.hutool.core.bean.copier.CopyOptions;
import com.tbs.common.util.BeanUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.sql.model.FetchObject;
import com.tbs.sql.model.SqlLimitModel;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author abstergo
 */
public class FetchMapper {
    @Resource
    private EntitySelectFetchMapper entitySelectFetchMapper;

    /**
     * 查询
     *
     * @param _query
     * @param _page
     * @param _compose
     * @param _extractor
     * @param _orderInfos
     * @param <T>
     * @return *
     */
    public <T> List<T> fetch(FetchObject fetchObject) {
        Map<String, Object> _prs = new HashMap<>();
        List<Map<String, Object>> val = entitySelectFetchMapper.fetch(fetchObject, _prs);
        if (CollUtil.isNotEmpty(val)) {
            List<T> list = new LinkedList<>();
            for (Map<String, Object> map : val) {
                list.add(BeanUtil.<T>mapToBean(map, (Class<T>)fetchObject.getEntity().getClass(), true,
                    CopyOptions.create()));
            }
            return list;
        }
        return new ArrayList<>();
    }

    /**
     * 查询单个
     *
     * @param _query
     * @param _page
     * @param _compose
     * @param _extractor
     * @param <T>
     * @return *
     */
    public <T> T fetchOne(FetchObject fetchObject) {
        Map<String, Object> _prs = new HashMap<>();
        fetchObject.setLimitModel(new SqlLimitModel(0L, 1L));
        List<Map<String, Object>> val = entitySelectFetchMapper.fetch(fetchObject, _prs);
        if (CollUtil.isNotEmpty(val)) {
            return BeanUtil.<T>mapToBean(CollUtil.first(val), (Class<T>)fetchObject.getEntity().getClass(), true,
                CopyOptions.create());
        }
        return null;
    }

    /**
     * 查询单个结果
     *
     * @param query
     * @param _compose
     * @param _extractor
     * @param <T>
     * @return
     */

    public <T> Long count(FetchObject fetchObject) {
        Map<String, Object> _prs = new HashMap<>();
        return entitySelectFetchMapper.count(fetchObject, _prs);
    }

}
