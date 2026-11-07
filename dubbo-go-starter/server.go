package starter

import (
	"fmt"
	"log"

	"dubbo.apache.org/dubbo-go/v3"
	"dubbo.apache.org/dubbo-go/v3/common"
	"dubbo.apache.org/dubbo-go/v3/metadata"
	"dubbo.apache.org/dubbo-go/v3/protocol"
	"dubbo.apache.org/dubbo-go/v3/registry"
	"dubbo.apache.org/dubbo-go/v3/server"
)

/**
 * 按既有链路装配 dubbo 实例与服务端
 *
 * 固定仅发布 dubbo(TCP) 协议：MetadataService 也走 dubbo 协议并复用同一端口，
 * 注册 URL 里不会出现 tri://，Java 消费端与 Dubbo Admin 不会误选 Triple。
 * 注册中心为 Nacos，且同时注册服务级与接口级（Admin 服务列表可见）。
 *
 * @param cfg 配置
 * @return 装配完成的 Server，失败时返回错误
 */
func newServer(cfg *Config) (*server.Server, error) {
	ins, err := dubbo.NewInstance(
		dubbo.WithName(cfg.Application),
		dubbo.WithRemoteMetadata(),
		dubbo.WithMetadataServiceProtocol("dubbo"),
		dubbo.WithMetadataServicePort(cfg.Port),
		dubbo.WithMetadataReport(
			metadata.WithProtocol("nacos"),
			metadata.WithAddress(cfg.NacosAddress),
		),
		dubbo.WithRegistry(
			registry.WithNacos(),
			registry.WithAddress(cfg.NacosAddress),
			registry.WithRegisterServiceAndInterface(),
		),
	)
	if err != nil {
		return nil, fmt.Errorf("创建 Dubbo 实例失败: %w", err)
	}
	srv, err := ins.NewServer(
		server.WithServerProtocol(
			protocol.WithDubbo(),
			protocol.WithPort(cfg.Port),
		),
	)
	if err != nil {
		return nil, fmt.Errorf("创建 Server 失败: %w", err)
	}
	return srv, nil
}

/**
 * 逐接口注册服务实现
 *
 * 无 IDL 注册：dubbo 协议下 Java 的 GenericService.$invoke 由
 * generic_service filter（默认 provider 链）按方法名还原成真实调用，
 * 不需要 protobuf ServiceInfo，也不经过 Triple 的路径与 codec 判断。
 * 序列化固定 hessian2。
 *
 * @param srv 服务端
 * @param bindings 业务绑定表（已通过契约校验）
 * @return 任一接口注册失败时返回带接口定位的错误，成功返回 nil
 */
func registerServices(srv *server.Server, bindings []Binding) error {
	for i := range bindings {
		b := &bindings[i]
		rpc, ok := b.Impl.(common.RPCService)
		if !ok {
			return fmt.Errorf("注册服务失败(接口 %s): 实现未提供 Reference() string", b.Interface)
		}
		if err := srv.RegisterService(
			rpc,
			server.WithInterface(b.Interface),
			server.WithSerialization("hessian2"),
		); err != nil {
			return fmt.Errorf("注册服务失败(接口 %s): %w", b.Interface, err)
		}
		log.Printf("[starter] 服务已注册: %s", b.Interface)
	}
	return nil
}
