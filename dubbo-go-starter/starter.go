package starter

import (
	"fmt"
	"log"
	"os"
	"os/signal"
	"strings"
	"syscall"
)

/**
 * 对外门面：加载配置并启动全部服务，随后阻塞运行
 *
 * 编排顺序（时序硬约束）：加载与校验 → 契约绑定校验 → POJO 注册（必须在
 * RegisterService 与 Serve 之前）→ 实例/服务端装配（仅 dubbo + hessian2）→
 * 逐接口注册 → 复用单连接发布 ServiceDefinition 元数据 → 阻塞 Serve，
 * 同时监听退出信号（Windows 下以 Ctrl+C/os.Interrupt 为主）。
 *
 * @param cfgPath 配置文件路径，为空时取当前目录下的 service.json
 * @param bindings 业务绑定表：每个配置声明的接口对应一个实现
 * @return 启动或运行失败时返回带定位的错误；收到退出信号时返回 nil
 */
func Run(cfgPath string, bindings ...Binding) error {
	if strings.TrimSpace(cfgPath) == "" {
		cfgPath = DefaultConfigPath
	}
	cfg, err := LoadConfig(cfgPath)
	if err != nil {
		return err
	}
	if err := validateBindings(cfg, bindings); err != nil {
		return err
	}
	if err := registerPOJOs(cfg, bindings); err != nil {
		return err
	}
	srv, err := newServer(cfg)
	if err != nil {
		return err
	}
	if err := registerServices(srv, bindings); err != nil {
		return err
	}
	if err := publishProviderDefinitions(cfg); err != nil {
		return fmt.Errorf("发布 provider 元数据失败: %w", err)
	}
	printStartupSummary(cfg)

	sigCh := make(chan os.Signal, 1)
	signal.Notify(sigCh, os.Interrupt, syscall.SIGTERM)
	serveErr := make(chan error, 1)
	go func() {
		serveErr <- srv.Serve()
	}()
	select {
	case err := <-serveErr:
		return fmt.Errorf("服务运行失败: %w", err)
	case s := <-sigCh:
		// 进程直接退出；dubbo-go v3.3.2 未提供稳定公开的停机入口，
		// Nacos 侧依赖实例心跳超时完成摘除
		log.Printf("[starter] 收到信号 %v，进程退出（Nacos 实例将随心跳超时摘除）", s)
		return nil
	}
}

/**
 * 打印启动摘要
 *
 * 输出 Nacos 地址、dubbo 端口与每个接口的方法签名概览，
 * 便于核对与 Dubbo Admin 泛化调用测试（method=xx parameterTypes=[...]）。
 *
 * @param cfg 配置
 */
func printStartupSummary(cfg *Config) {
	log.Printf("[starter] Go Dubbo 服务已启动（仅 dubbo 协议 / 支持 Java 泛化调用）")
	log.Printf("[starter]   Nacos:      %s", cfg.NacosAddress)
	log.Printf("[starter]   Dubbo(TCP): %d", cfg.Port)
	for i := range cfg.Services {
		svc := &cfg.Services[i]
		log.Printf("[starter]   接口:       %s", svc.Interface)
		for j := range svc.Methods {
			m := &svc.Methods[j]
			log.Printf("[starter]     %s(%s) -> %s", m.Name, strings.Join(m.ParameterTypes, ", "), m.ReturnType)
		}
	}
}
