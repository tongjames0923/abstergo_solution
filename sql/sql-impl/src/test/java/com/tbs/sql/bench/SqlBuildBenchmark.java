package com.tbs.sql.bench;

import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.assembler.impl.DefaultSqlAssembler;
import com.tbs.sql.dialect.extractor.CommonGroupExtractor;
import com.tbs.sql.dialect.extractor.CommonOrderExtractor;
import com.tbs.sql.dialect.extractor.CommonSelectExtractor;
import com.tbs.sql.dialect.extractor.CommonWhereExtractor;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.model.group.GroupModel;
import com.tbs.sql.model.limit.LimitModel;
import com.tbs.sql.model.order.OrderInfo;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.model.select.SelectItemModel;
import com.tbs.sql.model.where.WhereCompose;
import com.tbs.sql.model.where.WhereInfo;
import com.tbs.sql.model.where.WhereNode;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * SQL 生成链路的分段计时基准，无第三方测试框架依赖，直接运行 main。
 *
 * <p>对照方式：{@link Legacy} 逐字保留改造前的字符串逻辑作为 baseline，
 * optimized 侧直接调用当前真实实现类。同时测量 ns/op 与 B/op
 * （反射调用 ThreadMXBean.getThreadAllocatedBytes，取不到时输出 n/a）。</p>
 *
 * <p>每个用例、每个阶段都做等价性校验：不仅比较生成的 SQL 文本，
 * 还比较 WHERE 提取器写入的 {@code paramMap}（参数键与值必须完全一致），
 * 因为参数绑定是这条链路的对外契约。</p>
 *
 * <h3>阶段划分</h3>
 * SELECT / WHERE / ORDER / GROUP / LIMIT / ASSEMBLE / FULL（完整链路），
 * 并给出各阶段在 legacy FULL 耗时中的占比，用于定位瓶颈。
 *
 * <h3>SQL 语料</h3>
 * 从 5 字段 2 条件的简单查询，到 80 字段 30 条件的宽表、
 * 3 层嵌套 WHERE、500 元素 IN 集合，覆盖简单到复杂。
 *
 * <h3>运行（工作区根目录，JDK 21）</h3>
 * <pre>
 * javac -encoding UTF-8 -proc:none -cp "%CP%" -d sql/sql-impl/target/bench-classes ^
 *       &lt;本文件&gt; &lt;各改动实现类的源码&gt;
 * java -cp "sql/sql-impl/target/bench-classes;%CP%" com.tbs.sql.bench.SqlBuildBenchmark
 * </pre>
 * 其中 %CP% = common/target/classes、sql/sql-api/target/classes、
 * hutool-all、annotations、slf4j-api、lombok 的 jar。
 * （Linux/macOS 把 ^ 换成 \，路径分隔符换成 /，classpath 冒号分隔）
 *
 * <p>说明：单线程微基准，数字受 JIT 内联与标量替换影响，
 * 重点看分配量级、相对倍数与阶段占比，不要当作服务端绝对吞吐。
 * LIMIT 阶段对比的是 String.format 与直接拼接两种写法（方言实现在其它模块，
 * 基准不跨模块引用），其余阶段均调用真实类。</p>
 *
 * @author abstergo
 */
public final class SqlBuildBenchmark {

    private static final int WARMUP_ROUNDS = 3;
    private static final int MEASURE_ROUNDS = 5;
    private static volatile long blackhole;

    private static final CommonSelectExtractor NEW_SELECT = new CommonSelectExtractor();
    /** 模拟 EntityMetadataExtractor 长期持有、已标记可缓存的元数据对象 */
    private static final SelectInfoModel CACHED_INFO = cacheableSelectInfo(20);
    private static final CommonWhereExtractor NEW_WHERE = new CommonWhereExtractor("MySQL");
    private static final CommonOrderExtractor NEW_ORDER = new CommonOrderExtractor();
    private static final CommonGroupExtractor NEW_GROUP = new CommonGroupExtractor();
    private static final DefaultSqlAssembler NEW_ASSEMBLER = new DefaultSqlAssembler();

    private SqlBuildBenchmark() {
    }

