package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"os"

	"dubbo.apache.org/dubbo-go/v3"
	"dubbo.apache.org/dubbo-go/v3/common"
	_ "dubbo.apache.org/dubbo-go/v3/imports"
	"dubbo.apache.org/dubbo-go/v3/metadata"
	"dubbo.apache.org/dubbo-go/v3/protocol"
	"dubbo.apache.org/dubbo-go/v3/registry"
	nacosremoting "dubbo.apache.org/dubbo-go/v3/remoting/nacos"
	"dubbo.apache.org/dubbo-go/v3/server"
	hessian "github.com/apache/dubbo-go-hessian2"
	nacosvo "github.com/nacos-group/nacos-sdk-go/v2/vo"
)

const (
	appName       = "go-user-provider"
	ifaceName     = "org.apache.dubbo.samples.api.UserProvider"
	dubboPort     = 20001
	svcVersion    = ""
	svcGroup      = ""
	metaDataGroup = "dubbo"
)

// User 由 hessian2 传输，JavaClassName 与元数据里的返回类型保持一致。
type User struct {
	Id   string
	Name string
	Age  int32
}

func (User) JavaClassName() string {
	return "org.apache.dubbo.samples.api.User"
}

func init() {
	hessian.RegisterPOJO(&User{})
}

// UserProviderImpl 的类型名必须导出，且方法用真实签名：
// generic_service filter 会把 $invoke(method, types, args) 按反射出的参数类型
// 还原成具体值再调用，签名写成 []interface{} 会导致 string 无法 realize。
type UserProviderImpl struct{}

func (u *UserProviderImpl) Reference() string {
	return ifaceName
}

func (u *UserProviderImpl) Echo(ctx context.Context, name string) (string, error) {
	log.Printf("[Echo] name=%q", name)
	return "hello, " + name, nil
}

func (u *UserProviderImpl) GetUser(ctx context.Context, name string) (*User, error) {
	log.Printf("[GetUser] name=%q", name)
	return &User{Id: "1001", Name: name, Age: 28}, nil
}

// ------------------------------------------------------------
// Java ServiceDefinition 元数据：admin / 服务列表的方法签名来源
// ------------------------------------------------------------

type parameterDefinition struct {
	Index int    `json:"index"`
	Name  string `json:"name"`
	Type  string `json:"type"`
}

type methodDefinition struct {
	Name           string                `json:"name"`
	ParameterTypes []string              `json:"parameterTypes"`
	ReturnType     string                `json:"returnType"`
	Parameters     []parameterDefinition `json:"parameters,omitempty"`
}

type serviceDefinition struct {
	CanonicalName string             `json:"canonicalName"`
	CodeSource    string             `json:"codeSource"`
	Methods       []methodDefinition `json:"methods"`
	Types         []interface{}      `json:"types"`
	Parameters    map[string]string  `json:"parameters"`
}

func stringParam() []parameterDefinition {
	return []parameterDefinition{{Index: 0, Name: "name", Type: "java.lang.String"}}
}

func publishProviderDefinition(nacosAddr string) error {
	u, err := common.NewURL(fmt.Sprintf("nacos://%s?remote-client-name=%s-meta", nacosAddr, appName))
	if err != nil {
		return fmt.Errorf("构建 nacos URL 失败: %w", err)
	}
	cc, err := nacosremoting.NewNacosConfigClientByUrl(u)
	if err != nil {
		return fmt.Errorf("创建 nacos 配置客户端失败: %w", err)
	}
	defer cc.Close()

	def := serviceDefinition{
		CanonicalName: ifaceName,
		CodeSource:    "",
		Methods: []methodDefinition{
			{
				Name:           "Echo",
				ParameterTypes: []string{"java.lang.String"},
				ReturnType:     "java.lang.String",
				Parameters:     stringParam(),
			},
			{
				Name:           "GetUser",
				ParameterTypes: []string{"java.lang.String"},
				ReturnType:     "org.apache.dubbo.samples.api.User",
				Parameters:     stringParam(),
			},
		},
		Types: []interface{}{},
		Parameters: map[string]string{
			"application":   appName,
			"interface":     ifaceName,
			"side":          "provider",
			"version":       svcVersion,
			"group":         svcGroup,
			"methods":       "Echo,GetUser",
			"protocol":      "dubbo",
			"port":          fmt.Sprint(dubboPort),
			"serialization": "hessian2",
		},
	}
	data, err := json.Marshal(def)
	if err != nil {
		return fmt.Errorf("序列化 ServiceDefinition 失败: %w", err)
	}

	dataId := fmt.Sprintf("%s:%s:%s:provider:%s", ifaceName, svcVersion, svcGroup, appName)
	ok, err := cc.Client().PublishConfig(nacosvo.ConfigParam{
		DataId:  dataId,
		Group:   metaDataGroup,
		Content: string(data),
	})
	if err != nil {
		return fmt.Errorf("发布 provider 元数据失败: %w", err)
	}
	if !ok {
		return fmt.Errorf("发布 provider 元数据被 Nacos 拒绝, dataId=%s", dataId)
	}
	fmt.Printf("  provider 元数据已发布: dataId=%s group=%s\n", dataId, metaDataGroup)
	return nil
}

