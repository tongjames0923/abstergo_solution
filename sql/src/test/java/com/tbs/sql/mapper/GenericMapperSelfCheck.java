package com.tbs.sql.mapper;

import com.tbs.sql.annotation.core.SelectSQL;
import com.tbs.sql.dialect.extractor.CommonInsertExtractor;
import com.tbs.sql.dialect.extractor.CommonSelectExtractor;
import com.tbs.sql.dialect.extractor.CommonWhereExtractor;
import com.tbs.sql.dialect.oracle.extractor.OracleInsertExtractor;
import com.tbs.sql.dialect.sqlserver.extractor.SqlServerInsertExtractor;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.mapper.impl.GenericSqlProvider;
import com.tbs.sql.metadata.EntityColumnMetadata;
import com.tbs.sql.metadata.EntityMetadataExtractor;
import com.tbs.sql.metadata.EntityTableMetadata;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.model.where.WhereCompose;
import com.tbs.sql.model.where.WhereInfo;
import com.tbs.sql.model.where.WhereNode;
import com.tbs.sql.util.SqlUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link GenericSqlProvider} 与批量插入提取器的可运行自检。
 *
 * <p>以固定实体逐条断言生成的 SQL 文本，覆盖字段过滤、选择性插入与更新、条件拼接、
 * IN 列表占位符以及三种方言的批量插入语法。直接运行 {@code main} 即可，不需要数据库，
 * 也不需要启动 Spring 容器。</p>
 *
 * <h3>覆盖范围</h3>
 * <ul>
 *   <li>元数据提取：静态字段、{@code @Transient} 字段被排除，{@code @Column} 映射生效</li>
 *   <li>列名推导：显式 {@code @Column} 优先，未声明时按 Hibernate 规则转下划线</li>
 *   <li>与 Hibernate / tk.mybatis 对齐：父类字段参与映射、无 {@code @Table} 时表名转下划线、
 *       空串条件被跳过、中间泛型父接口可解析</li>
 *   <li>类级 {@code @SelectSQL} 自定义 SQL：被元数据采信并原样输出，且不要求 {@code @Table}</li>
 *   <li>WHERE 取值绑定：等值 / LIKE / IN 集合均只产出预处理占位符，业务字面值不入 SQL 文本</li>
 *   <li>单行插入、按主键查询、条件查询、计数、按主键更新、条件删除</li>
 *   <li>Mapper 泛型解析：仅按主键操作的语句依赖 {@link ProviderContext#getMapperType()}</li>
 *   <li>批量插入的通用、SQL Server、Oracle 三种语法与容量上限</li>
 * </ul>
 *
 * <h3>未覆盖部分</h3>
 * <ul>
 *   <li>分批行数计算与方言实例获取依赖 Spring 容器，需在应用内验证</li>
 *   <li>结果集到实体的映射依赖 MyBatis 的 {@code map-underscore-to-camel-case} 配置</li>
 * </ul>
 *
 * @author abstergo
 * @since 3.0.0
 * @see GenericSqlProvider
 */
public class GenericMapperSelfCheck {

    /**
     * 自定义 SQL 文本，同时作为 {@link RawSqlEntity} 上 {@code @SelectSQL} 的取值。
     */
    private static final String RAW_SQL = "SELECT id, user_name AS userName FROM t_demo";

    /**
     * 自检入口。
     *
     * @param args 未使用
     */
    public static void main(String[] args) {
        checkMetadata();
        checkNaming();
        checkAlignment();
        checkCustomSql();
        checkWhereBinding();
        checkSingleInsert();
        checkSelect();
        checkUpdate();
        checkDelete();
        checkBatchInsert();
        checkLimits();
        System.out.println("GenericMapperSelfCheck 全部通过");
    }

    /**
     * 校验实体表元数据的提取结果。
     */
    private static void checkMetadata() {
        EntityTableMetadata metadata = EntityMetadataExtractor.pullTableMetadata(DemoEntity.class);
        expect("表名", "t_demo", metadata.getTableName());
        expect("持久化列数", "3", String.valueOf(metadata.getColumns().size()));
        EntityColumnMetadata idColumn = metadata.getIdColumn();
        expect("主键列", "id", idColumn == null ? "null" : idColumn.getColumn());
        for (EntityColumnMetadata column : metadata.getColumns()) {
            if ("cache".equals(column.getProperty()) || "serialVersionUID".equals(column.getProperty())) {
                throw new IllegalStateException("非持久化字段未排除：" + column.getProperty());
            }
        }
    }

    /**
     * 校验列名推导：显式 {@code @Column} 优先，未声明时按 Hibernate 规则转下划线。
     *
     * <p>覆盖多单词字段漏写 {@code @Column} 的场景，防止运行期列名与建表结果偏差。</p>
     */
    private static void checkNaming() {
        EntityTableMetadata metadata = EntityMetadataExtractor.pullTableMetadata(NamingEntity.class);
        for (EntityColumnMetadata column : metadata.getColumns()) {
            if ("createTime".equals(column.getProperty())) {
                expect("未声明@Column的驼峰字段", "create_time", column.getColumn());
            }
            if ("userName".equals(column.getProperty())) {
                expect("显式@Column列名", "user_name", column.getColumn());
            }
        }
        expect("命名转换后的查询语句",
            "SELECT id AS id, user_name AS userName, create_time AS createTime FROM t_naming WHERE id = #{id}",
            new GenericSqlProvider().selectByPrimaryKey(context(NamingMapper.class), Map.of("id", 1L)));
    }

    /**
     * 校验与 Hibernate / tk.mybatis 对齐后的行为。
     *
     * <p>覆盖四类易回归点：父类字段参与映射、无 {@code @Table} 时的表名推导、空串条件跳过、
     * 中间泛型父接口的实体解析，以及单批方法传空集合时中断执行。</p>
     */
    private static void checkAlignment() {
        GenericSqlProvider provider = new GenericSqlProvider();
        DemoEntity condition = new DemoEntity();
        condition.userName = "";
        expect("空串条件被跳过",
            "SELECT id AS id, user_name AS userName, age AS age FROM t_demo",
            provider.select(null, Map.of("entity", condition)));

        EntityTableMetadata child = EntityMetadataExtractor.pullTableMetadata(ChildEntity.class);
        expect("父类字段参与映射", "2", String.valueOf(child.getColumns().size()));
        expect("父类字段列名", "remark", child.getColumns().get(1).getColumn());
        expect("无@Table时表名转下划线", "plain_entity", SqlUtils.getTableName(PlainEntity.class));
        expect("间接Mapper继承解析",
            "SELECT id AS id, user_name AS userName, age AS age FROM t_demo WHERE id = #{id}",
            provider.selectByPrimaryKey(context(IndirectDemoMapper.class), Map.of("id", 1L)));

        expectRejected("空批量插入", "records不能为空", () -> provider.insertBatch(Map.of("records", List.of())));
        expectRejected("空批量查询", "idList不能为空",
            () -> provider.selectByIdListBatch(context(DemoMapper.class), Map.of("idList", List.of())));
        expectRejected("空批量删除", "idList不能为空",
            () -> provider.deleteByIdListBatch(context(DemoMapper.class), Map.of("idList", List.of())));
    }

    /**
     * 校验类级 {@code @SelectSQL} 的自定义 SQL 通道。
     *
     * <p>本注解的 {@code @Target} 曾被误写为 {@code FIELD}：写在类上无法编译，写在字段上读点又在类，
     * 自定义 SQL 能力整体失效。此处锁定三件事：类上注解被元数据采信、rawSql 原样输出、
     * 该模式不要求 {@code @Table}，防止再次退化。</p>
     */
    private static void checkCustomSql() {
        SelectInfoModel info = EntityMetadataExtractor.pullFromClass(RawSqlEntity.class);
        expect("类级自定义SQL被采信", RAW_SQL, info.getRawSql());
        expect("自定义SQL模式下不解析表名", "null", String.valueOf(info.getTableName()));
        expect("自定义SQL模式下无字段清单", "null", String.valueOf(info.getFieldList()));
        expect("自定义SQL直通输出", RAW_SQL, new CommonSelectExtractor().extract(info));
    }

    /**
     * 校验 WHERE 条件值一律走预处理参数绑定，SQL 文本里不出现业务字面值。
     *
     * <p>框架曾有 {@code tbs.sql.use-raw-value} 开关把值转义后内联成字面量，但该分支读取的值转换器
     * 列表从未被加载（开启即 NPE），且全仓无一处配置，已整体下架。本自检锁定四件事：等值与 IN
     * 集合只产出 {@code #{_prs.param_n}} 占位符、LIKE 的通配符补在参数值而非 SQL 文本上、
     * {@code rawSql} 条件仍原样直通，防止内联能力被重新引入时无人察觉。</p>
     */
    private static void checkWhereBinding() {
        CommonWhereExtractor extractor = new CommonWhereExtractor("MySQL");

        Map<String, Object> equalParams = new HashMap<>();
        String equal = extractor.extract(compose(where("user_name", ContrastEnum.EQUAL, "secret-abc")), equalParams);
        expectTrue("等值条件产出占位符", equal.contains("#{_prs.param_0}"));
        expectTrue("等值条件不内联字面值", !equal.contains("secret-abc"));
        expect("等值条件参数值", "secret-abc", String.valueOf(equalParams.get("param_0")));

        Map<String, Object> likeParams = new HashMap<>();
        String like = extractor.extract(compose(where("user_name", ContrastEnum.LIKE, "secret-abc")), likeParams);
        expectTrue("LIKE条件不内联通配符", !like.contains("%"));
        expect("LIKE条件参数补全通配符", "%secret-abc%", String.valueOf(likeParams.get("param_0")));

        Map<String, Object> inParams = new HashMap<>();
        String in = extractor.extract(
            compose(where("user_name", ContrastEnum.IN, Arrays.asList("secret-a", "secret-b"))), inParams);
        expectTrue("IN集合每元素一个占位符",
            in.contains("#{_prs.param_0_0}") && in.contains("#{_prs.param_0_1}"));
        expectTrue("IN集合不内联字面值", !in.contains("secret-a") && !in.contains("secret-b"));
        expect("IN集合参数个数", "2", String.valueOf(inParams.size()));

        WhereInfo raw = new WhereInfo();
        raw.setRawSql("(1=0)");
        expectTrue("rawSql条件原样直通",
            extractor.extract(compose(raw), new HashMap<>()).contains("(1=0)"));
    }

    /**
     * 构造单个 WHERE 条件模型。
     *
     * @param column   列名
     * @param contrast 比较方式
     * @param value    条件值
     * @return WHERE 条件模型
     */
    private static WhereInfo where(String column, ContrastEnum contrast, Object value) {
        WhereInfo whereInfo = new WhereInfo();
        whereInfo.setFieldName(column);
        whereInfo.setContrast(contrast);
        whereInfo.setFieldValue(value);
        return whereInfo;
    }

    /**
     * 把若干条件组装成一层 AND 组合。
     *
     * @param infos 条件列表
     * @return 条件组合
     */
    private static WhereCompose compose(WhereInfo... infos) {
        WhereCompose compose = new WhereCompose();
        for (WhereInfo info : infos) {
            compose.getChildren().add(new WhereNode<>(ConnectorEnum.AND, info));
        }
        return compose;
    }

    /**
     * 断言条件成立，用于不便逐字比对的 SQL 文本片段。
     *
     * @param name      断言名
     * @param condition 待断言的条件
     */
    private static void expectTrue(String name, boolean condition) {
        if (!condition) {
            throw new IllegalStateException(name + " 断言失败");
        }
    }

    /**
     * 校验单行插入语句。
     */
    private static void checkSingleInsert() {
        GenericSqlProvider provider = new GenericSqlProvider();
        DemoEntity entity = new DemoEntity();
        entity.id = 1L;
        entity.userName = "u";
        expect("全字段插入",
            "INSERT INTO t_demo (id, user_name, age) VALUES (#{entity.id}, #{entity.userName}, #{entity.age})",
            provider.insert(null, Map.of("entity", entity)));
        expect("选择性插入",
            "INSERT INTO t_demo (id, user_name) VALUES (#{entity.id}, #{entity.userName})",
            provider.insertSelective(null, Map.of("entity", entity)));
    }

    /**
     * 校验查询与计数语句。
     */
    private static void checkSelect() {
        GenericSqlProvider provider = new GenericSqlProvider();
        ProviderContext context = context(DemoMapper.class);
        expect("按主键查询",
            "SELECT id AS id, user_name AS userName, age AS age FROM t_demo WHERE id = #{id}",
            provider.selectByPrimaryKey(context, Map.of("id", 1L)));
        expect("按主键批量查询",
            "SELECT id AS id, user_name AS userName, age AS age FROM t_demo "
                + "WHERE id IN (#{idList[0]}, #{idList[1]})",
            provider.selectByIdListBatch(context, Map.of("idList", Arrays.asList(1L, 2L))));
        DemoEntity condition = new DemoEntity();
        condition.userName = "u";
        expect("条件查询",
            "SELECT id AS id, user_name AS userName, age AS age FROM t_demo WHERE user_name = #{entity.userName}",
            provider.select(null, Map.of("entity", condition)));
        expect("条件计数",
            "SELECT COUNT(*) FROM t_demo WHERE user_name = #{entity.userName}",
            provider.selectCount(null, Map.of("entity", condition)));
    }

    /**
     * 校验按主键更新的语句，含主键为空时中断执行。
     */
    private static void checkUpdate() {
        GenericSqlProvider provider = new GenericSqlProvider();
        DemoEntity entity = new DemoEntity();
        entity.id = 1L;
        entity.userName = "u";
        expect("全字段更新",
            "UPDATE t_demo SET id = #{entity.id}, user_name = #{entity.userName}, age = #{entity.age} "
                + "WHERE id = #{entity.id}",
            provider.updateByPrimaryKey(null, Map.of("entity", entity)));
        expect("选择性更新",
            "UPDATE t_demo SET id = #{entity.id}, user_name = #{entity.userName} WHERE id = #{entity.id}",
            provider.updateByPrimaryKeySelective(null, Map.of("entity", entity)));
        try {
            provider.updateByPrimaryKey(null, Map.of("entity", new DemoEntity()));
            throw new IllegalStateException("主键为空时应中断执行");
        } catch (IllegalStateException e) {
            throw e;
        } catch (RuntimeException e) {
            if (e.getMessage() == null || !e.getMessage().contains("主键id不能为空")) {
                throw new IllegalStateException("主键为空时提示异常：" + e.getMessage(), e);
            }
        }
    }

    /**
     * 校验删除语句。
     */
    private static void checkDelete() {
        GenericSqlProvider provider = new GenericSqlProvider();
        ProviderContext context = context(DemoMapper.class);
        expect("按主键删除",
            "DELETE FROM t_demo WHERE id = #{id}",
            provider.deleteByPrimaryKey(context, Map.of("id", 1L)));
        expect("按主键批量删除",
            "DELETE FROM t_demo WHERE id IN (#{idList[0]}, #{idList[1]})",
            provider.deleteByIdListBatch(context, Map.of("idList", Arrays.asList(1L, 2L))));
        DemoEntity condition = new DemoEntity();
        condition.userName = "u";
        expect("条件删除",
            "DELETE FROM t_demo WHERE user_name = #{entity.userName}",
            provider.delete(null, Map.of("entity", condition)));
    }

    /**
     * 校验三种方言的批量插入语法。
     */
    private static void checkBatchInsert() {
        List<String> columns = List.of("id", "user_name");
        List<String> properties = List.of("id", "userName");
        expect("通用多行插入",
            "INSERT INTO t_demo (id, user_name) VALUES (#{records[0].id}, #{records[0].userName}), "
                + "(#{records[1].id}, #{records[1].userName})",
            new CommonInsertExtractor().buildBatchInsert("t_demo", columns, properties, 2));
        expect("SQL Server 复用通用语法",
            new CommonInsertExtractor().buildBatchInsert("t_demo", columns, properties, 2),
            new SqlServerInsertExtractor().buildBatchInsert("t_demo", columns, properties, 2));
        expect("Oracle INSERT ALL",
            "INSERT ALL INTO t_demo (id, user_name) VALUES (#{records[0].id}, #{records[0].userName}) "
                + "INTO t_demo (id, user_name) VALUES (#{records[1].id}, #{records[1].userName}) "
                + "SELECT 1 FROM DUAL",
            new OracleInsertExtractor().buildBatchInsert("t_demo", columns, properties, 2));
    }

    /**
     * 校验各方言的容量上限，上限值决定 {@code insertList} 的分批粒度。
     */
    private static void checkLimits() {
        expect("通用上限", "5000/3000", limits(new CommonInsertExtractor()));
        expect("SQL Server 上限", "1000/2100", limits(new SqlServerInsertExtractor()));
        expect("Oracle 上限", "500/999", limits(new OracleInsertExtractor()));
    }

    /**
     * 拼接提取器的行数与参数数上限。
     *
     * @param extractor 语句提取器
     * @return 形如 {@code 行数/参数数} 的文本
     */
    private static String limits(com.tbs.sql.dialect.IInsertExtractor extractor) {
        return extractor.maxRows() + "/" + extractor.maxParameters();
    }

    /**
     * 构造仅携带 Mapper 类型的提供器上下文，用于验证泛型实体解析。
     *
     * <p>{@link ProviderContext} 的构造器在各版本中可能不是 public，这里统一按反射构造，
     * 避免自检因 MyBatis 小版本升级而失效。</p>
     *
     * @param mapperType Mapper 接口类型
     * @return 提供器上下文
     */
    private static ProviderContext context(Class<?> mapperType) {
        try {
            Constructor<ProviderContext> constructor = ProviderContext.class
                .getDeclaredConstructor(Class.class, Method.class, String.class);
            constructor.setAccessible(true);
            return constructor.newInstance(mapperType, null, null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("构造ProviderContext失败", e);
        }
    }

    /**
     * 断言操作中断执行，且提示包含关键片段。
     *
     * @param name    断言项名称
     * @param keyword 期望出现在异常信息中的关键片段
     * @param action  待执行的操作
     */
    private static void expectRejected(String name, String keyword, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message != null && message.contains(keyword)) {
                return;
            }
            throw new IllegalStateException(name + " 提示异常：" + message, e);
        }
        throw new IllegalStateException(name + " 应中断执行");
    }

    /**
     * 断言实际值与期望值一致。
     *
     * @param name     断言项名称
     * @param expected 期望值
     * @param actual   实际值
     */
    private static void expect(String name, String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException(name + " 不一致\n期望：" + expected + "\n实际：" + actual);
        }
    }

    /**
     * 自检用 Mapper 接口，仅用于验证泛型实体解析。
     */
    interface DemoMapper extends IFrameworkMapper<DemoEntity, Long> {
    }

    /**
     * 自检用实体，覆盖静态字段、非持久化字段与列名映射三种过滤规则。
     */
    @Table(name = "t_demo")
    static class DemoEntity {
        private static final long serialVersionUID = 1L;

        @Id
        private Long id;

        @Column(name = "user_name")
        private String userName;

        private Integer age;

        @Transient
        private String cache;
    }

    /**
     * 自检用 Mapper 接口，用于验证命名转换实体的泛型解析。
     */
    interface NamingMapper extends IFrameworkMapper<NamingEntity, Long> {
    }

    /**
     * 自检用实体，覆盖“未声明 {@code @Column} 的驼峰字段”与“显式列名”两种列名推导。
     */
    @Table(name = "t_naming")
    static class NamingEntity {
        @Id
        private Long id;

        @Column(name = "user_name")
        private String userName;

        private Date createTime;
    }

    /**
     * 自检用 Mapper 中间抽象层，验证泛型父接口逐层绑定。
     *
     * @param <T> 实体类型
     */
    interface BaseDemoMapper<T> extends IFrameworkMapper<T, Long> {
    }

    /**
     * 自检用间接 Mapper，验证 {@code XxxMapper extends BaseMapper<Xxx>} 的实体解析。
     */
    interface IndirectDemoMapper extends BaseDemoMapper<DemoEntity> {
    }

    /**
     * 自检用父类，仅声明一个持久化字段。
     */
    static class ParentEntity {
        private String remark;
    }

    /**
     * 自检用子类，验证父类字段参与映射且静态字段被排除。
     */
    @Table(name = "t_child")
    static class ChildEntity extends ParentEntity {
        private static final long serialVersionUID = 1L;

        @Id
        private Long id;
    }

    /**
     * 自检用无 {@code @Table} 实体，验证表名 fallback 的驼峰转下划线。
     */
    static class PlainEntity {
        @Id
        private Long id;
    }

    /**
     * 自检用自定义 SQL 查询模型：无 {@code @Table}，SELECT 完全由类级注解提供。
     */
    @SelectSQL(RAW_SQL)
    static class RawSqlEntity {
        private Long id;

        private String userName;
    }
}
