package main

import (
	"context"
	"log"

	starter "dubbo-go-starter"
)

// User 由 hessian2 传输，JavaClassName 与配置/元数据里的返回类型保持一致。
// POJO 注册由 starter 启动时反射扫描自动完成，无需手工 init 注册。
type User struct {
	Id   string
	Name string
	Age  int32
}

func (User) JavaClassName() string {
	return "org.apache.dubbo.samples.api.User"
}

// UserProviderImpl 的类型名必须导出，且方法用真实签名：
// generic_service filter 会把 $invoke(method, types, args) 按反射出的参数类型
// 还原成具体值再调用，签名写成 []interface{} 会导致 string 无法 realize。
type UserProviderImpl struct{}

func (u *UserProviderImpl) Reference() string {
	return "org.apache.dubbo.samples.api.UserProvider"
}

func (u *UserProviderImpl) Echo(ctx context.Context, name string) (string, error) {
	log.Printf("[Echo] name=%q", name)
	return "hello, " + name, nil
}

func (u *UserProviderImpl) GetUser(ctx context.Context, name string) (*User, error) {
	log.Printf("[GetUser] name=%q", name)
	return &User{Id: "1001", Name: name, Age: 28}, nil
}

func main() {
	// 契约（接口/方法/参数/返回类型）见 service.json；
	// NACOS_ADDRESS / DUBBO_PORT 环境变量可覆盖配置。
	if err := starter.Run("service.json",
		starter.Binding{
			Interface: "org.apache.dubbo.samples.api.UserProvider",
			Impl:      &UserProviderImpl{},
		},
	); err != nil {
		log.Fatalf("服务启动失败: %v", err)
	}
}
