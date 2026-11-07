package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"

	starter "dubbo-go-starter"
)

// User 由 hessian2 传输，JavaClassName 与配置/元数据里的返回类型保持一致。
// m tag 决定泛化调用 Map 形态下的字段名；POJO 注册由 starter 启动期反射扫描自动完成。
type User struct {
	Id   string `m:"id" json:"id"`
	Name string `m:"name" json:"name"`
	Age  int32  `m:"age" json:"age"`
}

func (User) JavaClassName() string {
	return "org.apache.dubbo.samples.api.User"
}

// UserQuery 复杂查询条件：包含字符串、数值与嵌套 List 字段，
// 用于演示 List<复杂对象> 入参在泛化调用中按 Map 还原成结构体。
type UserQuery struct {
	Keyword string   `m:"keyword" json:"keyword"`
	MinAge  int32    `m:"minAge" json:"minAge"`
	Tags    []string `m:"tags" json:"tags"`
}

func (UserQuery) JavaClassName() string {
	return "org.apache.dubbo.samples.api.UserQuery"
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

// FindByNames 演示 List<String> 入参与 List<POJO> 返回。
// 配置登记 java.util.List（擦除类型），元素类型由本方法真实签名 []string 决定。
func (u *UserProviderImpl) FindByNames(ctx context.Context, names []string) ([]*User, error) {
	log.Printf("[FindByNames] 还原入参 names=%s", toJSON(names))
	users := make([]*User, 0, len(names))
	for i, name := range names {
		users = append(users, &User{Id: fmt.Sprintf("200%d", i), Name: name, Age: int32(20 + i)})
	}
	return users, nil
}

// FilterUsers 演示 List<复杂对象> + String 混合入参与 List<POJO> 返回。
// Java/Admin 侧传 Map 列表，starter 注册的 POJO 与 generic filter 按 []*UserQuery 还原。
func (u *UserProviderImpl) FilterUsers(ctx context.Context, queries []*UserQuery, tag string) ([]*User, error) {
	log.Printf("[FilterUsers] 还原入参 queries=%s tag=%q", toJSON(queries), tag)
	users := make([]*User, 0, len(queries))
	for i, q := range queries {
		if q == nil {
			continue
		}
		if tag != "" && !containsString(q.Tags, tag) {
			continue
		}
		users = append(users, &User{Id: fmt.Sprintf("300%d", i), Name: q.Keyword, Age: q.MinAge})
	}
	return users, nil
}

// UpdateUser 演示复杂 POJO 入参与 POJO 返回（Map → struct → Map 往返）。
func (u *UserProviderImpl) UpdateUser(ctx context.Context, user *User) (*User, error) {
	log.Printf("[UpdateUser] 还原入参 user=%s", toJSON(user))
	if user == nil {
		return nil, nil
	}
	return &User{Id: user.Id, Name: user.Name + "(updated)", Age: user.Age + 1}, nil
}

/**
 * 判断字符串切片是否包含目标值
 *
 * @param list 字符串切片
 * @param target 目标值
 * @return 包含时返回 true
 */
func containsString(list []string, target string) bool {
	for _, item := range list {
		if item == target {
			return true
		}
	}
	return false
}

/**
 * 将任意值序列化为 JSON，仅用于服务端日志打印还原后的参数
 *
 * @param v 待打印的值
 * @return JSON 文本，序列化失败时返回错误描述
 */
func toJSON(v any) string {
	data, err := json.Marshal(v)
	if err != nil {
		return fmt.Sprintf("<marshal failed: %v>", err)
	}
	return string(data)
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