func main() {
	nacosAddr := os.Getenv("NACOS_ADDRESS")
	if nacosAddr == "" {
		nacosAddr = "127.0.0.1:8848"
	}

	ins, err := dubbo.NewInstance(
		dubbo.WithName(appName),
		dubbo.WithRemoteMetadata(),
		// 只发 dubbo(TCP)：MetadataService 也走 dubbo 协议并复用同一端口，
		// 注册 URL 里不再出现 tri://，Java 消费端不会误选 Triple。
		dubbo.WithMetadataServiceProtocol("dubbo"),
		dubbo.WithMetadataServicePort(dubboPort),
		dubbo.WithMetadataReport(
			metadata.WithProtocol("nacos"),
			metadata.WithAddress(nacosAddr),
		),
		dubbo.WithRegistry(
			registry.WithNacos(),
			registry.WithAddress(nacosAddr),
			registry.WithRegisterServiceAndInterface(),
		),
	)
	if err != nil {
		log.Fatalf("创建 Dubbo 实例失败: %v", err)
	}

	// 只发布 dubbo(TCP) 协议：注册中心里只有 dubbo:// URL，
	// Java 消费端走 TCP + hessian2，彻底绕开 Triple 的 HTTP/2、
	// content-type 校验与 codec 分支。
	srv, err := ins.NewServer(
		server.WithServerProtocol(
			protocol.WithDubbo(),
			protocol.WithPort(dubboPort),
		),
	)
	if err != nil {
		log.Fatalf("创建 Server 失败: %v", err)
	}

	// 无 IDL 注册：dubbo 协议下 Java 的 GenericService.$invoke 由
	// generic_service filter（默认 provider 链）按方法名还原成真实调用，
	// 不需要 protobuf ServiceInfo，也不经过 Triple 的路径与 codec 判断。
	if err := srv.RegisterService(
		&UserProviderImpl{},
		server.WithInterface(ifaceName),
		server.WithSerialization("hessian2"),
	); err != nil {
		log.Fatalf("注册服务失败: %v", err)
	}

	if err := publishProviderDefinition(nacosAddr); err != nil {
		log.Fatalf("发布 provider 元数据失败: %v", err)
	}

	fmt.Println("Go Dubbo 服务已启动（仅 dubbo 协议 / 支持 Java 泛化调用）")
	fmt.Printf("  Nacos:      %s\n", nacosAddr)
	fmt.Printf("  接口:       %s\n", ifaceName)
	fmt.Printf("  Dubbo(TCP): %d\n", dubboPort)
	fmt.Println("  方法:       Echo(java.lang.String) -> java.lang.String")
	fmt.Println("              GetUser(java.lang.String) -> org.apache.dubbo.samples.api.User")
	fmt.Println("  泛化调用:   method=GetUser  parameterTypes=[\"java.lang.String\"]  args=[\"zhangsan\"]")
	fmt.Println("  成功时控制台打印 [Echo] / [GetUser] 日志")

	if err := srv.Serve(); err != nil {
		log.Fatalf("服务运行失败: %v", err)
	}
}
