// Package starter 提供轻量 Dubbo(Go) 提供者的快速开发装配能力：
// 以一份 JSON 契约配置声明接口、方法、参数与返回类型，绑定业务实现后调用 Run
// 即完成 POJO 注册、服务装配、Nacos 注册与 ServiceDefinition 元数据发布，
// 随后阻塞运行。协议固定为 dubbo(TCP)+hessian2、无 IDL 注册，
// 供 Java 泛化调用与 Dubbo Admin 直接消费。
package starter

import (
	"encoding/json"
	"fmt"
	"os"
	"strconv"
	"strings"
)

// 默认配置文件名：Run 未显式传路径时使用
const DefaultConfigPath = "service.json"

// Nacos 地址环境变量（与仓库 Java 服务口径一致）
const envNacosAddress = "NACOS_ADDRESS"

// Dubbo 端口环境变量（与仓库 Java 服务口径一致）
const envDubboPort = "DUBBO_PORT"

// 各默认值：与 dubbo-db-demo 原始取值保持一致
const (
	defaultNacosAddress  = "127.0.0.1:8848"
	defaultPort          = 20001
	defaultMetadataGroup = "dubbo"
)

// Config 服务契约配置根模型
type Config struct {
	// Application 应用名（Nacos 服务名 / 元 dataId 组成部分），必填
	Application string `json:"application"`
	// Port dubbo(TCP) 监听端口，默认 20001，可被 DUBBO_PORT 环境变量覆盖
	Port int `json:"port"`
	// NacosAddress Nacos 注册与元数据地址，默认 127.0.0.1:8848，可被 NACOS_ADDRESS 环境变量覆盖
	NacosAddress string `json:"nacosAddress"`
	// MetadataGroup ServiceDefinition 在 Nacos 配置中心的 group，默认 dubbo
	MetadataGroup string `json:"metadataGroup"`
	// Services 服务契约声明，至少一个
	Services []ServiceConfig `json:"services"`
}

// ServiceConfig 单个 Dubbo 接口的契约声明
type ServiceConfig struct {
	// Interface 接口全限定名（Java 侧坐标），必填
	Interface string `json:"interface"`
	// Version 服务版本，默认空
	Version string `json:"version"`
	// Group 服务分组（Java 泛化引用按 interface#group 匹配，空只匹配未声明 group 的提供者），默认空
	Group string `json:"group"`
	// Methods 方法签名声明，至少一个
	Methods []MethodConfig `json:"methods"`
}

// MethodConfig 单个方法的 Java 侧签名声明
type MethodConfig struct {
	// Name 方法名（必须与 Go 实现上的导出方法同名），必填
	Name string `json:"name"`
	// ParameterTypes 擦除后的 JVM 参数类型串：基本类型写关键字(long/int/boolean)，对象与集合写 FQCN，禁止带泛型
	ParameterTypes []string `json:"parameterTypes"`
	// ReturnType 擦除后的 JVM 返回类型串，必填
	ReturnType string `json:"returnType"`
	// Parameters 参数定义（index/name/type），供元数据发布与 Admin 测试调用表单使用；缺省时按 ParameterTypes 自动生成 argN
	Parameters []ParamConfig `json:"parameters,omitempty"`
}

// ParamConfig 单个入参的定义
type ParamConfig struct {
	// Index 签名顺序，从 0 起连续
	Index int `json:"index"`
	// Name 参数名，供命名式调用与表单渲染
	Name string `json:"name"`
	// Type 与 ParameterTypes[Index] 一致的擦除类型串
	Type string `json:"type"`
}

// LoadConfig 从 path 读取 JSON 配置，应用环境变量覆盖与默认值，并做完整校验
func LoadConfig(path string) (*Config, error) {
	if strings.TrimSpace(path) == "" {
		return nil, fmt.Errorf("配置文件路径为空")
	}
	data, err := os.ReadFile(path)
	if err != nil {
		return nil, fmt.Errorf("读取配置文件 %s 失败: %w", path, err)
	}
	cfg := &Config{}
	if err := json.Unmarshal(data, cfg); err != nil {
		return nil, fmt.Errorf("解析配置文件 %s 失败: %w", path, err)
	}
	cfg.applyEnvOverrides()
	cfg.applyDefaults()
	if err := cfg.Validate(); err != nil {
		return nil, err
	}
	return cfg, nil
}

