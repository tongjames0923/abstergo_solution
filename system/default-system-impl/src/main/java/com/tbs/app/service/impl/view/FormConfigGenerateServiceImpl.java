package com.tbs.app.service.impl.view;

import com.tbs.app.constant.SystemApiI18nConstant;
import com.tbs.app.service.view.IFormConfigGenerateService;
import com.tbs.app.web.AbstractCrudController;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.shared.pojo.model.view.CrudControllerOption;
import com.tbs.shared.pojo.model.view.FormConfig;
import com.tbs.shared.pojo.model.view.FormField;
import jakarta.annotation.Resource;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 表单配置生成服务实现
 * <p>
 * 以 CRUD 控制器的泛型 {@code Q} / {@code T} 为元数据来源：查询场景读取查询条件字段，
 * 新增/编辑场景读取实体字段，生成可被 FormCreator 直接装载的默认配置草稿。
 * </p>
 *
 * @author abstergo
 */
@Service
public class FormConfigGenerateServiceImpl implements IFormConfigGenerateService {

    /**
     * 查询表单场景
     */
    private static final String SCENE_SEARCH = "search";
    /**
     * 新增表单场景
     */
    private static final String SCENE_SAVE = "save";
    /**
     * 编辑表单场景
     */
    private static final String SCENE_EDIT = "edit";

    /**
     * 支持的生成场景
     */
    private static final Set<String> SCENES = Set.of(SCENE_SEARCH, SCENE_SAVE, SCENE_EDIT);

    /**
     * 实体场景中默认不生成的字段
     */
    private static final Set<String> SKIP_ENTITY_FIELDS =
        Set.of("id", "flag", "createTime", "updateTime", "creator", "updater");

    /**
     * 查询条件场景中框架内部字段
     */
    private static final Set<String> SKIP_QUERY_FIELDS =
        Set.of("entityClass", "resourcePath", "selectInfoModel", "sqlSelectInfoModelMap");

    /**
     * 需要转成日期范围的查询字段后缀
     */
    private static final String SUFFIX_RANGE = "Range";
    /**
     * 区间查询起始后缀
     */
    private static final String SUFFIX_GREATER_THAN = "GreaterThan";
    /**
     * 区间查询结束后缀
     */
    private static final String SUFFIX_LESS_THAN = "LessThan";

    /**
     * 排序字段后缀，排序由表格处理，不生成到查询表单
     */
    private static final String SUFFIX_ORDER = "Order";

    /**
     * 查询字段常见条件后缀
     */
    private static final List<String> CONDITION_SUFFIXES =
        List.of("Equals", "Like", "In", "Start", "End", SUFFIX_ORDER);

    /**
     * 查询表单标题 i18n key
     */
    private static final String TITLE_SEARCH = "view.form.generate.title.search";
    /**
     * 新增表单标题 i18n key
     */
    private static final String TITLE_SAVE = "view.form.generate.title.save";
    /**
     * 编辑表单标题 i18n key
     */
    private static final String TITLE_EDIT = "view.form.generate.title.edit";

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public List<CrudControllerOption> listControllers() {
        List<CrudControllerOption> options = new ArrayList<>();
        for (AbstractCrudController<?, ?, ?> controller : resolveControllers()) {
            options.add(toOption(controller));
        }
        return options;
    }

