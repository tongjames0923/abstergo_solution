package com.tbs.sql.framework.extractor.impls.mysql;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.sql.enums.QueryConnectorEnum;
import com.tbs.sql.enums.QueryContrastEnum;
import com.tbs.sql.framework.converter.IValueConverter;
import com.tbs.sql.framework.extractor.IWhereSqlExtractor;
import com.tbs.sql.framework.model.SqlWhereCompose;
import com.tbs.sql.framework.model.SqlWhereInfo;
import com.tbs.sql.framework.model.SqlWhereNode;
import com.tbs.sql.properties.SqlProperty;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class MysqlWhereExtractor implements IWhereSqlExtractor {
    @Resource
    private SqlProperty sqlProperty;

    private List<IValueConverter> valueConverters;

    @PostConstruct
    void init() {
        if (sqlProperty.isUseRawValue()) {
            log.warn("[TBS-SQL] useRawValue已启用，SQL参数将直接拼接，存在SQL注入风险！请确保所有输入值都经过严格校验。");
        }
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
        } else if (QueryContrastEnum.IN == contrastEnum || QueryContrastEnum.NOT_IN == contrastEnum) {
            return "(" + placeholder + ")";
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
                AssertUtil.throwError(
                    () -> new FormattedRuntimeException("不支持的查询条件：{}", queryContrastEnum).errorLevel(
                        ErrorLevelEnum.CRITICAL_SYSTEM_ERROR));
        }
        return "";
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

        // 处理IN和NOT IN操作符以及Collection类型参数
        if ((info.getContrast() == QueryContrastEnum.IN || info.getContrast() == QueryContrastEnum.NOT_IN) &&
            val instanceof Collection) {
            Collection<?> collection = (Collection<?>)val;
            if (!collection.isEmpty()) {
                if ((info.getUseSafe() != null && info.getUseSafe()) || !sqlProperty.isUseRawValue()) {
                    // 安全模式：使用参数占位符
                    List<String> placeholders = new ArrayList<>();
                    int index = 0;
                    for (Object item : collection) {
                        String itemKey = key + "_" + index++;
                        placeholders.add("#{_prs." + itemKey + "}");
                        paramMap.put(itemKey, item);
                    }
                    valStr = "(" + String.join(",", placeholders) + ")";
                } else {
                    // 非安全模式：直接拼接值（需转义防止SQL注入）
                    List<String> values = new ArrayList<>();
                    for (Object item : collection) {
                        String convertedValue = escapeSqlString(convert(item));
                        values.add("'" + convertedValue + "'");
                    }
                    valStr = "(" + String.join(",", values) + ")";
                }

                String name = info.getFieldName();
                String constract = contrast(info.getContrast());
                return String.format(" (%s %s %s) ", name, constract, valStr);
            } else {
                // 空集合处理：IN空集合恒假(1=0)，NOT IN空集合恒真(1=1)
                if (info.getContrast() == QueryContrastEnum.IN) {
                    return " (1 = 0) ";
                } else {
                    return " (1 = 1) ";
                }
            }
        }

        // 处理IS NULL和IS NOT NULL操作符不需要参数值
        if (info.getContrast() == QueryContrastEnum.IS_NULL || info.getContrast() == QueryContrastEnum.IS_NOT_NULL) {
            String name = info.getFieldName();
            String constract = contrast(info.getContrast());
            return String.format(" (%s %s) ", name, constract);
        }

        if ((info.getUseSafe() != null && info.getUseSafe()) || !sqlProperty.isUseRawValue()) {
            valStr = String.format("#{_prs.%s}", key);
            if (val != null && val instanceof String) {
                val = contrastStrSet(info.getContrast(), val.toString());
            }
            paramMap.put(key, val);
        } else {
            Object value = info.getFieldValue();
            valStr = String.format("'%s'", escapeSqlString(convert(value)));
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

    /**
     * 转义SQL字符串中的特殊字符，防止SQL注入
     * 将单引号(')转义为两个单引号('')
     *
     * @param value 原始字符串值
     * @return 转义后的字符串
     */
    static String escapeSqlString(String value) {
        if (value == null) {
            return null;
        }
        // 转义单引号：' -> ''
        return value.replace("'", "''");
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
