package com.tbs.sql.util;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link SqlTextUtils} 重写前后的性能对照基准，无第三方依赖，直接运行 main 即可。
 *
 * <p>对照方式：{@link Legacy} 逐字保留重写前的实现作为 baseline，
 * optimized 侧直接调用当前 {@link SqlTextUtils}。同时测量
 * <b>ns/op</b>（耗时中位数）与 <b>B/op</b>（每轮线程分配字节数 / 调用次数，
 * 通过反射调用 JDK 的 ThreadMXBean.getThreadAllocatedBytes，取不到时输出 n/a）。</p>
 *
 * <p>每个用例、每个操作都先做等价性校验，避免「跑得快但结果变了」；
 * 唯一的预期差异是末尾无换行的纯行注释（重写顺带修掉的旧 bug），
 * 以及「先截断后折叠」与「先折叠后截断」的输出长度差异，均单独标注。</p>
 *
 * <h3>SQL 语料</h3>
 * 从简单单行 SELECT 到多层 CTE / 窗口函数 / UNION / 5000 占位符的超长 IN 语句，
 * 覆盖无注释、前导空白、头部混合注释、UPDATE / DELETE、纯注释等形态。
 *
 * <h3>运行（JDK 21，工作区根目录）</h3>
 * <pre>
 * javac -encoding UTF-8 -d sql/sql-api/target/bench-classes ^
 *       sql/sql-api/src/main/java/com/tbs/sql/util/SqlTextUtils.java ^
 *       sql/sql-api/src/test/java/com/tbs/sql/util/SqlTextUtilsBenchmark.java
 * java -cp sql/sql-api/target/bench-classes com.tbs.sql.util.SqlTextUtilsBenchmark
 * </pre>
 * （Linux/macOS 把续行符 ^ 换成 \，路径分隔符换成 /）
 *
 * <p>说明：这是单线程微基准，数字受 JIT 内联、标量替换与 GC 影响，
 * 重点看分配量级与相对倍数，不要当作服务端绝对吞吐。</p>
 *
 * @author abstergo
 */
public final class SqlTextUtilsBenchmark {

    private static final int WARMUP_ROUNDS = 3;
    private static final int MEASURE_ROUNDS = 5;
    private static final int LOG_LIMIT = 2000;
    private static final String TRUNCATED_SUFFIX = "...(truncated)";

    /** 防止 JIT 把整段计算消除掉，且不在内层循环里引入 volatile 写 */
    private static volatile long blackhole;

    private SqlTextUtilsBenchmark() {
    }

