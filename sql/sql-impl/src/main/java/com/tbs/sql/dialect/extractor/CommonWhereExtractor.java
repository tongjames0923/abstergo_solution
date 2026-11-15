package com.tbs.sql.dialect.extractor;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.dialect.IWhereExtractor;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.model.where.WhereCompose;
import com.tbs.sql.model.where.WhereInfo;
import com.tbs.sql.model.where.WhereNode;
import com.tbs.sql.util.SqlIdentifierUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * 通用WHERE语句提取器，适用于MySQL、PostgreSQL、SQL Server、Oracle等数据库。
 *
 * <p>所有主流关系型数据库的WHERE条件语法（比较运算符、LIKE、IN、IS NULL等）完全一致，
 * 参数化绑定均通过MyBatis的 {@code #{_prs.p0}} 机制处理（与数据库无关），
 * 因此使用单一公共实现，通过构造函数传入方言名称用于日志标识。</p>
 *
 * <p>条件值一律经 MyBatis 预处理参数绑定，不会以字面量形式拼入 SQL 文本。</p>
 *
 * <p>实现说明：条件树只遍历一次并直接写入 StringBuilder，不再为每层构造
 * LinkedList/Map.Entry 中间集合；占位符与条件片段用直接拼接代替 String.format；
 * 参数序号使用 long[] 避免 Long 拆装箱。生成的 SQL 文本与参数键序列与旧实现逐字节一致。</p>
 *
 * @author abstergo
 * @since 2.0.0
 * @see IWhereExtractor
 * @see WhereCompose
 */
@Slf4j
public class CommonWhereExtractor implements IWhereExtractor {

    private final String dialectName;

    /**
     * 构造函数，指定方言名称用于日志输出。
     *
     * @param dialectName 方言名称，如 "MySQL"、"PostgreSQL"、"Oracle" 等
     */
    public CommonWhereExtractor(String dialectName) {
        this.dialectName = dialectName;
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

    String singleWhereInfo(WhereInfo info, long id, Map<String, Object> paramMap) {
        if (StrUtil.isNotEmpty(info.getRawSql())) {
            return info.getRawSql();
        }
        String key = makeParamKey(id);
        Object val = info.getFieldValue();
        if (StrUtil.isNotEmpty(info.getFieldValueStr())) val = info.getFieldValueStr();
        String fieldName = SqlIdentifierUtils.column(info.getFieldName());
        ContrastEnum contrast = info.getContrast();

        if ((contrast == ContrastEnum.IN || contrast == ContrastEnum.NOT_IN)
            && val instanceof Collection) {
            Collection<?> collection = (Collection<?>) val;
            if (!collection.isEmpty()) {
                /*
                 * IN / NOT IN 集合的每个元素各占一个预处理参数，取值不进入 SQL 文本。
                 */
                StringBuilder placeholders = new StringBuilder(collection.size() * 16);
                int index = 0;
                for (Object item : collection) {
                    String itemKey = key + "_" + index++;
                    if (index > 1) placeholders.append(',');
                    placeholders.append("#{_prs.").append(itemKey).append('}');
                    paramMap.put(itemKey, item);
                }
                return " (" + fieldName + " " + contrast(contrast) + " (" + placeholders + ")) ";
            }
            return "(1=1)";
        }

        if (contrast == ContrastEnum.IS_NULL || contrast == ContrastEnum.IS_NOT_NULL) {
            return " (" + fieldName + " " + contrast(contrast) + ") ";
        }

        /*
         * 单值条件同样只下标占位符：LIKE 类比较先给参数值补上 % 再绑定，SQL 文本里不出现字面值。
         */
        String valStr = "#{_prs." + key + "}";
        if (val instanceof String) val = contrastStrSet(contrast, (String) val);
        paramMap.put(key, val);
        return " (" + fieldName + " " + contrast(contrast) + " " + valStr + ") ";
    }

    private String extraWhereSql(WhereCompose compose, long[] id, Map<String, Object> paramMap) {
        if (compose == null || CollUtil.isEmpty(compose.getChildren())) return "";
        StringBuilder sb = new StringBuilder(64);
        boolean first = true;
        for (WhereNode node : compose.getChildren()) {
            if (node.isWhereInfo()) {
                first = appendNode(sb, node.getConnector(),
                    singleWhereInfo((WhereInfo) node.getValue(), id[0]++, paramMap), first);
            }
            if (node.isCompose()) {
                first = appendNode(sb, node.getConnector(),
                    extraWhereSql((WhereCompose) node.getValue(), id, paramMap), first);
            }
        }
        return sb.toString();
    }

    /**
     * 追加一个已生成的条件片段；返回更新后的「是否仍为首片段」标志。
     * 空片段直接跳过且不消耗连接符，与原实现「先收集再拼装」的行为一致。
     */
    private static boolean appendNode(StringBuilder sb, ConnectorEnum connector, String value, boolean first) {
        if (StrUtil.isEmpty(value)) return first;
        if (!first) {
            sb.append(connector == null || connector == ConnectorEnum.AND ? " AND " : " OR ");
        }
        sb.append(" ( ").append(value).append(" ) ");
        return false;
    }

    @Override
    public String extract(WhereCompose sqlWhereCompose, Map<String, Object> paramMap) {
        String where = extraWhereSql(sqlWhereCompose, new long[]{0L}, paramMap).trim();
        return StrUtil.isEmpty(where) ? "" : " WHERE " + where;
    }

    String makeParamKey(long id) {
        return "param_" + id;
    }
}
