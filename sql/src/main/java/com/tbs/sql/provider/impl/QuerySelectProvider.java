package com.tbs.sql.provider.impl;

import com.tbs.sql.model.FetchObject;
import com.tbs.sql.query.IPage;
import com.tbs.sql.query.IQueryCriteria;
import com.tbs.sql.utils.QueryUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author abstergo
 */
public class QuerySelectProvider {

    public QuerySelectProvider() {
    }

    /**
     * 获取查询的sql
     *
     * @param _query
     * @param _compose
     * @param _extractor
     * @param <T>
     * @return
     */
    public <T> String entityCountSql(FetchObject _fetchObject, Map<String, Object> _prs) {
        if (_prs == null) {
            _prs = new HashMap<>();
        }
        FetchObject newObject = new FetchObject(_fetchObject.getEntity(), null, _fetchObject.getWhereCompose(),
                null, _fetchObject.getSqlGroupModels());
        String sql = QueryUtil.getInstance().getQueryByInfos(newObject, _prs);
        return "select count(*) from (" + sql + ") as t1";
    }

    public String dynamicSql(IQueryCriteria _query, IPage _page, Map<String, Object> _prs) throws IllegalAccessException {
        final QueryUtil queryUtil = QueryUtil.getInstance();
        if (_prs == null) {
            _prs = new HashMap<>();
        }
        return queryUtil.getQuery(_query, _page, _prs);
    }

    public String dynamicCountSql(IQueryCriteria _query, Map<String, Object> _prs) throws IllegalAccessException {
        final QueryUtil queryUtil = QueryUtil.getInstance();
        if (_prs == null) {
            _prs = new HashMap<>();
        }
        String q = queryUtil.getQuery(_query, null, _prs);
        return "select count(*) from (" + q + ") as t1";
    }

    public <T> String entitySql(FetchObject _fetchObject, Map<String, Object> _prs) {
        if (_prs == null) {
            _prs = new HashMap<>();
        }
        return QueryUtil.getInstance().getQueryByInfos(_fetchObject, _prs);
    }
}
