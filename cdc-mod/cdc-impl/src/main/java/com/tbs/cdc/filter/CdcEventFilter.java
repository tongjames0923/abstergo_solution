package com.tbs.cdc.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tbs.cdc.i18n.CdcI18nConstant;
import com.tbs.cdc.model.CdcEvent;
import com.tbs.cdc.property.CdcProperty;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * CDC事件过滤链
 * <p>
 * 在事件进入分发前按配置逐级判定：库白/黑名单、表级正则白/黑名单、行级SpEL条件。
 * 任一级不命中即丢弃事件。SpEL表达式按{@code db.table}预编译并缓存，避免重复解析。
 * 使用只读{@link SimpleEvaluationContext}限定表达式仅能访问数据变量，禁止类型引用与构造，
 * 防止不可信配置引入表达式注入风险。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "tbs.cdc", name = "enable", havingValue = "true", matchIfMissing = true)
public class CdcEventFilter {

    @Resource
    private CdcProperty cdcProperty;

    /**
     * SpEL表达式预编译缓存，key为表达式原文
     */
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    /**
     * 正则模式编译缓存，key为正则原文
     */
    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();

    /**
     * SpEL解析器（线程安全，可复用）
     */
    private final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 判定事件是否通过过滤链
     *
     * @param event 变更事件，非null
     * @return true表示通过（应分发），false表示被过滤丢弃
     */
    public boolean accepts(CdcEvent event) {
        CdcProperty.Filter filter = cdcProperty.getFilter();
        String database = event.getDatabase();
        String qualifiedTable = StrUtil.format("{}.{}", database, event.getTable());

        if (!matchDatabase(filter.getIncludeDatabases(), filter.getExcludeDatabases(), database)) {
            return false;
        }
        if (!matchTable(filter.getIncludeTablePatterns(), filter.getExcludeTablePatterns(), qualifiedTable)) {
            return false;
        }
        return matchRowCondition(filter.getRowConditions(), qualifiedTable, event);
    }

    /**
     * 库级白/黑名单判定
     *
     * @param include   白名单，空表示不限制
     * @param exclude   黑名单
     * @param database  库名
     * @return true表示库通过
     */
    private boolean matchDatabase(List<String> include, List<String> exclude, String database) {
        if (CollUtil.isNotEmpty(include) && !include.contains(database)) {
            return false;
        }
        return CollUtil.isEmpty(exclude) || !exclude.contains(database);
    }

    /**
     * 表级正则白/黑名单判定，命中白名单任一模式且不命中黑名单任一模式视为通过
     *
     * @param includePatterns  白名单正则集合
     * @param excludePatterns  黑名单正则集合
     * @param qualifiedTable   形如db.table的限定表名
     * @return true表示表通过
     */
    private boolean matchTable(List<String> includePatterns, List<String> excludePatterns, String qualifiedTable) {
        if (CollUtil.isNotEmpty(includePatterns) && !anyMatch(includePatterns, qualifiedTable)) {
            return false;
        }
        return CollUtil.isEmpty(excludePatterns) || !anyMatch(excludePatterns, qualifiedTable);
    }

    /**
     * 行级SpEL条件判定
     * <p>
     * 未配置该表条件时默认通过；配置的条件表达式求值结果非true时丢弃事件。
     * </p>
     *
     * @param rowConditions  行级条件映射
     * @param qualifiedTable 形如db.table的限定表名
     * @param event          变更事件
     * @return true表示行通过
     */
    private boolean matchRowCondition(Map<String, String> rowConditions, String qualifiedTable, CdcEvent event) {
        if (CollUtil.isEmpty(rowConditions)) {
            return true;
        }
        String expressionStr = rowConditions.get(qualifiedTable);
        if (StrUtil.isBlank(expressionStr)) {
            return true;
        }
        Expression expression = expressionCache.computeIfAbsent(expressionStr, parser::parseExpression);
        SimpleEvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();
        context.setVariable("beforeData", event.getBeforeData());
        context.setVariable("afterData", event.getAfterData());
        context.setVariable("changeType", event.getChangeType());
        context.setVariable("database", event.getDatabase());
        context.setVariable("table", event.getTable());
        Boolean result;
        try {
            result = expression.getValue(context, Boolean.class);
        } catch (Exception e) {
            throw AssertUtil.isTrue(true).cause(e).i18n(CdcI18nConstant.ROW_CONDITION_FAILED,
                qualifiedTable, expressionStr).errorLevel(ErrorLevelEnum.SYSTEM_ERROR).build();
        }
        return Boolean.TRUE.equals(result);
    }

    /**
     * 判断目标串是否命中正则集合中的任一模式
     *
     * @param patterns 正则集合
     * @param target   目标串
     * @return true表示至少命中一个
     */
    private boolean anyMatch(List<String> patterns, String target) {
        for (String regex : patterns) {
            Pattern pattern = patternCache.computeIfAbsent(regex, Pattern::compile);
            if (pattern.matcher(target).matches()) {
                return true;
            }
        }
        return false;
    }
}
