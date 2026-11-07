package starter

import (
	"context"
	"fmt"
	"log"
	"reflect"
	"strings"

	hessian "github.com/apache/dubbo-go-hessian2"
)

// Binding 将配置声明的接口契约与业务实现绑定
type Binding struct {
	// Interface 接口全限定名，必须与配置中某个 services[].interface 一致
	Interface string
	// Impl 业务实现，必须为导出结构体的指针；须实现 Reference() string 返回同一接口全限定名，
	// 方法用真实签名（首参可带 context.Context，返回值形如 (T, error) 或 (T) 或 error）：
	// generic_service filter 会把 $invoke(method, types, args) 按反射出的参数类型还原成具体值再调用
	Impl any
	// POJOs 可选兜底：仅在方法签名之外（如 Map 值元素）出现的传输对象，反射扫描覆盖不到时在此登记
	POJOs []any
}

// contextType 用于签名校验时识别 context.Context 参数
var contextType = reflect.TypeOf((*context.Context)(nil)).Elem()

// errorType 用于签名校验时识别 error 返回值
var errorType = reflect.TypeOf((*error)(nil)).Elem()

// javaClassNameType hessian2 的 POJO 契约接口类型
var javaClassNameType = reflect.TypeOf((*interface{ JavaClassName() string })(nil)).Elem()

/**
 * 校验绑定表与配置契约的一致性
 *
 * 双向匹配：配置声明的每个接口必须有绑定，每个绑定必须命中配置声明；
 * 绑定的实现必须通过 validateImpl 的结构/方法签名校验。
 *
 * @param cfg 已通过 Validate 的配置
 * @param bindings 业务绑定表
 * @return 不一致时返回带接口定位的错误，成功返回 nil
 */
func validateBindings(cfg *Config, bindings []Binding) error {
	if len(bindings) == 0 {
		return fmt.Errorf("绑定校验失败: bindings 为空")
	}
	declared := make(map[string]*ServiceConfig, len(cfg.Services))
	for i := range cfg.Services {
		declared[cfg.Services[i].Interface] = &cfg.Services[i]
	}
	bound := make(map[string]struct{}, len(bindings))
	for i := range bindings {
		b := &bindings[i]
		if strings.TrimSpace(b.Interface) == "" {
			return fmt.Errorf("绑定校验失败: bindings[%d].Interface 必填", i)
		}
		svc, ok := declared[b.Interface]
		if !ok {
			return fmt.Errorf("绑定校验失败: bindings[%d] 接口 %s 未在配置中声明", i, b.Interface)
		}
		if _, dup := bound[b.Interface]; dup {
			return fmt.Errorf("绑定校验失败: bindings[%d] 接口 %s 重复绑定", i, b.Interface)
		}
		bound[b.Interface] = struct{}{}
		if b.Impl == nil {
			return fmt.Errorf("绑定校验失败: bindings[%d] 接口 %s 的 Impl 不能为 nil", i, b.Interface)
		}
		if err := validateImpl(svc, b.Impl); err != nil {
			return err
		}
	}
	for _, svc := range cfg.Services {
		if _, ok := bound[svc.Interface]; !ok {
			return fmt.Errorf("绑定校验失败: 配置中的接口 %s 缺少对应的 Binding", svc.Interface)
		}
	}
	return nil
}

/**
 * 校验单个实现与契约声明的匹配
 *
 * 结构约束：Impl 必须是导出结构体的指针，并实现 Reference() string
 * （返回值等于声明的接口名，dubbo-go 服务分发的定位依据）；
 * 方法约束：每个声明方法必须存在同名导出方法，参数个数与
 * parameterTypes 一致（可额外带首参 context.Context），返回值形态与
 * returnType（void/单值）一致，末位返回值为 error。
 *
 * @param svc 服务契约声明
 * @param impl 业务实现（结构体指针）
 * @return 不匹配时返回带接口/方法定位与期望签名的错误，成功返回 nil
 */
func validateImpl(svc *ServiceConfig, impl any) error {
	t := reflect.TypeOf(impl)
	if t.Kind() != reflect.Ptr {
		return fmt.Errorf("绑定校验失败: 接口 %s 的实现必须以指针传入(如 &FooImpl{})，当前类型 %s",
			svc.Interface, t.String())
	}
	elem := t.Elem()
	if elem.Kind() != reflect.Struct {
		return fmt.Errorf("绑定校验失败: 接口 %s 的实现必须为结构体，当前类型 %s", svc.Interface, t.String())
	}
	if !isExportedName(elem.Name()) {
		return fmt.Errorf("绑定校验失败: 接口 %s 的实现类型 %s 必须导出(首字母大写)",
			svc.Interface, elem.Name())
	}
	ref, ok := t.MethodByName("Reference")
	if !ok || ref.Type.NumIn() != 1 || ref.Type.NumOut() != 1 || ref.Type.Out(0).Kind() != reflect.String {
		return fmt.Errorf("绑定校验失败: 接口 %s 的实现 %s 必须提供 Reference() string 返回接口全限定名",
			svc.Interface, elem.Name())
	}
	if got := reflect.ValueOf(impl).MethodByName("Reference").Call(nil)[0].String(); got != svc.Interface {
		return fmt.Errorf("绑定校验失败: 接口 %s 的实现 %s Reference() 返回 %q，与声明的接口名不一致",
			svc.Interface, elem.Name(), got)
	}
	for i := range svc.Methods {
		if err := validateMethodSignature(svc, &svc.Methods[i], t); err != nil {
			return err
		}
	}
	return nil
}