    public static void main(String[] args) {
        System.out.println("=== SQL build chain: legacy vs optimized ===");
        System.out.printf("JDK=%s | warmup=%d, measure=%d | allocation=%s%n",
                System.getProperty("java.version"), WARMUP_ROUNDS, MEASURE_ROUNDS,
                ALLOC_METHOD == null ? "n/a" : "ThreadMXBean");
        System.out.println();

        int failures = verifyEdgeCases();
        List<SqlCase> cases = buildCases();
        List<Stage> stages = buildStages();

        double[] logSpeedup = new double[stages.size()];
        int[] speedupCount = new int[stages.size()];
        double totalLegacyBytes = 0;
        double totalNewBytes = 0;
        int checks = 0;

        for (SqlCase sqlCase : cases) {
            System.out.printf("[%s] loops=%d/round, whereNodes=%d, fields=%d%n",
                    sqlCase.name, sqlCase.loops, countNodes(sqlCase.where),
                    sqlCase.selectInfo.getFieldList() == null ? 0 : sqlCase.selectInfo.getFieldList().size());
            System.out.printf("  %-10s %11s %11s %9s %12s %12s %8s  %s%n",
                    "stage", "legacy ns", "new ns", "speedup", "legacy B/op", "new B/op", "cost%", "check");

            // 先测完所有阶段再统一打印，cost% = 该阶段 legacy 耗时占各阶段之和的比例
            double[][] metrics = new double[stages.size()][4];
            String[] results = new String[stages.size()];
            for (int si = 0; si < stages.size(); si++) {
                Stage stage = stages.get(si);
                boolean ok = equivalent(stage, sqlCase);
                checks++;
                if (!ok) {
                    failures++;
                }

                Map<String, Object> scratch = new HashMap<>(64);
                for (int r = 0; r < WARMUP_ROUNDS; r++) {
                    runBatch(stage.legacy, sqlCase, scratch, sqlCase.loops);
                    runBatch(stage.optimized, sqlCase, scratch, sqlCase.loops);
                }
                long[] legacyTimes = new long[MEASURE_ROUNDS];
                long[] newTimes = new long[MEASURE_ROUNDS];
                long[] legacyBytes = new long[MEASURE_ROUNDS];
                long[] newBytes = new long[MEASURE_ROUNDS];
                for (int r = 0; r < MEASURE_ROUNDS; r++) {
                    long[] a = runBatch(stage.legacy, sqlCase, scratch, sqlCase.loops);
                    long[] b = runBatch(stage.optimized, sqlCase, scratch, sqlCase.loops);
                    legacyTimes[r] = a[0];
                    legacyBytes[r] = a[1];
                    newTimes[r] = b[0];
                    newBytes[r] = b[1];
                }
                double nsLegacy = median(legacyTimes) / (double) sqlCase.loops;
                double nsNew = median(newTimes) / (double) sqlCase.loops;
                double bytesLegacy = median(legacyBytes) / (double) sqlCase.loops;
                double bytesNew = median(newBytes) / (double) sqlCase.loops;
                double speedup = nsLegacy / Math.max(nsNew, 0.01);
                metrics[si][0] = nsLegacy;
                metrics[si][1] = nsNew;
                metrics[si][2] = bytesLegacy;
                metrics[si][3] = bytesNew;
                results[si] = ok ? "OK" : "FAIL";
                logSpeedup[si] += Math.log(speedup);
                speedupCount[si]++;
                totalLegacyBytes += Math.max(0, bytesLegacy);
                totalNewBytes += Math.max(0, bytesNew);
            }
            // cost% 只统计前 6 个基础阶段，FULL / SELECT_HIT / FULL_HIT 是组合或缓存对照，不参与分母
            final int baseStages = 6;
            double stageSum = 0;
            for (int si = 0; si < baseStages; si++) {
                stageSum += metrics[si][0];
            }
            for (int si = 0; si < stages.size(); si++) {
                String shareText = si < baseStages
                        ? String.format(Locale.ROOT, "%.0f%%", stageSum > 0 ? metrics[si][0] / stageSum * 100 : 0)
                        : "-";
                System.out.printf("  %-10s %11.1f %11.1f %8.2fx %12s %12s %8s  %s%n",
                        stages.get(si).name, metrics[si][0], metrics[si][1],
                        metrics[si][0] / Math.max(metrics[si][1], 0.01),
                        formatBytes(metrics[si][2]), formatBytes(metrics[si][3]),
                        shareText, results[si]);
            }
            System.out.println();
        }

        System.out.println("=== geometric mean speedup by stage (all cases) ===");
        for (int i = 0; i < stages.size(); i++) {
            System.out.printf("  %-10s %8.2fx%n", stages.get(i).name,
                    Math.exp(logSpeedup[i] / Math.max(1, speedupCount[i])));
        }
        System.out.printf("=== total allocated bytes per full pass: legacy=%.0f B, new=%.0f B (%.1f%% less)%n",
                totalLegacyBytes, totalNewBytes,
                totalLegacyBytes <= 0 ? 0 : 100.0 * (1 - totalNewBytes / totalLegacyBytes));
        System.out.printf("=== equivalence: %d checks, %d failures ===%n", checks, failures);
        if (failures > 0) {
            throw new AssertionError("Equivalence check failed, see FAIL rows / edge cases above");
        }
    }

