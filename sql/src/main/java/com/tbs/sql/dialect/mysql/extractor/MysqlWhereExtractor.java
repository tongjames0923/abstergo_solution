package com.tbs.sql.dialect.mysql.extractor;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.sql.converter.IValueConverter;
import com.tbs.sql.dialect.IWhereExtractor;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.model.where.WhereCompose;
import com.tbs.sql.model.where.WhereInfo;
import com.tbs.sql.model.where.WhereNode;
import com.tbs.sql.properties.SqlProperty;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

@Slf4j
public class MysqlWhereExtractor implements IWhereExtractor {
    @Resource
    private SqlProperty sqlProperty;
    private List<IValueConverter> valueConverters;

    @PostConstruct
    void init() {
        if (sqlProperty.isUseRawValue()) {
            log.warn("[TBS-SQL] useRawValue已启用，SQL参数将直接拼接，存在SQL注入风险！");
        }
        try {
            valueConverters = SpringUtil.getBeansOfType(IValueConverter.class).values().stream()
                .sorted(Comparator.comparingInt(IValueConverter::getPriority)).collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            valueConverters = new ArrayList<>();
        }
    }

    @Nullable
    private static String contrastStrSet(ContrastEnum contrastEnum, String placeholder) {
        if (ContrastEnum.IS_NOT_NULL == contrastEnum || ContrastEnum.IS_NULL == contrastEnum) return "";
        else if (ContrastEnum.LLIKE == contrastEnum) return placeholder + "%";
        else if (ContrastEnum.RLIKE == contrastEnum) return "%" + placeholder;
        else if (ContrastEnum.LIKE == contrastEnum) return "%" + placeholder + "%";
        else if (ContrastEnum.IN == contrastEnum || ContrastEnum.NOT_IN == contrastEnum) return "(" + placeholder + ")";
        return placeholder;
    }

    private String contrast(final ContrastEnum contrastEnum) {
        switch (contrastEnum) {
            case EQUAL: return " = ";
            case LESS: return " < ";
            case GREATER: return " > ";
            case LESS_EQUAL: return " <= ";
            case GREATER_EQUAL: return " >= ";
            case RLIKE: case LLIKE: case LIKE: return " like ";
            case IN: return " in ";
            case NOT_IN: return " not in ";
            case NOT_LIKE: return " not like ";
            case IS_NULL: return " is null ";
            case IS_NOT_NULL: return " is not null ";
            case NOT_EQUAL: return " <> ";
            default:
                AssertUtil.throwError(() -> new FormattedRuntimeException("不支持的查询条件：{}", contrastEnum).errorLevel(ErrorLevelEnum.CRITICAL_SYSTEM_ERROR));
        }
        return "";
    }

    String singleWhereInfo(WhereInfo info, Long id, Map<String, Object> paramMap) {
        if (info.getRawSql() != null) return info.getRawSql();
        String key = makeParamKey(id);
        Object val = info.getFieldValue();
        if (StrUtil.isNotEmpty(info.getFieldValueStr())) val = info.getFieldValueStr();

        if ((info.getContrast() == ContrastEnum.IN || info.getContrast() == ContrastEnum.NOT_IN) && val instanceof Collection) {
            Collection<?> collection = (Collection<?>) val;
            if (!collection.isEmpty()) {
                if ((info.getUseSafe() != null && info.getUseSafe()) || !sqlProperty.isUseRawValue()) {
                    List<String> placeholders = new ArrayList<>();
                    int index = 0;
                    for (Object item : collection) {
                        String itemKey = key + "_" + index++;
                        placeholders.add("#{_prs." + itemKey + "}");
                        paramMap.put(itemKey, item);
                    }
                    return String.format(" (%s %s %s) ", info.getFieldName(), contrast(info.getContrast()), "(" + String.join(",", placeholders) + ")");
                } else {
                    List<String> values = new ArrayList<>();
                    for (Object item : collection) {
                        values.add("'" + escapeSqlString(convert(item)) + "'");
                    }
                    return String.format(" (%s %s %s) ", info.getFieldName(), contrast(info.getContrast()), "(" + String.join(",", values) + ")");
                }
            } else {
                return info.getContrast() == ContrastEnum.IN ? " (1 = 0) " : " (1 = 1) ";
            }
        }

        if (info.getContrast() == ContrastEnum.IS_NULL || info.getContrast() == ContrastEnum.IS_NOT_NULL) {
            return String.format(" (%s %s) ", info.getFieldName(), contrast(info.getContrast()));
        }

        String valStr;
        if ((info.getUseSafe() != null && info.getUseSafe()) || !sqlProperty.isUseRawValue()) {
            valStr = String.format("#{_prs.%s}", key);
            if (val != null && val instanceof String) val = contrastStrSet(info.getContrast(), val.toString());
            paramMap.put(key, val);
        } else {
            valStr = String.format("'%s'", escapeSqlString(convert(val)));
            valStr = contrastStrSet(info.getContrast(), valStr);
            paramMap.put(key, valStr);
        }
        return String.format(" (%s %s %s) ", info.getFieldName(), contrast(info.getContrast()), valStr);
    }

    private String extraWhereSql(WhereCompose compose, Long[] id, Map<String, Object> paramMap) {
        if (compose == null || CollUtil.isEmpty(compose.getChildren())) return "";
        List<Map.Entry<ConnectorEnum, String>> whereNode = new LinkedList<>();
        for (WhereNode node : compose.getChildren()) {
            if (node.isWhereInfo()) {
                WhereInfo info = (WhereInfo) node.getValue();
                whereNode.add(new AbstractMap.SimpleEntry<>(node.getConnector(), singleWhereInfo(info, id[0]++, paramMap)));
            }
            if (node.isCompose()) {
                WhereCompose subCompose = (WhereCompose) node.getValue();
                whereNode.add(new AbstractMap.SimpleEntry<>(node.getConnector(), extraWhereSql(subCompose, id, paramMap)));
            }
        }
        if (CollUtil.isEmpty(whereNode)) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<ConnectorEnum, String> entry : whereNode) {
            String value = entry.getValue();
            if (StrUtil.isEmpty(value)) continue;
            if (!first) sb.append(entry.getKey() == null || entry.getKey() == ConnectorEnum.AND ? " AND " : " OR ");
            sb.append(" ( ").append(value).append(" ) ");
            first = false;
        }
        return sb.toString();
    }

    @Override
    public String extract(WhereCompose sqlWhereCompose, Map<String, Object> paramMap) {
        String where = extraWhereSql(sqlWhereCompose, new Long[]{0L}, paramMap).trim();
        return StrUtil.isEmpty(where) ? "" : " where " + where;
    }

    static String escapeSqlString(String value) {
        if (value == null) return null;
        return value.replace("'", "''");
    }

    String convert(Object value) {
        for (IValueConverter converter : valueConverters) {
            if (value == null) { if (converter.support(null, null)) return converter.convert(null, null); }
            else { if (converter.support(value.getClass(), value)) return converter.convert(value.getClass(), value); }
        }
        return "";
    }

    @Override
    public List<IValueConverter> getValueConverters() { return valueConverters; }

    String makeParamKey(Long id) { return String.format("p%d", id); }
}