/**
 * 以环境变量覆盖配置值
 *
 * NACOS_ADDRESS 覆盖 Nacos 地址、DUBBO_PORT 覆盖 dubbo 端口；
 * 环境变量为空或非法时保留配置文件/默认值。
 */
func (c *Config) applyEnvOverrides() {
	if v := strings.TrimSpace(os.Getenv(envNacosAddress)); v != "" {
		c.NacosAddress = v
	}
	if v := strings.TrimSpace(os.Getenv(envDubboPort)); v != "" {
		if p, err := strconv.Atoi(v); err == nil {
			c.Port = p
		}
	}
}

/**
 * 填充各字段默认值
 *
 * 仅对零值字段生效，显式配置优先。
 */
func (c *Config) applyDefaults() {
	if strings.TrimSpace(c.NacosAddress) == "" {
		c.NacosAddress = defaultNacosAddress
	}
	if c.Port <= 0 {
		c.Port = defaultPort
	}
	if strings.TrimSpace(c.MetadataGroup) == "" {
		c.MetadataGroup = defaultMetadataGroup
	}
}

/**
 * 校验配置完整性与类型串口径
 *
 * 校验项：应用名/接口/方法名非空、端口范围、接口与方法在各自作用域内唯一、
 * 类型串为擦除口径（拒绝带泛型尖括号与空白字符）、parameters 与
 * parameterTypes 严格对齐；未声明 parameters 时自动补齐为 argN。
 *
 * @return 校验失败返回带字段定位的错误，成功返回 nil
 */
func (c *Config) Validate() error {
	if strings.TrimSpace(c.Application) == "" {
		return fmt.Errorf("配置校验失败: application 必填")
	}
	if c.Port <= 0 || c.Port > 65535 {
		return fmt.Errorf("配置校验失败: port=%d 超出合法范围 1-65535", c.Port)
	}
	if strings.TrimSpace(c.NacosAddress) == "" {
		return fmt.Errorf("配置校验失败: nacosAddress 必填")
	}
	if strings.TrimSpace(c.MetadataGroup) == "" {
		return fmt.Errorf("配置校验失败: metadataGroup 必填")
	}
	if len(c.Services) == 0 {
		return fmt.Errorf("配置校验失败: services 至少声明一个服务")
	}
	ifaceSeen := make(map[string]struct{}, len(c.Services))
	for i := range c.Services {
		svc := &c.Services[i]
		if err := validateService(svc); err != nil {
			return err
		}
		if _, dup := ifaceSeen[svc.Interface]; dup {
			return fmt.Errorf("配置校验失败: services[%d] 接口 %s 重复声明", i, svc.Interface)
		}
		ifaceSeen[svc.Interface] = struct{}{}
	}
	return nil
}

/**
 * 校验单个服务声明并为方法补齐参数定义
 *
 * @return 校验失败返回带接口/方法定位的错误，成功返回 nil
 */
func validateService(svc *ServiceConfig) error {
	if strings.TrimSpace(svc.Interface) == "" {
		return fmt.Errorf("配置校验失败: services[].interface 必填")
	}
	if strings.ContainsAny(svc.Interface, " \t,") {
		return fmt.Errorf("配置校验失败: 接口 %s 含非法字符(空白/逗号)", svc.Interface)
	}
	if len(svc.Methods) == 0 {
		return fmt.Errorf("配置校验失败: 接口 %s 的 methods 至少声明一个方法", svc.Interface)
	}
	methodSeen := make(map[string]struct{}, len(svc.Methods))
	for i := range svc.Methods {
		m := &svc.Methods[i]
		if err := validateMethod(svc.Interface, m); err != nil {
			return err
		}
		if _, dup := methodSeen[m.Name]; dup {
			return fmt.Errorf("配置校验失败: 接口 %s 的方法 %s 重复声明", svc.Interface, m.Name)
		}
		methodSeen[m.Name] = struct{}{}
	}
	return nil
}