    public static void main(String[] args) {
        System.out.println("=== SqlTextUtils: legacy vs optimized ===");
        System.out.printf("JDK=%s | warmup=%d rounds, measure=%d rounds | allocation=%s%n",
                System.getProperty("java.version"), WARMUP_ROUNDS, MEASURE_ROUNDS,
                ALLOC_METHOD == null ? "n/a (ThreadMXBean unavailable)" : "ThreadMXBean");
        System.out.println();

        int failures = verifyEdgeCases();
        List<SqlCase> cases = buildCases();
        List<Scenario> scenarios = buildScenarios();

        double[] logSpeedup = new double[scenarios.size()];
        int[] speedupCount = new int[scenarios.size()];
        double totalLegacyBytes = 0;
        double totalNewBytes = 0;
        int checks = 0;

        for (SqlCase sqlCase : cases) {
            String sql = sqlCase.sql;
            int loops = loopsFor(sql);
            System.out.printf("[%s] len=%d, loops=%d/round%n", sqlCase.name, sql.length(), loops);
            System.out.printf("  %-24s %11s %11s %9s %12s %12s  %s%n",
                    "operation", "legacy ns", "new ns", "speedup", "legacy B/op", "new B/op", "check");

            for (int si = 0; si < scenarios.size(); si++) {
                Scenario scenario = scenarios.get(si);
                Object legacyValue = scenario.legacy.run(sql);
                Object newValue = scenario.optimized.run(sql);
                String check;
                if (scenario.skipEquivalence) {
                    check = "n/a (expected diff)";
                } else if (sqlCase.allowDiffOps.contains(scenario.name)) {
                    check = "DIFF* (bugfix)";
                    if (Objects.equals(legacyValue, newValue)) {
                        check = "OK";
                    }
                } else {
                    checks++;
                    if (Objects.equals(legacyValue, newValue)) {
                        check = "OK";
                    } else {
                        check = "FAIL";
                        failures++;
                    }
                }

                for (int r = 0; r < WARMUP_ROUNDS; r++) {
                    runBatch(scenario.legacy, sql, loops);
                    runBatch(scenario.optimized, sql, loops);
                }
                long[] legacyTimes = new long[MEASURE_ROUNDS];
                long[] newTimes = new long[MEASURE_ROUNDS];
                long[] legacyBytes = new long[MEASURE_ROUNDS];
                long[] newBytes = new long[MEASURE_ROUNDS];
                for (int r = 0; r < MEASURE_ROUNDS; r++) {
                    long[] a = runBatch(scenario.legacy, sql, loops);
                    long[] b = runBatch(scenario.optimized, sql, loops);
                    legacyTimes[r] = a[0];
                    legacyBytes[r] = a[1];
                    newTimes[r] = b[0];
                    newBytes[r] = b[1];
                }
                double nsLegacy = median(legacyTimes) / (double) loops;
                double nsNew = median(newTimes) / (double) loops;
                double bytesLegacy = median(legacyBytes) / (double) loops;
                double bytesNew = median(newBytes) / (double) loops;
                double speedup = nsLegacy / Math.max(nsNew, 0.01);
                logSpeedup[si] += Math.log(speedup);
                speedupCount[si]++;
                totalLegacyBytes += Math.max(0, bytesLegacy);
                totalNewBytes += Math.max(0, bytesNew);

                System.out.printf("  %-24s %11.1f %11.1f %8.2fx %12s %12s  %s%n",
                        scenario.name, nsLegacy, nsNew, speedup,
                        formatBytes(bytesLegacy), formatBytes(bytesNew), check);
            }
            System.out.println();
        }

        System.out.println("=== geometric mean speedup by operation (all cases) ===");
        for (int i = 0; i < scenarios.size(); i++) {
            System.out.printf("  %-24s %8.2fx%n", scenarios.get(i).name,
                    Math.exp(logSpeedup[i] / Math.max(1, speedupCount[i])));
        }
        System.out.printf("=== total allocated bytes per full pass: legacy=%.0f B, new=%.0f B (%.1f%% less)%n",
                totalLegacyBytes, totalNewBytes,
                totalLegacyBytes <= 0 ? 0 : 100.0 * (1 - totalNewBytes / totalLegacyBytes));
        System.out.printf("=== equivalence: %d checks, %d failures ===%n", checks, failures);
        if (failures > 0) {
            throw new AssertionError("Equivalence check failed, see FAIL rows above");
        }
    }

    // ------------------------------------------------------------------
    // 测量基础设施
    // ------------------------------------------------------------------

    @FunctionalInterface
    private interface Op {
        Object run(String sql);
    }

    private static final class Scenario {
        final String name;
        final Op legacy;
        final Op optimized;
        final boolean skipEquivalence;

        Scenario(String name, Op legacy, Op optimized, boolean skipEquivalence) {
            this.name = name;
            this.legacy = legacy;
            this.optimized = optimized;
            this.skipEquivalence = skipEquivalence;
        }

        Scenario(String name, Op legacy, Op optimized) {
            this(name, legacy, optimized, false);
        }
    }

    private static final class SqlCase {
        final String name;
        final String sql;
        final Set<String> allowDiffOps;