    // ------------------------------------------------------------------
    // 测量基础设施
    // ------------------------------------------------------------------

    @FunctionalInterface
    private interface Op {
        String run(SqlCase sqlCase, Map<String, Object> params);
    }

    private static final class Stage {
        final String name;
        final Op legacy;
        final Op optimized;

        Stage(String name, Op legacy, Op optimized) {
            this.name = name;
            this.legacy = legacy;
            this.optimized = optimized;
        }
    }

    private static final class SqlCase {
        final String name;
        final int loops;
        final SelectInfoModel selectInfo;
        final WhereCompose where;
        final List<OrderInfo> orders;
        final List<GroupModel> groups;
        final LimitModel limit;
        final String fragSelect;
        final String fragWhere;
        final String fragOrder;
        final String fragGroup;
        final String fragLimit;

        SqlCase(String name, int loops, SelectInfoModel selectInfo, WhereCompose where,
                List<OrderInfo> orders, List<GroupModel> groups, LimitModel limit) {
            this.name = name;
            this.loops = loops;
            this.selectInfo = selectInfo;
            this.where = where;
            this.orders = orders;
            this.groups = groups;
            this.limit = limit;
            Map<String, Object> params = new HashMap<>();
            this.fragSelect = Legacy.select(selectInfo);
            this.fragWhere = Legacy.where(where, params);
            this.fragOrder = Legacy.order(orders);
            this.fragGroup = Legacy.group(groups);
            this.fragLimit = Legacy.limit(limit);
        }
    }

    /** 等价性：SQL 文本逐字节一致 + WHERE 写入的参数表内容一致 */
    private static boolean equivalent(Stage stage, SqlCase sqlCase) {
        Map<String, Object> legacyParams = new HashMap<>();
        Map<String, Object> newParams = new HashMap<>();
        String legacySql = stage.legacy.run(sqlCase, legacyParams);
        String newSql = stage.optimized.run(sqlCase, newParams);
        return Objects.equals(legacySql, newSql) && Objects.equals(legacyParams, newParams);
    }

    private static long[] runBatch(Op op, SqlCase sqlCase, Map<String, Object> params, int loops) {
        long sink = 0;
        long bytesBefore = allocatedBytes();
        long startTime = System.nanoTime();
        for (int i = 0; i < loops; i++) {
            params.clear();
            sink += op.run(sqlCase, params).length();
        }
        long elapsed = System.nanoTime() - startTime;
        long bytesAfter = allocatedBytes();
        blackhole = sink;
        return new long[]{elapsed, bytesAfter - bytesBefore};
    }

    private static long median(long[] values) {
        long[] copy = values.clone();
        Arrays.sort(copy);
        return copy[copy.length / 2];
    }

    private static String formatBytes(double value) {
        return value < 0 ? "n/a" : String.format(Locale.ROOT, "%.0f", value);
    }

    private static final Method ALLOC_METHOD = initAllocMethod();

