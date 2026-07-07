package com.tbs.sql.dialect.oracle.extractor;

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

/**
 * Oracle WHERE语句提取器。
 *
 * <p>该类实现了 {@link IWhereExtractor} 接口，提供了Oracle特有的WHERE条件提取功能。
 * 通过MyBatis的命名参数机制（{@code #{_prs.p0}}）实现参数化查询，防止SQL注入。</p>
 *
 * <p>Oracle的WHERE语法与MySQL/SQL Server基本一致：
 * <ul>
 *   <li>不等于操作符使用 {@code <>}（标准SQL）</li>
 *   <li>LIKE语法一致</li>
 *   <li>参数绑定使用 {@code #{_prs.p0}} 格式（由MyBatis处理，与数据库无关）</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @since 2.0.0
 * @see IWhereExtractor
 * @see WhereCompose
 */
@Slf4j
public class OracleWhereExtractor implements IWhereExtractor {

    @Resource
    private SqlProperty sqlProperty;

    private List<IValueConverter> valueConverters;

    @PostConstruct
    void init() {
        if (sqlProperty.isUseRawValue()) {
            log.warn("[TBS-SQL-Oracle] useRawValue已启用，SQL参数将直接拼接，存在SQL注入风险！");
        }
        try {
            valueConverters = SpringUtil.getBeansOfType(IValueConverter.class).values().stream()
                .sorted(Comparator.comparingInt(IValueConverter::getPriority))
                .collect(java.util.stream.Collectors.toList());
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
        else if (ContrastEnum.IN == contrastEnum || ContrastEnum.NOT_IN == contrastEnum)
            return "(" + placeholder + ")";
        return placeholder;
    }

    private String contrast(final ContrastEnum contrastEnum) {
        switch (contrastEnum) {
            case EQUAL: return " = ";
            case LESS: return " < ";
            case GREATER: return " > ";
            case LESS_EQUAL: return " <= ";
            case GREATER_EQUAL: return " >= ";
            case RLIKE: case LLIKE: case LIKE: return " LIKE ";
            case IN: return " IN ";
            case NOT_IN: return " NOT IN ";
            case NOT_LIKE: return " NOT LIKE ";
            case IS_NULL: return " IS NULL ";
            case IS_NOT_NULL: return " IS NOT NULL ";
            case NOT_EQUAL: return " <> ";
            default:
                AssertUtil.throwError(
                    () -> new FormattedRuntimeException("不支持的查询条件：{}", contrastEnum)
                        .errorLevel(ErrorLevelEnum.CRITICAL_SYSTEM_ERROR));
        }
        return "";
    }

    String singleWhereInfo(WhereInfo info, Long id, Map<String, Object> paramMap) {
        if (info.getRawSql() != null) return info.getRawSql();
        String key = makeParamKey(id);
        Object val = info.getFieldValue();
        if (StrUtil.isNotEmpty(info.getFieldValueStr())) val = info.getFieldValueStr();

        if ((info.getContrast() == ContrastEnum.IN || info.getContrast() == ContrastEnum.NOT_IN)
            && val instanceof Collection) {
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
                    return String.format(" (%s %s %s) ", info.getFieldName(),
                        contrast(info.getContrast()),
                        "(" + String.join(",", placeholders) + ")");
                } else {
                    List<String> values = new ArrayList<>();
                    for (Object item : collection) {
                        values.add("'" + escapeSqlString(convert(item)) + "'");
                    }
                    return String.format(" (%s %s %s) ", info.getFieldName(),
                        contrast(info.getContrast()),
                        "(" + String.join(",", values) + ")");
                }
            } else {
                return info.getContrast() == ContrastEnum.IN ? " (1 = 1) " : " (1 = 1) ";
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
                whereNode.add(new AbstractMap.SimpleEntry<>(node.getConnector(),
                    singleWhereInfo(info, id[0]++, paramMap)));
            }
            if (node.isCompose()) {
                WhereCompose subCompose = (WhereCompose) node.getValue();
                whereNode.add(new AbstractMap.SimpleEntry<>(node.getConnector(),
                    extraWhereSql(subCompose, id, paramMap)));
            }
        }
        if (CollUtil.isEmpty(whereNode)) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<ConnectorEnum, String> entry : whereNode) {
            String value = entry.getValue();
            if (StrUtil.isEmpty(value)) continue;
            if (!first) sb.append(
                entry.getKey() == null || entry.getKey() == ConnectorEnum.AND ? " AND " : " OR ");
            sb.append(" ( ").append(value).append(" ) ");
            first = false;
        }
        return sb.toString();
    }

    @Override
    public String extract(WhereCompose sqlWhereCompose, Map<String, Object> paramMap) {
        String where = extraWhereSql(sqlWhereCompose, new Long[]{0L}, paramMap).trim();
        return StrUtil.isEmpty(where) ? "" : " WHERE " + where;
    }

    static String escapeSqlString(String value) {
        if (value == null) return null;
        return value.replace("'", "''");
    }

    String convert(Object value) {
        for (IValueConverter converter : valueConverters) {
            if (value == null) {
                if (converter.support(null, null)) return converter.convert(null, null);
            } else {
                if (converter.support(value.getClass(), value)) return converter.convert(value.getClass(), value);
            }
        }
        return "";
    }

    @Override
    public List<IValueConverter> getValueConverters() {
        return valueConverters;
    }

    String makeParamKey(Long id) {
        return String.format("p%d", id);
    }
}