        SqlCase(String name, String sql, String... allowDiffOps) {
            this.name = name;
            this.sql = sql;
            this.allowDiffOps = new HashSet<>(Arrays.asList(allowDiffOps));
        }
    }

    /**
     * 单轮批量调用，返回 {耗时纳秒, 分配字节数}。
     * 消费方式只看返回值长度/布尔值，避免对 legacy 新生成的 String 做 hashCode 全串扫描。
     */
    private static long[] runBatch(Op op, String sql, int loops) {
        long sink = 0;
        long bytesBefore = allocatedBytes();
        long startTime = System.nanoTime();
        for (int i = 0; i < loops; i++) {
            sink += consume(op.run(sql));
        }
        long elapsed = System.nanoTime() - startTime;
        long bytesAfter = allocatedBytes();
        blackhole = sink;
        return new long[]{elapsed, bytesAfter - bytesBefore};
    }

    private static long consume(Object value) {
        if (value instanceof String) {
            return ((String) value).length();
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1L : 0L;
        }
        return ((Number) value).longValue();
    }

    private static int loopsFor(String sql) {
        return Math.max(100, Math.min(10_000, 1_500_000 / Math.max(1, sql.length())));
    }

    private static long median(long[] values) {
        long[] copy = values.clone();
        Arrays.sort(copy);
        return copy[copy.length / 2];
    }

    private static String formatBytes(double value) {
        return value < 0 ? "n/a" : String.format(Locale.ROOT, "%.0f", value);
    }

    /**
     * 通过反射取线程分配量，避免编译期依赖 com.sun.management（不影响 maven --release 编译）。
     */
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
    // 边界等价性校验（不参与计时）
    // ------------------------------------------------------------------

    private static int verifyEdgeCases() {
        String[] edges = {
                null,
                "",
                "   \n\t  ",
                "SELECT 1",
                "  \r\n  SELECT 1 \r\n",
                "-- c\nSELECT 1",
                "  -- c\r\n\t/* hint */\n  SELECT 1",
                "/* unclosed",
                "  /* unclosed",
                "- 1",
                "a  b\tc",
                "a\u0001b",
                "WHEREnot WHERE",
                "select * from t where_1 = ?",
                "select * from t WHERE id = ?",
                "select * from t WhErE id = ?",
                "select where_col, mywhere, 1where from t",
                "where",
                "wherex",
                "where_",
                "x where",
                "/* where */ select 1",
                "select 1 -- where x\n from t",
                "-- head where x\nselect 1",
        };
        int failures = 0;
        for (int i = 0; i < edges.length; i++) {
            String sql = edges[i];
            boolean same = Objects.equals(Legacy.stripLeadingComments(sql), SqlTextUtils.stripLeadingComments(sql))
                    && Objects.equals(Legacy.normalize(sql), SqlTextUtils.normalize(sql))
                    && Objects.equals(Legacy.firstKeyword(sql), SqlTextUtils.firstKeyword(sql))
                    && Legacy.containsWhere(sql) == SqlTextUtils.containsWhere(sql)
                    && Objects.equals(Legacy.singleLine(sql), SqlTextUtils.singleLine(sql))
                    && Legacy.excludes(sql, "select") == SqlTextUtils.startsWithIgnoreCase(sql, "select");
            if (!same) {
                failures++;
                System.out.printf("  edge[%02d] MISMATCH: %s%n", i, String.valueOf(sql));
            }
        }
        System.out.printf("Edge cases: %d checked, %d mismatches%n%n", edges.length, failures);
        return failures;
    }

    // ------------------------------------------------------------------
    // 场景定义
    // ------------------------------------------------------------------