    private static Method initAllocMethod() {
        try {
            Class<?> type = Class.forName("com.sun.management.ThreadMXBean");
            Object bean = ManagementFactory.getThreadMXBean();
            if (!type.isInstance(bean)) {
                return null;
            }
            type.getMethod("setThreadAllocatedMemoryEnabled", boolean.class).invoke(bean, Boolean.TRUE);
            return type.getMethod("getThreadAllocatedBytes", long.class);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private static long allocatedBytes() {
        if (ALLOC_METHOD == null) {
            return -1;
        }
        try {
            return (Long) ALLOC_METHOD.invoke(
                    ManagementFactory.getThreadMXBean(), Thread.currentThread().getId());
        } catch (Throwable ignored) {
            return -1;
        }
    }

    // ------------------------------------------------------------------
    // 阶段定义
    // ------------------------------------------------------------------

    private static List<Stage> buildStages() {
        List<Stage> list = new ArrayList<>();
        list.add(new Stage("SELECT",
                (c, p) -> Legacy.select(c.selectInfo),
                (c, p) -> NEW_SELECT.extract(c.selectInfo)));
        list.add(new Stage("WHERE",
                (c, p) -> Legacy.where(c.where, p),
                (c, p) -> NEW_WHERE.extract(c.where, p)));
        list.add(new Stage("ORDER",
                (c, p) -> Legacy.order(c.orders),
                (c, p) -> NEW_ORDER.extract(c.orders)));
        list.add(new Stage("GROUP",
                (c, p) -> Legacy.group(c.groups),
                (c, p) -> NEW_GROUP.extract(c.groups)));
        list.add(new Stage("LIMIT",
                (c, p) -> Legacy.limit(c.limit),
                (c, p) -> newLimit(c.limit)));
        list.add(new Stage("ASSEMBLE",
                (c, p) -> Legacy.assemble(c.fragSelect, c.fragWhere, c.fragOrder, c.fragGroup, c.fragLimit),
                (c, p) -> NEW_ASSEMBLER.assembleSelectSql(
                        c.fragSelect, c.fragWhere, c.fragOrder, c.fragGroup, c.fragLimit)));
        list.add(new Stage("FULL",
                (c, p) -> Legacy.assemble(
                        Legacy.select(c.selectInfo), Legacy.where(c.where, p),
                        Legacy.order(c.orders), Legacy.group(c.groups), Legacy.limit(c.limit)),
                (c, p) -> NEW_ASSEMBLER.assembleSelectSql(
                        NEW_SELECT.extract(c.selectInfo), NEW_WHERE.extract(c.where, p),
                        NEW_ORDER.extract(c.orders), NEW_GROUP.extract(c.groups), newLimit(c.limit))));
        // 片段缓存：SELECT_HIT 复用同一个已标记模型；FULL_HIT 看其在完整链路中的贡献
        list.add(new Stage("SELECT_HIT",
                (c, p) -> Legacy.select(CACHED_INFO),
                (c, p) -> NEW_SELECT.extract(CACHED_INFO)));
        list.add(new Stage("FULL_HIT",
                (c, p) -> Legacy.assemble(
                        Legacy.select(CACHED_INFO), Legacy.where(c.where, p),
                        Legacy.order(c.orders), Legacy.group(c.groups), Legacy.limit(c.limit)),
                (c, p) -> NEW_ASSEMBLER.assembleSelectSql(
                        NEW_SELECT.extract(CACHED_INFO), NEW_WHERE.extract(c.where, p),
                        NEW_ORDER.extract(c.orders), NEW_GROUP.extract(c.groups), newLimit(c.limit))));
        return list;
    }

    /** 改造后的 LIMIT 拼接（原实现为 String.format，仅拼两个数字） */
    private static String newLimit(LimitModel model) {
        if (model == null) return "";
        Long offset = model.getOffset();
        Long count = model.getCount();
        if (count == null) return "";
        if (offset == null || offset == 0L) return " LIMIT " + count;
        return " LIMIT " + count + " OFFSET " + offset;
    }

    // ------------------------------------------------------------------
    // 边界等价性校验（不参与计时）
    // ------------------------------------------------------------------

    private static int verifyEdgeCases() {
        List<WhereCompose> composes = new ArrayList<>();
        composes.add(null);
        composes.add(new WhereCompose());
        composes.add(compose(infoNode(ConnectorEnum.AND, "a", ContrastEnum.EQUAL, 1)));
        composes.add(compose(rawNode(ConnectorEnum.AND, "a = 1")));
        composes.add(compose(infoNode(ConnectorEnum.AND, "a", ContrastEnum.IN, new ArrayList<>())));
        composes.add(compose(infoNode(ConnectorEnum.AND, "a", ContrastEnum.IS_NULL, null)));
        composes.add(compose(infoNode(ConnectorEnum.AND, "a", ContrastEnum.IS_NOT_NULL, null)));
        composes.add(compose(infoNode(ConnectorEnum.AND, "a", ContrastEnum.LIKE, "abc")));
        composes.add(compose(infoNode(ConnectorEnum.AND, "a", ContrastEnum.LLIKE, "abc")));
        composes.add(compose(infoNode(ConnectorEnum.AND, "a", ContrastEnum.RLIKE, "abc")));
        composes.add(compose(infoNode(ConnectorEnum.AND, "a", ContrastEnum.NOT_IN, Arrays.asList(1, 2, 3))));
        composes.add(compose(infoNode(ConnectorEnum.OR, "a", ContrastEnum.EQUAL, 1),
                infoNode(ConnectorEnum.AND, "b", ContrastEnum.GREATER, 2)));
        composes.add(compose(new WhereNode(ConnectorEnum.AND, "unknown"),
                infoNode(ConnectorEnum.AND, "c", ContrastEnum.EQUAL, 3)));
        composes.add(compose(composeNode(ConnectorEnum.AND, new WhereCompose()),
                infoNode(ConnectorEnum.AND, "d", ContrastEnum.EQUAL, 4)));
        WhereInfo withStr = new WhereInfo();
        withStr.setFieldName("e");
        withStr.setContrast(ContrastEnum.EQUAL);
        withStr.setFieldValue(123);
        withStr.setFieldValueStr("override");
        composes.add(compose(WhereNode.createInfoNode(ConnectorEnum.AND, withStr)));

        int failures = 0;
        for (int i = 0; i < composes.size(); i++) {
            Map<String, Object> legacyParams = new HashMap<>();
            Map<String, Object> newParams = new HashMap<>();
            String legacySql = Legacy.where(composes.get(i), legacyParams);
            String newSql = NEW_WHERE.extract(composes.get(i), newParams);
            if (!Objects.equals(legacySql, newSql) || !Objects.equals(legacyParams, newParams)) {
                failures++;
                System.out.printf("  edge[%02d] MISMATCH%n    legacy=%s %s%n    new   =%s %s%n",
                        i, legacySql, legacyParams, newSql, newParams);
            }
        }

        List<SelectInfoModel> selects = new ArrayList<>();
        selects.add(SelectInfoModel.rawSql("SELECT 1"));
        selects.add(new SelectInfoModel(null, "t_user", null, null, null));
        selects.add(new SelectInfoModel(null, "t_user", "u", fields(3), null));
        selects.add(new SelectInfoModel(null, "t_user", "u",
                Arrays.asList(new SelectItemModel("col", null)), null));
        for (int i = 0; i < selects.size(); i++) {
            if (!Objects.equals(Legacy.select(selects.get(i)), NEW_SELECT.extract(selects.get(i)))) {
                failures++;
                System.out.printf("  select edge[%02d] MISMATCH%n", i);
            }
        }
        // SELECT 片段缓存专项：命中一致性、owner 隔离、引用级失效、未标记模型不缓存
        SelectInfoModel cacheable = cacheableSelectInfo(4);
        CommonSelectExtractor ownerA = new CommonSelectExtractor();
        CommonSelectExtractor ownerB = new CommonSelectExtractor();
        String expectedSelect = Legacy.select(cacheable);
        String first = ownerA.extract(cacheable);
        boolean storedUnderA = Objects.equals(first, cacheable.cachedSelectSql(ownerA));
        boolean ownerIsolated = cacheable.cachedSelectSql(ownerB) == null;
        String second = ownerB.extract(cacheable);
        boolean secondOk = Objects.equals(expectedSelect, second)
                && Objects.equals(second, cacheable.cachedSelectSql(ownerB));
        cacheable.setTableName("t_changed");
        boolean invalidated = cacheable.cachedSelectSql(ownerB) == null;
        boolean rerenderOk = Objects.equals(Legacy.select(cacheable), ownerB.extract(cacheable));
        SelectInfoModel plain = new SelectInfoModel(null, "t_plain", null, fields(3), null);
        ownerA.extract(plain);
        boolean plainNotCached = plain.cachedSelectSql(ownerA) == null;
        SelectInfoModel other = cacheableSelectInfo(2);
        boolean otherOk = Objects.equals(Legacy.select(other), ownerA.extract(other))
                && other.cachedSelectSql(ownerA) != null;

        // 缓存字段不得污染 @Data 的 equals/hashCode（@EqualsAndHashCode.Exclude 生效）
        SelectInfoModel marked = cacheableSelectInfo(2);
        SelectInfoModel unmarked = new SelectInfoModel(null, "t_cached", "c", fields(2), null);
        ownerA.extract(marked);
        boolean modelSemantics = marked.equals(unmarked) && unmarked.equals(marked)
                && marked.hashCode() == unmarked.hashCode();
        // 模拟启动期预热：用与运行期同一个提取器实例渲染一次，后续调用应直接命中
        SelectInfoModel warmed = cacheableSelectInfo(6);
        CommonSelectExtractor sharedExtractor = new CommonSelectExtractor();
        String warmedSql = sharedExtractor.extract(warmed);
        boolean warmupHit = Objects.equals(warmedSql, warmed.cachedSelectSql(sharedExtractor))
                && Objects.equals(warmedSql, sharedExtractor.extract(warmed));

        boolean[] cacheChecks = {Objects.equals(expectedSelect, first), storedUnderA, ownerIsolated,
                secondOk, invalidated, rerenderOk, plainNotCached, otherOk, modelSemantics, warmupHit};
        String[] cacheNames = {"first render", "stored under owner", "owner isolation",
                "second owner render", "reference invalidation", "re-render after change",
                "unmarked model not cached", "per-model cache", "equals/hashCode unchanged",
                "startup warmup is hit at runtime"};
        for (int i = 0; i < cacheChecks.length; i++) {
            if (!cacheChecks[i]) {
                failures++;
                System.out.println("  cache check FAILED: " + cacheNames[i]);
            }
        }

        System.out.printf("Edge cases: %d WHERE + %d SELECT + %d CACHE checked, %d mismatches%n%n",
                composes.size(), selects.size(), cacheChecks.length, failures);
        return failures;
    }

    // ------------------------------------------------------------------
    // SQL 语料
    // ------------------------------------------------------------------

    private static List<SqlCase> buildCases() {
        List<SqlCase> list = new ArrayList<>();
        list.add(new SqlCase("01 simple (5 cols / 2 conds)", 10_000,
                select(5, "t_user", "u"),
                compose(infoNode(ConnectorEnum.AND, "id", ContrastEnum.EQUAL, 42),
                        infoNode(ConnectorEnum.AND, "name", ContrastEnum.LIKE, "ab")),
                orders(1), groups(0), new LimitModel(0L, 20L)));
        list.add(new SqlCase("02 medium (20 cols / 8 conds)", 5_000,
                select(20, "t_order", "o"),
                compose(flatConditions(8)),
                orders(3), groups(2), new LimitModel(100L, 50L)));
        list.add(new SqlCase("03 nested WHERE (3 levels)", 3_000,
                select(15, "t_account", "a"),
                nestedWhere(),
                orders(2), groups(1), new LimitModel(0L, 100L)));
        list.add(new SqlCase("04 big IN (500 binds)", 500,
                select(10, "t_order", "o"),
                compose(infoNode(ConnectorEnum.AND, "id", ContrastEnum.IN, inValues(500)),
                        infoNode(ConnectorEnum.AND, "tenant_id", ContrastEnum.EQUAL, 7)),
                orders(1), groups(0), new LimitModel(0L, 50L)));
        list.add(new SqlCase("05 wide (80 cols / 30 conds)", 1_000,
                select(80, "t_detail", "d"),
                compose(flatConditions(30)),
                orders(10), groups(5), new LimitModel(500L, 100L)));
        list.add(new SqlCase("06 rawSql + empty parts", 10_000,
                SelectInfoModel.rawSql("SELECT id, name FROM t_user"),
                new WhereCompose(),
                orders(0), groups(0), null));
        return list;
    }

    /** 构造一个与实体元数据缓存等价的可缓存模型 */
    private static SelectInfoModel cacheableSelectInfo(int columns) {
        SelectInfoModel info = new SelectInfoModel(null, "t_cached", "c", fields(columns), null);
        info.markSelectCacheable();
        return info;
    }

    private static SelectInfoModel select(int columnCount, String table, String alias) {
        return new SelectInfoModel(null, table, alias, fields(columnCount), null);
    }

    private static List<SelectItemModel> fields(int n) {
        List<SelectItemModel> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(new SelectItemModel("col_" + i, "prop" + i));
        }
        return list;
    }

    private static List<OrderInfo> orders(int n) {
        List<OrderInfo> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            OrderInfo info = new OrderInfo();
            info.setFieldName("col_" + i);
            info.setOrder(i % 2 == 0 ? OrderEnum.DESC : OrderEnum.ASC);
            list.add(info);
        }
        return list;
    }