/**
 * 校验单个声明方法在实现上的签名匹配
 *
 * @param svc 服务契约声明
 * @param mc 方法契约声明
 * @param implType 实现类型（指针）
 * @return 不匹配时返回带期望签名的错误，成功返回 nil
 */
func validateMethodSignature(svc *ServiceConfig, mc *MethodConfig, implType reflect.Type) error {
	loc := fmt.Sprintf("接口 %s 方法 %s", svc.Interface, mc.Name)
	m, ok := implType.MethodByName(mc.Name)
	if !ok {
		return fmt.Errorf("绑定校验失败: %s 在实现上未找到导出方法(方法名需与声明一致且导出)", loc)
	}
	// reflect.Type 的方法类型包含接收者参数，入参计数需去掉第 0 位的接收者
	mt := m.Type
	numIn := mt.NumIn() - 1
	switch numIn {
	case len(mc.ParameterTypes):
	case len(mc.ParameterTypes) + 1:
		if mt.In(1) != contextType {
			return fmt.Errorf("绑定校验失败: %s 首参数类型 %s 不是 context.Context，多余参数无法匹配",
				loc, mt.In(1).String())
		}
	default:
		return fmt.Errorf("绑定校验失败: %s 声明 %d 个参数(实际实现入参 %d 个，可含 1 个 context.Context)与实现不一致",
			loc, len(mc.ParameterTypes), numIn)
	}
	return validateMethodReturn(loc, mc, mt)
}

/**
 * 校验方法返回值形态与 returnType 声明一致
 *
 * @param loc 定位描述（接口/方法）
 * @param mc 方法契约声明
 * @param mt 实现方法类型
 * @return 形态不一致时返回错误，成功返回 nil
 */
func validateMethodReturn(loc string, mc *MethodConfig, mt reflect.Type) error {
	numOut := mt.NumOut()
	if numOut > 2 {
		return fmt.Errorf("绑定校验失败: %s 返回值最多 1 个结果 + 1 个 error，当前有 %d 个返回值", loc, numOut)
	}
	hasError := numOut > 0 && mt.Out(numOut-1) == errorType
	valueOut := numOut
	if hasError {
		valueOut--
	}
	if valueOut > 1 {
		return fmt.Errorf("绑定校验失败: %s 返回值最多 1 个结果，当前有 %d 个非 error 返回值", loc, valueOut)
	}
	isVoid := mc.ReturnType == "void"
	if isVoid && valueOut != 0 {
		return fmt.Errorf("绑定校验失败: %s returnType 声明为 void，但实现返回了 %d 个结果值", loc, valueOut)
	}
	if !isVoid && valueOut != 1 {
		return fmt.Errorf("绑定校验失败: %s returnType 声明为 %s，但实现没有返回结果值", loc, mc.ReturnType)
	}
	return nil
}

/**
 * 反射扫描全部实现与兜底列表，注册去重后的 POJO
 *
 * 扫描范围：每个绑定的实现上全部导出方法（含未声明方法，保证零遗漏）的
 * 入参与返回类型，解包指针/切片/数组/map 与结构体字段后递归；对实现
 * JavaClassName() string 的结构体按 reflect.Type 全局去重并调用
 * hessian.RegisterPOJO，最后对配置中的类型串做 POJO 覆盖提示。
 * 必须在 RegisterService 与 Serve 之前调用。
 *
 * @param cfg 已通过 Validate 的配置
 * @param bindings 业务绑定表
 * @return 注册失败时返回错误，成功返回 nil
 */
func registerPOJOs(cfg *Config, bindings []Binding) error {
	registered := make(map[reflect.Type]struct{})
	javaNames := make(map[string]struct{})
	for i := range bindings {
		b := &bindings[i]
		t := reflect.TypeOf(b.Impl)
		for j := 0; j < t.NumMethod(); j++ {
			mt := t.Method(j).Type
			for k := 0; k < mt.NumIn(); k++ {
				if err := walkAndRegister(mt.In(k), registered, javaNames); err != nil {
					return err
				}
			}
			for k := 0; k < mt.NumOut(); k++ {
				if err := walkAndRegister(mt.Out(k), registered, javaNames); err != nil {
					return err
				}
			}
		}
		for _, p := range b.POJOs {
			if p == nil {
				continue
			}
			if err := walkAndRegister(reflect.TypeOf(p), registered, javaNames); err != nil {
				return err
			}
		}
	}
	warnUnknownTypes(cfg, javaNames)
	return nil
}