    private static List<Scenario> buildScenarios() {
        List<Scenario> list = new ArrayList<>();
        list.add(new Scenario("stripLeadingComments",
                Legacy::stripLeadingComments, SqlTextUtils::stripLeadingComments));
        list.add(new Scenario("normalize",
                Legacy::normalize, SqlTextUtils::normalize));
        list.add(new Scenario("excludePrefix",
                sql -> Legacy.excludes(sql, "select"),
                sql -> SqlTextUtils.startsWithIgnoreCase(sql, "select")));
        list.add(new Scenario("firstKeyword",
                Legacy::firstKeyword, SqlTextUtils::firstKeyword));
        list.add(new Scenario("containsWhere",
                Legacy::containsWhere, SqlTextUtils::containsWhere));
        list.add(new Scenario("singleLine",
                Legacy::singleLine, SqlTextUtils::singleLine));
        list.add(new Scenario("fullPipeline",
                SqlTextUtilsBenchmark::legacyPipeline,
                SqlTextUtilsBenchmark::newPipeline));
        // 重写后调用方改为「先截断再折叠」，输出内容可与旧顺序不同，只比较成本
        list.add(new Scenario("logTruncate",
                SqlTextUtilsBenchmark::legacyLogText,
                SqlTextUtilsBenchmark::newLogText, true));
        return list;
    }

    /**
     * 模拟一条 SQL 的完整装饰链：2 次排除规则（check + complete）、
     * 2 次命令类型推断、UPDATE/DELETE 时 1 次 WHERE 判断、1 次日志单行化。
     * 返回值是 0..63 的位掩码，落在 Integer 缓存范围内，不额外引入分配。
     */
    private static Integer legacyPipeline(String sql) {
        int bits = 0;
        if (Legacy.excludes(sql, "select")) {
            bits |= 1;
        }
        if (Legacy.excludes(sql, "with")) {
            bits |= 2;
        }
        String keyword = Legacy.firstKeyword(sql);
        String keywordAgain = Legacy.firstKeyword(sql);
        if (keyword.equals(keywordAgain)) {
            bits |= 4;
        }
        if ("update".equals(keyword) || "delete".equals(keyword)) {
            if (Legacy.containsWhere(sql)) {
                bits |= 8;
            }
        }
        String line = Legacy.singleLine(sql);
        return bits | ((line.length() & 1) << 5);
    }

    private static Integer newPipeline(String sql) {
        int bits = 0;
        if (SqlTextUtils.startsWithIgnoreCase(sql, "select")) {
            bits |= 1;
        }
        if (SqlTextUtils.startsWithIgnoreCase(sql, "with")) {
            bits |= 2;
        }
        String keyword = SqlTextUtils.firstKeyword(sql);
        String keywordAgain = SqlTextUtils.firstKeyword(sql);
        if (keyword.equals(keywordAgain)) {
            bits |= 4;
        }
        if ("update".equals(keyword) || "delete".equals(keyword)) {
            if (SqlTextUtils.containsWhere(sql)) {
                bits |= 8;
            }
        }
        String line = SqlTextUtils.singleLine(sql);
        return bits | ((line.length() & 1) << 5);
    }

    /** 旧调用顺序：全量单行化后再截断 */
    private static String legacyLogText(String sql) {
        String line = Legacy.singleLine(sql);
        return line.length() <= LOG_LIMIT ? line : line.substring(0, LOG_LIMIT) + TRUNCATED_SUFFIX;
    }

    /** 新调用顺序：先截断原串，再单行化（超长 SQL 不扫描注定被丢弃的部分） */
    private static String newLogText(String sql) {
        String raw = sql.length() <= LOG_LIMIT ? sql : sql.substring(0, LOG_LIMIT) + TRUNCATED_SUFFIX;
        return SqlTextUtils.singleLine(raw);
    }

    // ------------------------------------------------------------------
    // SQL 语料：从简单到复杂
    // ------------------------------------------------------------------

