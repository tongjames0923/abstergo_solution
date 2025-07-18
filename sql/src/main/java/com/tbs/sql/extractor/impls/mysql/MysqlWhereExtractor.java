package com.tbs.sql.extractor.impls.mysql;

import com.tbs.common.util.CollUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.sql.converter.IValueConverter;
import com.tbs.sql.enums.QueryConnectorEnum;
import com.tbs.sql.enums.QueryContrastEnum;
import com.tbs.sql.extractor.IWhereSqlExtractor;
import com.tbs.sql.model.SqlWhereCompose;
import com.tbs.sql.model.SqlWhereInfo;
import com.tbs.sql.model.SqlWhereNode;
import com.tbs.sql.properties.SqlProperty;
import org.jetbrains.annotations.Nullable;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mysql where条件解析器
 *
 * @author abstergo
 */
public class MysqlWhereExtractor implements IWhereSqlExtractor {
    @Resource
    private SqlProperty sqlProperty;

    private List<IValueConverter> valueConverters;

    @PostConstruct
    void init() {
        try {
            valueConverters = SpringUtil.getBeansOfType(IValueConverter.class).values().stream()
                .sorted(Comparator.comparingInt(IValueConverter::getPriority)).collect(Collectors.toList());
        } catch (Exception e) {
            valueConverters = new ArrayList<>();
        }
    }

    /**
     * 设置运算符的特殊格式
     *
     * @param queryContrastEnum
     * @return
     */
    @Nullable
    private static String contrastStrSet(QueryContrastEnum contrastEnum, String placeholder) {
        if (QueryContrastEnum.IS_NOT_NULL == contrastEnum || QueryContrastEnum.IS_NULL == contrastEnum) {
            return "";
        } else if (QueryContrastEnum.LLIKE == contrastEnum) {
            return placeholder + "%";
        } else if (QueryContrastEnum.RLIKE == contrastEnum) {
            return "%" + placeholder;
        } else if (QueryContrastEnum.LIKE == contrastEnum) {
            return "%" + placeholder + "%";
        }

        return placeholder;
    }

    /**
     * 获取运算符
     *
     * @param queryContrastEnum
     * @return
     */
    private String contrast(final QueryContrastEnum queryContrastEnum) {
        switch (queryContrastEnum) {
            case EQUAL:
                return " = ";
            case LESS:
                return " < ";
            case GREATER:
                return " > ";
            case LESS_EQUAL:
                return " <= ";
            case GREATER_EQUAL:
                return " >= ";
            case RLIKE:
            case LLIKE:
            case LIKE:
                return " like ";
            case IN:
                return " in ";
            case NOT_IN:
                return " not in ";
            case NOT_LIKE:
                return " not like ";
            case IS_NULL:
                return " is null ";
            case IS_NOT_NULL:
                return " is not null ";
            case NOT_EQUAL:
                return " <> ";
            default:
                throw new UnsupportedOperationException("不支持的运算符");
        }
    }

    String singleWhereInfo(SqlWhereInfo info, Long id, Map<String, Object> paramMap) {

        if (info.getRawSql() != null) {
            return info.getRawSql();
        }

        String key = makeParamKey(id);
        Object val = info.getFieldValue();
        String valStr = "";
        if (StrUtil.isNotEmpty(info.getFieldValueStr())) {
            val = info.getFieldValueStr();
        }

        if ((info.getUseSafe() != null && info.getUseSafe()) || !sqlProperty.isUseRawValue()) {
            valStr = String.format("#{_prs.%s}", key);
            if (val != null && val instanceof String) {
                val = contrastStrSet(info.getContrast(), val.toString());
            }
            paramMap.put(key, val);
        } else {
            Object value = info.getFieldValue();
            valStr = String.format("'%s'", convert(value));
            valStr = contrastStrSet(info.getContrast(), valStr);
            paramMap.put(key, valStr);
        }

        String name = info.getFieldName();
        String constract = contrast(info.getContrast());
        return String.format(" (%s %s %s) ", name, constract, valStr);
    }

    private String extraWhereSql(SqlWhereCompose sqlWhereCompose, Long[] id, Map<String, Object> paramMap) {

        if (sqlWhereCompose == null || CollUtil.isEmpty(sqlWhereCompose.getChildren())) {
            return "";
        }
        List<Map.Entry<QueryConnectorEnum, String>> whereNode = new LinkedList<>();
        for (SqlWhereNode node : sqlWhereCompose.getChildren()) {
            if (node.isWhereInfo()) {
                SqlWhereInfo info = (SqlWhereInfo)node.getValue();
                String s = singleWhereInfo(info, id[0]++, paramMap);
                whereNode.add(new AbstractMap.SimpleEntry<>(node.getConnector(), s));
            }
            if (node.isCompose()) {
                SqlWhereCompose compose = (SqlWhereCompose)node.getValue();
                String s = extraWhereSql(compose, id, paramMap);
                whereNode.add(new AbstractMap.SimpleEntry<>(node.getConnector(), s));
            }
        }
        if (CollUtil.isEmpty(whereNode)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<QueryConnectorEnum, String> entry : whereNode) {
            QueryConnectorEnum connector = entry.getKey();
            String value = entry.getValue();
            if (StrUtil.isEmpty(value)) {
                continue;
            }

            if (!first) {
                if (connector == null || connector == QueryConnectorEnum.AND) {
                    sb.append(" AND ");
                } else {
                    sb.append(" OR ");
                }
            }
            sb.append(" ( ");
            sb.append(value);
            sb.append(" ) ");
            first = false;
        }

        return sb.toString();
    }

    @Override
    public String extraWhereSql(SqlWhereCompose sqlWhereCompose, Map<String, Object> paramMap) {
        String where = extraWhereSql(sqlWhereCompose, new Long[] {0L}, paramMap).trim();
        return StrUtil.isEmpty(where) ? "" : " where " + where;
    }

    String convert(Object value) {
        String result = "";
        for (IValueConverter converter : getValueConverters()) {
            if (value == null) {
                if (converter.support(null, null)) {
                    return converter.convert(null, null);
                }
            } else {
                if (converter.support(value.getClass(), value)) {
                    return converter.convert(value.getClass(), value);
                }
            }
        }
        return result;
    }

    @Override
    public List<IValueConverter> getValueConverters() {
        return valueConverters;
    }

    String makeParamKey(Long id) {
        return String.format("p%d", id);
    }

}