/**
 * 递归解包类型并注册其中的 POJO
 *
 * @param t 待解包类型
 * @param registered 已注册类型集合（去重）
 * @param javaNames 已注册 POJO 的 JavaClassName 集合
 * @return 注册失败时返回错误，成功返回 nil
 */
func walkAndRegister(t reflect.Type, registered map[reflect.Type]struct{}, javaNames map[string]struct{}) error {
	visited := make(map[reflect.Type]struct{})
	var walk func(reflect.Type) error
	walk = func(cur reflect.Type) error {
		if cur == nil {
			return nil
		}
		if _, seen := visited[cur]; seen {
			return nil
		}
		visited[cur] = struct{}{}
		switch cur.Kind() {
		case reflect.Ptr:
			return walk(cur.Elem())
		case reflect.Slice, reflect.Array:
			return walk(cur.Elem())
		case reflect.Map:
			if err := walk(cur.Key()); err != nil {
				return err
			}
			return walk(cur.Elem())
		case reflect.Struct:
			if _, done := registered[cur]; done {
				return nil
			}
			if implementsJavaClassName(cur) {
				pojo, ok := reflect.New(cur).Interface().(hessian.POJO)
				if ok {
					if err := registerPOJO(pojo, cur, registered, javaNames); err != nil {
						return err
					}
				}
			}
			for i := 0; i < cur.NumField(); i++ {
				f := cur.Field(i)
				if f.PkgPath != "" {
					continue
				}
				if err := walk(f.Type); err != nil {
					return err
				}
			}
			return nil
		default:
			return nil
		}
	}
	return walk(t)
}

/**
 * 注册单个 POJO 并登记其 JavaClassName
 *
 * @param pojo hessian POJO 实例（结构体指针）
 * @param t 结构体类型
 * @param registered 已注册类型集合（去重）
 * @param javaNames 已注册 POJO 的 JavaClassName 集合
 * @return 注册失败时返回错误，成功返回 nil
 */
func registerPOJO(pojo hessian.POJO, t reflect.Type, registered map[reflect.Type]struct{}, javaNames map[string]struct{}) error {
	hessian.RegisterPOJO(pojo)
	registered[t] = struct{}{}
	javaNames[pojo.JavaClassName()] = struct{}{}
	log.Printf("[starter] POJO 已注册: %s (java: %s)", t.String(), pojo.JavaClassName())
	return nil
}

/**
 * 对配置中的类型串做 POJO 覆盖提示
 *
 * 类型串既不在已知 Java 白名单前缀内、也不匹配任何已注册 POJO 的
 * JavaClassName 时输出警告（不阻断启动）：Java 侧将以 Map 形态传值，
 * 由服务端按真实方法签名 realize。
 *
 * @param cfg 配置
 * @param javaNames 已注册 POJO 的 JavaClassName 集合
 */
func warnUnknownTypes(cfg *Config, javaNames map[string]struct{}) {
	known := []string{
		"java.lang.", "java.util.", "java.math.", "java.time.", "java.sql.",
		"void", "boolean", "byte", "char", "short", "int", "long", "float", "double",
	}
	for i := range cfg.Services {
		svc := &cfg.Services[i]
		var types []string
		for j := range svc.Methods {
			m := &svc.Methods[j]
			types = append(types, m.ReturnType)
			types = append(types, m.ParameterTypes...)
		}
		for _, typeName := range types {
			if typeName == "" {
				continue
			}
			if _, ok := javaNames[typeName]; ok {
				continue
			}
			if startsWithAny(typeName, known) {
				continue
			}
			log.Printf("[starter] 警告: 接口 %s 声明的类型 %s 未匹配到已注册 POJO 的 JavaClassName，Java 侧将以 Map 形态传值，请确认类型串",
				svc.Interface, typeName)
		}
	}
}

/**
 * 判断结构体类型（值或指针形式）是否实现 hessian2 的 POJO 契约
 *
 * @param t 结构体类型
 * @return 实现 JavaClassName() string 时返回 true
 */
func implementsJavaClassName(t reflect.Type) bool {
	return t.Implements(javaClassNameType) || reflect.PtrTo(t).Implements(javaClassNameType)
}

/**
 * 判断名称是否为 Go 导出标识符
 *
 * @param name 类型或标识符名称
 * @return 首字母为大写字母或下划线时返回 true
 */
func isExportedName(name string) bool {
	if name == "" {
		return false
	}
	r := []rune(name)
	return r[0] == '_' || (r[0] >= 'A' && r[0] <= 'Z')
}

/**
 * 判断类型串是否命中任一已知前缀/关键字
 *
 * @param typeName 待判断类型串
 * @param prefixes 前缀与关键字列表
 * @return 命中返回 true
 */
func startsWithAny(typeName string, prefixes []string) bool {
	for _, p := range prefixes {
		if strings.HasPrefix(typeName, p) {
			return true
		}
	}
	return false
}