    private static List<SqlCase> buildCases() {
        List<SqlCase> list = new ArrayList<>();
        list.add(new SqlCase("01 simple one-line SELECT",
                "SELECT id, name FROM t_user WHERE id = ?"));
        list.add(new SqlCase("02 formatted SELECT (leading whitespace)",
                "\n    SELECT u.id, u.name\n      FROM t_user u\n     WHERE u.status = ?\n  "));
        list.add(new SqlCase("03 single leading line comment",
                "-- traceId: 7f3a9c\nSELECT * FROM t_order WHERE id = ?"));
        list.add(new SqlCase("04 many mixed leading comments",
                manyComments()));
        list.add(new SqlCase("05 medium JOIN + subquery",
                mediumJoin()));
        list.add(new SqlCase("06 complex CTE + window + UNION",
                complexAnalytics()));
        list.add(new SqlCase("07 huge IN list (5000 binds)",
                hugeInList()));
        list.add(new SqlCase("08 UPDATE with WHERE",
                "UPDATE t_account SET balance = ?, mtime = ? WHERE id = ? AND tenant_id = ?"));
        list.add(new SqlCase("09 DELETE without WHERE (worst scan)",
                "DELETE FROM t_archive"));
        list.add(new SqlCase("10 pure comment, no trailing newline",
                "-- only a comment",
                "stripLeadingComments", "normalize"));
        return list;
    }