/**
 * 校验单个方法声明
 *
 * 类型串口径：基本类型写关键字、对象与集合写擦除 FQCN，禁止泛型串
 * 与空白字符；parameters 与 parameterTypes 必须一一对齐，缺省时自动补齐。
 *
 * @return 校验失败返回带接口/方法定位的错误，成功返回 nil
 */
func validateMethod(iface string, m *MethodConfig) error {
	loc := fmt.Sprintf("接口 %s 方法 %s", iface, m.Name)
	if strings.TrimSpace(m.Name) == "" {
		return fmt.Errorf("配置校验失败: %s: methods[].name 必填", iface)
	}
	if err := validateTypeName(loc, "returnType", m.ReturnType); err != nil {
		return err
	}
	for i, pt := range m.ParameterTypes {
		if err := validateTypeName(loc, fmt.Sprintf("parameterTypes[%d]", i), pt); err != nil {
			return err
		}
	}
	if len(m.Parameters) == 0 {
		// 未声明参数定义时按参数类型自动补齐 argN，保持元数据完整（Admin 测试调用表单依赖）
		m.Parameters = buildDefaultParams(m.ParameterTypes)
		return nil
	}
	if len(m.Parameters) != len(m.ParameterTypes) {
		return fmt.Errorf("配置校验失败: %s: parameters(%d 个) 与 parameterTypes(%d 个) 数量不一致",
			loc, len(m.Parameters), len(m.ParameterTypes))
	}
	for i := range m.Parameters {
		p := &m.Parameters[i]
		if p.Index != i {
			return fmt.Errorf("配置校验失败: %s: parameters[%d].index=%d 应从 0 起连续", loc, i, p.Index)
		}
		if strings.TrimSpace(p.Name) == "" {
			return fmt.Errorf("配置校验失败: %s: parameters[%d].name 必填", loc, i)
		}
		if err := validateTypeName(loc, fmt.Sprintf("parameters[%d].type", i), p.Type); err != nil {
			return err
		}
		if p.Type != m.ParameterTypes[i] {
			return fmt.Errorf("配置校验失败: %s: parameters[%d].type=%s 与 parameterTypes[%d]=%s 不一致",
				loc, i, p.Type, i, m.ParameterTypes[i])
		}
	}
	return nil
}

/**
 * 校验单个 Java 类型串的擦除口径
 *
 * @param loc 定位描述（接口/方法/字段）
 * @param field 字段名
 * @param typeName 待校验类型串
 * @return 非空、不含泛型尖括号与空白字符时返回 nil，否则返回错误
 */
func validateTypeName(loc, field, typeName string) error {
	if strings.TrimSpace(typeName) == "" {
		return fmt.Errorf("配置校验失败: %s: %s 必填", loc, field)
	}
	if strings.ContainsAny(typeName, "<>") {
		return fmt.Errorf("配置校验失败: %s: %s=%s 含泛型标记，请登记擦除类型(如 java.util.List)", loc, field, typeName)
	}
	if strings.ContainsAny(typeName, " \t") {
		return fmt.Errorf("配置校验失败: %s: %s=%s 含空白字符", loc, field, typeName)
	}
	return nil
}

/**
 * 按参数类型串生成默认参数定义
 *
 * @param parameterTypes 擦除参数类型串列表
 * @return 参数名形如 arg0/arg1 的参数定义列表，无参方法返回空列表
 */
func buildDefaultParams(parameterTypes []string) []ParamConfig {
	if len(parameterTypes) == 0 {
		return []ParamConfig{}
	}
	params := make([]ParamConfig, len(parameterTypes))
	for i, pt := range parameterTypes {
		params[i] = ParamConfig{Index: i, Name: fmt.Sprintf("arg%d", i), Type: pt}
	}
	return params
}