    @Override
    public FormConfig generate(String controller, String scene) {
        AssertUtil.isTrue(controller == null || controller.isBlank())
            .i18n(SystemApiI18nConstant.FORM_GENERATE_CONTROLLER_EMPTY)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        String normalizedScene = scene == null || scene.isBlank()
            ? SCENE_SEARCH : scene.trim().toLowerCase(Locale.ROOT);
        AssertUtil.isTrue(!SCENES.contains(normalizedScene))
            .i18n(SystemApiI18nConstant.FORM_GENERATE_SCENE_INVALID).params(scene)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

        AbstractCrudController<?, ?, ?> target = findController(controller);
        AssertUtil.isTrue(target == null)
            .i18n(SystemApiI18nConstant.FORM_GENERATE_CONTROLLER_NOT_FOUND).params(controller)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

        Class<?> modelClass = resolveModelClass(target, normalizedScene);
        AssertUtil.isTrue(modelClass == null)
            .i18n(SystemApiI18nConstant.FORM_GENERATE_MODEL_UNSUPPORTED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

        return buildConfig(target, modelClass, normalizedScene);
    }

    /**
     * 解析所有 CRUD 控制器并按类名排序
     *
     * @return 控制器列表
     */
    @SuppressWarnings("unchecked")
    private List<AbstractCrudController<?, ?, ?>> resolveControllers() {
        List<AbstractCrudController<?, ?, ?>> controllers = new ArrayList<>();
        for (Object controller : applicationContext.getBeansOfType(AbstractCrudController.class).values()) {
            controllers.add((AbstractCrudController<?, ?, ?>)controller);
        }
        controllers.sort(Comparator.comparing(controller -> AopUtils.getTargetClass(controller).getSimpleName()));
        return controllers;
    }

    /**
     * 按类名或类级路径查找控制器
     *
     * @param controller 类名或路径
     * @return 匹配的控制器，未找到时返回 null
     */
    private AbstractCrudController<?, ?, ?> findController(String controller) {
        String keyword = normalize(controller);
        return resolveControllers().stream().filter(item -> {
            Class<?> targetClass = AopUtils.getTargetClass(item);
            return targetClass.getSimpleName().equalsIgnoreCase(keyword)
                || targetClass.getName().equalsIgnoreCase(keyword)
                || normalize(controllerPath(targetClass)).equals(keyword);
        }).findFirst().orElse(null);
    }

    /**
     * 组装控制器选项
     *
     * @param controller 控制器实例
     * @return 选项模型
     */
    private CrudControllerOption toOption(AbstractCrudController<?, ?, ?> controller) {
        Class<?> targetClass = AopUtils.getTargetClass(controller);
        CrudControllerOption option = new CrudControllerOption();
        option.setName(targetClass.getSimpleName());
        option.setPath(controllerPath(targetClass));
        option.setBusinessName(controller.getBusinessName());
        return option;
    }

    /**
     * 读取控制器类级请求路径
     *
     * @param targetClass 控制器类
     * @return 请求路径，未声明时返回空串
     */
    private String controllerPath(Class<?> targetClass) {
        RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(targetClass, RequestMapping.class);
        if (mapping == null) {
            return "";
        }
        String[] paths = mapping.value().length > 0 ? mapping.value() : mapping.path();
        return paths.length == 0 ? "" : paths[0];
    }

    /**
     * 解析场景对应的模型类型
     *
     * @param controller 控制器实例
     * @param scene      场景
     * @return 查询条件类或实体类，无法解析时返回 null
     */
    private Class<?> resolveModelClass(AbstractCrudController<?, ?, ?> controller, String scene) {
        ResolvableType type = ResolvableType.forClass(AopUtils.getTargetClass(controller))
            .as(AbstractCrudController.class);
        return SCENE_SEARCH.equals(scene) ? type.getGeneric(2).resolve() : type.getGeneric(0).resolve();
    }

    /**
     * 构建默认表单配置
     *
     * @param controller 控制器实例
     * @param modelClass 模型类型
     * @param scene      场景
     * @return 表单配置草稿
     */
    private FormConfig buildConfig(AbstractCrudController<?, ?, ?> controller, Class<?> modelClass, String scene) {
        List<Field> sourceFields = collectFields(modelClass);
        FormConfig config = new FormConfig();
        config.setTitle(I18nUtil.get(titleKey(scene), controller.getBusinessName()));
        config.setExpandable(true);
        config.setExpanded(true);
        config.setLabelWidth("100px");
        config.setGutter(16);
        config.setSpan(8);
        config.setFields(buildFields(sourceFields, scene));
        return config;
    }

    /**
     * 收集模型及其父类中的实例字段
     *
     * @param modelClass 模型类型
     * @return 字段列表
     */
    private List<Field> collectFields(Class<?> modelClass) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = modelClass; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isSynthetic() || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                fields.add(field);
            }
        }
        return fields;
    }

    /**
     * 根据场景把字段转换成表单字段
     *
     * @param sourceFields 模型字段
     * @param scene        场景
     * @return 表单字段列表
     */
    private List<FormField> buildFields(List<Field> sourceFields, String scene) {
        Map<String, Field> byName = sourceFields.stream()
            .collect(Collectors.toMap(Field::getName, Function.identity(), (first, second) -> first, LinkedHashMap::new));
        Set<String> used = new HashSet<>();
        List<FormField> fields = new ArrayList<>();
        for (Field field : sourceFields) {
            String prop = field.getName();
            boolean skip = SCENE_SEARCH.equals(scene)
                ? SKIP_QUERY_FIELDS.contains(prop) || prop.endsWith(SUFFIX_ORDER)
                : SKIP_ENTITY_FIELDS.contains(prop);
            if (skip) {
                continue;
            }
            String pairBase = rangePairBase(prop);
            if (SCENE_SEARCH.equals(scene) && pairBase != null) {
                Field upper = byName.get(pairBase + SUFFIX_GREATER_THAN);
                Field lower = byName.get(pairBase + SUFFIX_LESS_THAN);
                if (upper != null && lower != null) {
                    if (prop.endsWith(SUFFIX_GREATER_THAN)) {
                        fields.add(newField(pairBase + SUFFIX_RANGE, fieldLabel(pairBase), "date-range",
                            clearableProps()));
                    }
                    used.add(upper.getName());
                    used.add(lower.getName());
                    continue;
                }
            }
            if (used.contains(prop)) {
                continue;
            }
            String type = resolveFieldType(field, scene);
            fields.add(newField(prop, fieldLabel(prop), type, buildProps(field, type)));
            used.add(prop);
        }
        return fields;
    }

    /**
     * 识别区间查询字段的基础名
     *
     * @param prop 字段名
     * @return 基础名，非区间字段返回 null
     */
    private String rangePairBase(String prop) {
        if (prop.endsWith(SUFFIX_GREATER_THAN)) {
            return prop.substring(0, prop.length() - SUFFIX_GREATER_THAN.length());
        }
        if (prop.endsWith(SUFFIX_LESS_THAN)) {
            return prop.substring(0, prop.length() - SUFFIX_LESS_THAN.length());
        }
        return null;
    }

    /**
     * 按场景与字段类型推导控件类型
     *
     * @param field 字段
     * @param scene 场景
     * @return 表单字段类型
     */
    private String resolveFieldType(Field field, String scene) {
        if (SCENE_SEARCH.equals(scene)) {
            if (field.getName().endsWith(SUFFIX_RANGE)) {
                return "date-range";
            }
            return field.getType().isEnum() ? "select" : "input";
        }
        if (field.getType().isEnum()) {
            return "select";
        }
        if (String.class.equals(field.getType()) && isUrlField(field.getName())) {
            return "url-image";
        }
        return "input";
    }

    /**
     * 判断实体字段是否适合图片地址控件
     *
     * @param prop 字段名
     * @return 是否适合
     */
    private boolean isUrlField(String prop) {
        String lower = prop.toLowerCase(Locale.ROOT);
        return lower.contains("url") || lower.contains("avatar") || lower.contains("image") || lower.contains("icon");
    }

    /**
     * 构建控件属性
     *
     * @param field 字段
     * @param type  控件类型
     * @return 控件属性
     */
    private Map<String, Object> buildProps(Field field, String type) {
        Map<String, Object> props = new HashMap<>();
        if ("select".equals(type) && field.getType().isEnum()) {
            List<Map<String, Object>> options = new ArrayList<>();
            for (Object constant : field.getType().getEnumConstants()) {
                String value = ((Enum<?>)constant).name();
                options.add(Map.of("label", value, "value", value));
            }
            props.put("options", options);
        }
        if (!"url-image".equals(type)) {
            props.put("clearable", true);
        }
        return props;
    }

    /**
     * 生成可清空属性
     *
     * @return 控件属性
     */
    private Map<String, Object> clearableProps() {
        Map<String, Object> props = new HashMap<>();
        props.put("clearable", true);
        return props;
    }

    /**
     * 构建表单字段
     *
     * @param prop  字段名
     * @param label 显示名
     * @param type  控件类型
     * @param props 控件属性
     * @return 表单字段
     */
    private FormField newField(String prop, String label, String type, Map<String, Object> props) {
        FormField field = new FormField();
        field.setProp(prop);
        field.setLabel(label);
        field.setType(type);
        field.setProps(props);
        return field;
    }

    /**
     * 去掉查询条件后缀作为默认显示名
     *
     * @param prop 字段名
     * @return 默认显示名
     */
    private String fieldLabel(String prop) {
        for (String suffix : CONDITION_SUFFIXES) {
            if (prop.endsWith(suffix) && prop.length() > suffix.length()) {
                return prop.substring(0, prop.length() - suffix.length());
            }
        }
        return prop;
    }

    /**
     * 场景标题 i18n key
     *
     * @param scene 场景
     * @return i18n key
     */
    private String titleKey(String scene) {
        if (SCENE_SAVE.equals(scene)) {
            return TITLE_SAVE;
        }
        if (SCENE_EDIT.equals(scene)) {
            return TITLE_EDIT;
        }
        return TITLE_SEARCH;
    }

    /**
     * 规范化类名或路径
     *
     * @param value 原始值
     * @return 去除首尾空白与斜杠的值
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
