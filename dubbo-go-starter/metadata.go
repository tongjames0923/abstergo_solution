package starter

import (
	"encoding/json"
	"fmt"
	"log"
	"strings"

	"dubbo.apache.org/dubbo-go/v3/common"
	nacosremoting "dubbo.apache.org/dubbo-go/v3/remoting/nacos"
	nacosvo "github.com/nacos-group/nacos-sdk-go/v2/vo"
)

// ------------------------------------------------------------
// Java ServiceDefinition 元数据：Dubbo Admin 服务列表与
// 方法签名的来源，字段与 dubbo-db-demo 原实现保持一致。
// ------------------------------------------------------------

// parameterDefinition 单个入参的 Java 侧定义
type parameterDefinition struct {
	Index int    `json:"index"`
	Name  string `json:"name"`
	Type  string `json:"type"`
}

// methodDefinition 单个方法的 Java 侧签名
type methodDefinition struct {
	Name           string                `json:"name"`
	ParameterTypes []string              `json:"parameterTypes"`
	ReturnType     string                `json:"returnType"`
	Parameters     []parameterDefinition `json:"parameters,omitempty"`
}

// serviceDefinition Java 侧服务定义元数据
type serviceDefinition struct {
	CanonicalName string             `json:"canonicalName"`
	CodeSource    string             `json:"codeSource"`
	Methods       []methodDefinition `json:"methods"`
	Types         []interface{}      `json:"types"`
	Parameters    map[string]string  `json:"parameters"`
}

/**
 * 计算 provider 元数据在 Nacos 配置中心的 dataId
 *
 * 规则与 dubbo-db-demo 原实现一致：interface:version:group:provider:application
 *
 * @param cfg 配置
 * @param svc 服务契约声明
 * @return dataId 字符串
 */
func providerDataID(cfg *Config, svc *ServiceConfig) string {
	return fmt.Sprintf("%s:%s:%s:provider:%s", svc.Interface, svc.Version, svc.Group, cfg.Application)
}

/**
 * 从契约配置构建 Java ServiceDefinition
 *
 * 方法签名与参数定义逐项映射自 MethodConfig；Parameters 与原实现
 * 逐字段一致（application/interface/side/version/group/methods/
 * protocol/port/serialization）。
 *
 * @param cfg 配置
 * @param svc 服务契约声明
 * @return 可序列化的 serviceDefinition
 */
func buildServiceDefinition(cfg *Config, svc *ServiceConfig) serviceDefinition {
	methods := make([]methodDefinition, 0, len(svc.Methods))
	methodNames := make([]string, 0, len(svc.Methods))
	for i := range svc.Methods {
		m := &svc.Methods[i]
		params := make([]parameterDefinition, 0, len(m.Parameters))
		for j := range m.Parameters {
			p := m.Parameters[j]
			params = append(params, parameterDefinition{Index: p.Index, Name: p.Name, Type: p.Type})
		}
		methods = append(methods, methodDefinition{
			Name:           m.Name,
			ParameterTypes: m.ParameterTypes,
			ReturnType:     m.ReturnType,
			Parameters:     params,
		})
		methodNames = append(methodNames, m.Name)
	}
	return serviceDefinition{
		CanonicalName: svc.Interface,
		CodeSource:    "",
		Methods:       methods,
		Types:         []interface{}{},
		Parameters: map[string]string{
			"application":   cfg.Application,
			"interface":     svc.Interface,
			"side":          "provider",
			"version":       svc.Version,
			"group":         svc.Group,
			"methods":       strings.Join(methodNames, ","),
			"protocol":      "dubbo",
			"port":          fmt.Sprint(cfg.Port),
			"serialization": "hessian2",
		},
	}
}

/**
 * 复用单个 Nacos 配置客户端逐接口发布 provider 元数据
 *
 * 相比逐接口新建/关闭客户端的原实现，多接口场景只建立一次连接，
 * 顺序发布（不并发，避免 Nacos 侧乱序与瞬时压力），完成后统一关闭。
 *
 * @param cfg 配置
 * @return 任一接口发布失败时返回带接口定位的错误，成功返回 nil
 */
func publishProviderDefinitions(cfg *Config) error {
	u, err := common.NewURL(fmt.Sprintf("nacos://%s?remote-client-name=%s-meta", cfg.NacosAddress, cfg.Application))
	if err != nil {
		return fmt.Errorf("构建 nacos URL 失败: %w", err)
	}
	cc, err := nacosremoting.NewNacosConfigClientByUrl(u)
	if err != nil {
		return fmt.Errorf("创建 nacos 配置客户端失败: %w", err)
	}
	defer cc.Close()
	for i := range cfg.Services {
		svc := &cfg.Services[i]
		def := buildServiceDefinition(cfg, svc)
		data, err := json.Marshal(def)
		if err != nil {
			return fmt.Errorf("序列化 ServiceDefinition 失败(接口 %s): %w", svc.Interface, err)
		}
		dataId := providerDataID(cfg, svc)
		ok, err := cc.Client().PublishConfig(nacosvo.ConfigParam{
			DataId:  dataId,
			Group:   cfg.MetadataGroup,
			Content: string(data),
		})
		if err != nil {
			return fmt.Errorf("发布 provider 元数据失败(接口 %s): %w", svc.Interface, err)
		}
		if !ok {
			return fmt.Errorf("发布 provider 元数据被 Nacos 拒绝, 接口 %s dataId=%s", svc.Interface, dataId)
		}
		log.Printf("[starter] provider 元数据已发布: dataId=%s group=%s", dataId, cfg.MetadataGroup)
	}
	return nil
}
