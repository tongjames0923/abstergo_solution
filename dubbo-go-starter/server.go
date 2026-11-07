package starter

import (
	"fmt"
	"log"

	"dubbo.apache.org/dubbo-go/v3"
	_ "dubbo.apache.org/dubbo-go/v3/cluster/cluster/failover"
	_ "dubbo.apache.org/dubbo-go/v3/cluster/loadbalance/random"
	"dubbo.apache.org/dubbo-go/v3/common"
	_ "dubbo.apache.org/dubbo-go/v3/config_center/nacos"
	_ "dubbo.apache.org/dubbo-go/v3/filter/accesslog"
	_ "dubbo.apache.org/dubbo-go/v3/filter/echo"
	_ "dubbo.apache.org/dubbo-go/v3/filter/exec_limit"
	_ "dubbo.apache.org/dubbo-go/v3/filter/generic"
	_ "dubbo.apache.org/dubbo-go/v3/filter/graceful_shutdown"
	_ "dubbo.apache.org/dubbo-go/v3/filter/token"
	_ "dubbo.apache.org/dubbo-go/v3/filter/tps"
	_ "dubbo.apache.org/dubbo-go/v3/filter/tps/limiter"
	_ "dubbo.apache.org/dubbo-go/v3/filter/tps/strategy"
	"dubbo.apache.org/dubbo-go/v3/metadata"
	_ "dubbo.apache.org/dubbo-go/v3/metadata/mapping/metadata"
	_ "dubbo.apache.org/dubbo-go/v3/metadata/report/nacos"
	"dubbo.apache.org/dubbo-go/v3/protocol"
	_ "dubbo.apache.org/dubbo-go/v3/protocol/dubbo"
	_ "dubbo.apache.org/dubbo-go/v3/proxy/proxy_factory"
	"dubbo.apache.org/dubbo-go/v3/registry"
	_ "dubbo.apache.org/dubbo-go/v3/registry/directory"
	_ "dubbo.apache.org/dubbo-go/v3/registry/nacos"
	_ "dubbo.apache.org/dubbo-go/v3/registry/protocol"
	_ "dubbo.apache.org/dubbo-go/v3/registry/servicediscovery"
	"dubbo.apache.org/dubbo-go/v3/server"
)

// dubbo-go 的 SPI 实现靠 blank import 触发各包 init 注册工厂，这里按最小集合精确导入：
// registry/protocol（缺失会 panic: protocol for [registry] is not existing）、
// registry/nacos + config_center/nacos、protocol/dubbo、metadata/report/nacos、
// filter/generic（泛化调用）及默认 filter 链所需实现。
// 刻意不导入 imports 整包：它会连带注册 protocol/triple 与 triple/reflection，
// 导致服务额外暴露 tri://:50051 与 grpc.reflection 服务，Java 消费端会误选 Triple。

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
		// 显式锁定协议 ID，避免默认 tri 协议配置被一并导出
		server.WithServerProtocolIDs([]string{"dubbo"}),
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