    private static String manyComments() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append("-- trace ").append(i).append(": tenant=?\n");
            sb.append("/* hint ").append(i).append(": INDEX(t idx_").append(i).append(") */\n");
            sb.append("   \n");
        }
        sb.append("\n  SELECT o.id FROM t_order o WHERE o.id = ?");
        return sb.toString();
    }

    private static String mediumJoin() {
        return "/* order detail */\n"
                + "\n  SELECT o.id, o.order_no, c.name AS customer, SUM(i.amount) AS total\n"
                + "    FROM t_order o\n"
                + "    JOIN t_customer c ON c.id = o.customer_id\n"
                + "    LEFT JOIN t_order_item i ON i.order_id = o.id\n"
                + "    LEFT JOIN (SELECT order_id, COUNT(*) AS cnt FROM t_payment\n"
                + "               WHERE state = ? GROUP BY order_id) p ON p.order_id = o.id\n"
                + "   WHERE o.created_at BETWEEN ? AND ?\n"
                + "     AND o.status IN (?, ?, ?)\n"
                + "     AND c.tenant_id = ?\n"
                + "   GROUP BY o.id, o.order_no, c.name\n"
                + "  HAVING SUM(i.amount) > ?\n"
                + "   ORDER BY total DESC\n";
    }

    private static String complexAnalytics() {
        StringBuilder sb = new StringBuilder(4096);
        sb.append("/* report=quarterly-revenue; tenant=? */\n")
                .append("-- generated by query builder v3\n")
                .append("  WITH recent_orders AS (\n")
                .append("      SELECT o.id, o.customer_id, o.amount, o.created_at, o.status,\n")
                .append("             ROW_NUMBER() OVER (PARTITION BY o.customer_id\n")
                .append("                               ORDER BY o.created_at DESC) AS rn\n")
                .append("      FROM t_order o\n")
                .append("      JOIN t_order_item i ON i.order_id = o.id\n")
                .append("      WHERE o.created_at >= ? AND o.created_at < ?\n")
                .append("        AND o.status IN (?, ?, ?, ?)\n")
                .append("        AND i.sku_id IN (SELECT sku_id FROM t_sku WHERE category_id = ?)\n")
                .append("  ),\n")
                .append("  customer_totals AS (\n")
                .append("      SELECT r.customer_id, SUM(r.amount) AS total_amount,\n")
                .append("             COUNT(DISTINCT r.id) AS order_count,\n")
                .append("             MAX(r.created_at) AS last_order_at\n")
                .append("      FROM recent_orders r\n")
                .append("      WHERE r.rn <= ?\n")
                .append("      GROUP BY r.customer_id\n")
                .append("      HAVING SUM(r.amount) > ?\n")
                .append("  ),\n")
                .append("  region_rank AS (\n")
                .append("      SELECT c.region_id, c.id AS customer_id,\n")
                .append("             DENSE_RANK() OVER (PARTITION BY c.region_id\n")
                .append("                               ORDER BY t.total_amount DESC) AS rk\n")
                .append("      FROM customer_totals t\n")
                .append("      JOIN t_customer c ON c.id = t.customer_id\n")
                .append("      WHERE c.deleted = 0\n")
                .append("  )\n")
                .append("SELECT rg.name AS region_name, c.name AS customer_name,\n")
                .append("       t.total_amount, t.order_count, rr.rk,\n")
                .append("       CASE WHEN t.total_amount > ? THEN 'VIP' ELSE 'NORMAL' END AS level\n")
                .append("  FROM customer_totals t\n")
                .append("  JOIN t_customer c ON c.id = t.customer_id\n")
                .append("  JOIN t_region rg ON rg.id = c.region_id\n")
                .append("  LEFT JOIN region_rank rr ON rr.customer_id = c.id\n")
                .append(" WHERE t.last_order_at >= ? AND c.tenant_id = ?\n");
        for (int i = 0; i < 10; i++) {
            sb.append("UNION ALL SELECT rg.name, c.name, t.total_amount, t.order_count, rr.rk, 'BUCKET_")
                    .append(i).append("'\n")
                    .append("  FROM customer_totals t JOIN t_customer c ON c.id = t.customer_id\n")
                    .append("  JOIN t_region rg ON rg.id = c.region_id\n")
                    .append("  LEFT JOIN region_rank rr ON rr.customer_id = c.id\n")
                    .append(" WHERE t.total_amount BETWEEN ? AND ? AND c.tenant_id = ?\n");
        }
        sb.append(" ORDER BY 3 DESC, 2 ASC\n");
        return sb.toString();
    }

    private static String hugeInList() {
        StringBuilder sb = new StringBuilder(16_384);
        sb.append("SELECT id, order_no, amount, created_at FROM t_order WHERE id IN (");
        for (int i = 0; i < 5000; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append('?');
        }
        sb.append(") AND tenant_id = ? AND status = ? ORDER BY created_at DESC");
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // 重写前的实现（baseline，逐字保留）
    // ------------------------------------------------------------------

    private static final class Legacy {

        private static final Pattern LEADING_COMMENT_PATTERN =
                Pattern.compile("^(?:--[^\\n]*\\n|/\\*.*?\\*/|\\s)+", Pattern.DOTALL);
        private static final Pattern WHERE_PATTERN = Pattern.compile("\\bwhere\\b");
        private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

        private Legacy() {
        }

        static String stripLeadingComments(String sql) {
            if (sql == null || sql.isEmpty()) {
                return "";
            }
            Matcher matcher = LEADING_COMMENT_PATTERN.matcher(sql);
            return matcher.replaceFirst("");
        }

        static String normalize(String sql) {
            return stripLeadingComments(sql).toLowerCase(Locale.ROOT);
        }

        static String firstKeyword(String sql) {
            String normalized = stripLeadingComments(sql);
            int end = 0;
            int length = normalized.length();
            while (end < length && Character.isWhitespace(normalized.charAt(end))) {
                end++;
            }
            int start = end;
            while (end < length && Character.isLetterOrDigit(normalized.charAt(end))) {
                end++;
            }
            return normalized.substring(start, end).toLowerCase(Locale.ROOT);
        }

        static boolean containsWhere(String sql) {
            return WHERE_PATTERN.matcher(normalize(sql)).find();
        }

        static String singleLine(String sql) {
            if (sql == null || sql.isEmpty()) {
                return "";
            }
            return WHITESPACE_PATTERN.matcher(sql).replaceAll(" ").trim();
        }

        /** 旧 SqlExcludeRule：整串小写化后 startsWith */
        static boolean excludes(String sql, String prefix) {
            return normalize(sql).startsWith(prefix);
        }
    }
}