    private static List<GroupModel> groups(int n) {
        List<GroupModel> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            GroupModel model = new GroupModel();
            model.setFieldName("col_" + i);
            list.add(model);
        }
        return list;
    }

    private static List<Object> inValues(int n) {
        List<Object> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(i);
        }
        return list;
    }

    private static List<WhereNode> flatConditions(int n) {
        List<WhereNode> nodes = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            nodes.add(infoNode(i % 5 == 0 ? ConnectorEnum.OR : ConnectorEnum.AND,
                    "col_" + i, i % 3 == 0 ? ContrastEnum.LIKE : ContrastEnum.EQUAL,
                    i % 3 == 0 ? "v" + i : i));
        }
        return nodes;
    }

    private static WhereCompose nestedWhere() {
        WhereCompose inner = compose(
                infoNode(ConnectorEnum.AND, "status", ContrastEnum.IN, Arrays.asList(1, 2, 3)),
                infoNode(ConnectorEnum.OR, "deleted", ContrastEnum.IS_NULL, null));
        WhereCompose middle = compose(
                composeNode(ConnectorEnum.AND, inner),
                infoNode(ConnectorEnum.OR, "name", ContrastEnum.LIKE, "abc"));
        return compose(
                infoNode(ConnectorEnum.AND, "tenant_id", ContrastEnum.EQUAL, 9),
                composeNode(ConnectorEnum.AND, middle),
                composeNode(ConnectorEnum.OR, compose(rawNode(ConnectorEnum.AND, "flag = 1"))),
                composeNode(ConnectorEnum.AND, new WhereCompose()));
    }

    private static WhereCompose compose(WhereNode... nodes) {
        WhereCompose result = new WhereCompose();
        result.getChildren().addAll(Arrays.asList(nodes));
        return result;
    }

    private static WhereCompose compose(List<WhereNode> nodes) {
        WhereCompose result = new WhereCompose();
        result.getChildren().addAll(nodes);
        return result;
    }

    private static WhereNode infoNode(ConnectorEnum connector, String field, ContrastEnum contrast, Object value) {
        WhereInfo info = new WhereInfo();
        info.setFieldName(field);
        info.setContrast(contrast);
        info.setFieldValue(value);
        return WhereNode.createInfoNode(connector, info);
    }

    private static WhereNode rawNode(ConnectorEnum connector, String rawSql) {
        WhereInfo info = new WhereInfo();
        info.setRawSql(rawSql);
        return WhereNode.createInfoNode(connector, info);
    }

    private static WhereNode composeNode(ConnectorEnum connector, WhereCompose child) {
        return WhereNode.createComposeNode(connector, child);
    }

    private static int countNodes(WhereCompose compose) {
        if (compose == null || compose.getChildren() == null) return 0;
        int total = 0;
        for (WhereNode node : compose.getChildren()) {
            total++;
            if (node.isCompose()) total += countNodes((WhereCompose) node.getValue());
        }
        return total;
    }

    // ------------------------------------------------------------------
    // 改造前的实现（baseline，字符串逻辑逐字保留；断言路径简化为显式判空）
    // ------------------------------------------------------------------

    private static final class Legacy {

        private Legacy() {
        }

        static String select(SelectInfoModel infoModel) {
            if (infoModel == null) throw new IllegalArgumentException("SelectInfoModel can not be null");
            if (StrUtil.isNotEmpty(infoModel.getRawSql())) return infoModel.getRawSql();
            if (StrUtil.isEmpty(infoModel.getTableName())) throw new IllegalArgumentException("tableName can not be null");

            String fromClause = buildFromClause(infoModel.getTableName(), infoModel.getTableAlias());
            List<SelectItemModel> fieldList = infoModel.getFieldList();
            if (CollUtil.isEmpty(fieldList)) return "SELECT * FROM " + fromClause;
            StringBuilder sb = new StringBuilder("SELECT ");
            for (SelectItemModel item : fieldList) {
                if (item == null) throw new IllegalArgumentException("SelectItemModel can not be null");
                if (StrUtil.isEmpty(item.getFieldName())) throw new IllegalArgumentException("fieldName can not be null");
                sb.append(item.getFieldName());
                if (StrUtil.isNotEmpty(item.getFieldAlias())) sb.append(" AS ").append(item.getFieldAlias());
                sb.append(",");
            }
            String sql = sb.substring(0, sb.length() - 1);
            return String.format("%s FROM %s", sql, fromClause);
        }

        private static String buildFromClause(String tableName, String tableAlias) {
            if (StrUtil.isNotEmpty(tableAlias)) return tableName + " AS " + tableAlias;
            return tableName;
        }

        static String order(List<OrderInfo> orderInfos) {
            if (CollUtil.isEmpty(orderInfos)) return "";
            StringBuilder sb = new StringBuilder();
            for (OrderInfo info : orderInfos) {
                sb.append(info.getFieldName()).append(" ")
                    .append(OrderEnum.getOrder(info.getOrder() == null ? OrderEnum.ASC : info.getOrder()))
                    .append(",");
            }
            return " ORDER BY " + sb.substring(0, sb.length() - 1);
        }

        static String group(List<GroupModel> groupModels) {
            if (CollUtil.isEmpty(groupModels)) return "";
            StringBuilder sb = new StringBuilder();
            for (GroupModel model : groupModels) {
                sb.append(model.getFieldName()).append(",");
            }
            if (sb.isEmpty()) return "";
            return " GROUP BY " + sb.substring(0, sb.length() - 1);
        }

        static String limit(LimitModel model) {
            if (model == null) return "";
            Long offset = model.getOffset();
            Long count = model.getCount();
            if (count == null) return "";
            if (offset == null || offset == 0L) return " LIMIT " + count;
            return String.format(" LIMIT %d OFFSET %d", count, offset);
        }

        static String assemble(String selectSql, String whereSql, String orderSql, String groupSql, String limitSql) {
            StringJoiner joiner = new StringJoiner(" ");
            if (selectSql != null && !selectSql.isEmpty()) joiner.add(selectSql);
            if (whereSql != null && !whereSql.isEmpty()) joiner.add(whereSql);
            if (groupSql != null && !groupSql.isEmpty()) joiner.add(groupSql);
            if (orderSql != null && !orderSql.isEmpty()) joiner.add(orderSql);
            if (limitSql != null && !limitSql.isEmpty()) joiner.add(limitSql);
            return joiner.toString();
        }

        static String where(WhereCompose compose, Map<String, Object> paramMap) {
            String where = extraWhereSql(compose, new Long[]{0L}, paramMap).trim();
            return StrUtil.isEmpty(where) ? "" : " WHERE " + where;
        }

        private static String contrastStrSet(ContrastEnum contrastEnum, String placeholder) {
            if (ContrastEnum.IS_NOT_NULL == contrastEnum || ContrastEnum.IS_NULL == contrastEnum) return "";
            else if (ContrastEnum.LLIKE == contrastEnum) return placeholder + "%";
            else if (ContrastEnum.RLIKE == contrastEnum) return "%" + placeholder;
            else if (ContrastEnum.LIKE == contrastEnum) return "%" + placeholder + "%";
            else if (ContrastEnum.IN == contrastEnum || ContrastEnum.NOT_IN == contrastEnum)
                return "(" + placeholder + ")";
            return placeholder;
        }

        private static String contrast(ContrastEnum contrastEnum) {
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
                default: throw new IllegalArgumentException("unsupported contrast: " + contrastEnum);
            }
        }

        static String singleWhereInfo(WhereInfo info, Long id, Map<String, Object> paramMap) {
            if (StrUtil.isNotEmpty(info.getRawSql())) return info.getRawSql();
            String key = String.format("param_%d", id);
            Object val = info.getFieldValue();
            if (StrUtil.isNotEmpty(info.getFieldValueStr())) val = info.getFieldValueStr();
            String fieldName = info.getFieldName();

            if ((info.getContrast() == ContrastEnum.IN || info.getContrast() == ContrastEnum.NOT_IN)
                && val instanceof Collection) {
                Collection<?> collection = (Collection<?>) val;
                if (!collection.isEmpty()) {
                    List<String> placeholders = new ArrayList<>();
                    int index = 0;
                    for (Object item : collection) {
                        String itemKey = key + "_" + index++;
                        placeholders.add("#{_prs." + itemKey + "}");
                        paramMap.put(itemKey, item);
                    }
                    return String.format(" (%s %s %s) ", fieldName,
                        contrast(info.getContrast()),
                        "(" + String.join(",", placeholders) + ")");
                }
                return "(1=1)";
            }

            if (info.getContrast() == ContrastEnum.IS_NULL || info.getContrast() == ContrastEnum.IS_NOT_NULL) {
                return String.format(" (%s %s) ", fieldName, contrast(info.getContrast()));
            }

            String valStr = String.format("#{_prs.%s}", key);
            if (val != null && val instanceof String) val = contrastStrSet(info.getContrast(), val.toString());
            paramMap.put(key, val);
            return String.format(" (%s %s %s) ", fieldName, contrast(info.getContrast()), valStr);
        }

        private static String extraWhereSql(WhereCompose compose, Long[] id, Map<String, Object> paramMap) {
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
    }
}
