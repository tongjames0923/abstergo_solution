package starter

import (
	"context"
	"os"
	"path/filepath"
	"strings"
	"testing"
)

// validConfigJSON 基础合法配置：单接口单方法，未声明 parameters（应自动补齐 argN）
const validConfigJSON = `{
  "application": "go-test-provider",
  "port": 20099,
  "metadataGroup": "dubbo",
  "services": [
    {
      "interface": "com.example.FooProvider",
      "version": "",
      "group": "",
      "methods": [
        {"name": "Echo", "parameterTypes": ["java.lang.String"], "returnType": "java.lang.String"}
      ]
    }
  ]
}`

// DemoFooImpl 合法实现：导出结构体指针、Reference 匹配、方法真实签名
type DemoFooImpl struct{}

func (f *DemoFooImpl) Reference() string { return "com.example.FooProvider" }

func (f *DemoFooImpl) Echo(ctx context.Context, name string) (string, error) {
	return "hello, " + name, nil
}

// testUser 测试用 POJO：实现 hessian 的 JavaClassName 契约
type testUser struct {
	Id   string
	Name string
}

func (testUser) JavaClassName() string { return "com.example.User" }

// writeTempConfig 将配置内容写入临时文件并返回路径
func writeTempConfig(t *testing.T, content string) string {
	t.Helper()
	path := filepath.Join(t.TempDir(), "service.json")
	if err := os.WriteFile(path, []byte(content), 0o600); err != nil {
		t.Fatalf("写入临时配置失败: %v", err)
	}
	return path
}

// loadValidConfig 加载基础合法配置（供多个用例复用）
func loadValidConfig(t *testing.T) *Config {
	t.Helper()
	cfg, err := LoadConfig(writeTempConfig(t, validConfigJSON))
	if err != nil {
		t.Fatalf("加载合法配置失败: %v", err)
	}
	return cfg
}

func TestLoadConfig_Valid(t *testing.T) {
	cfg := loadValidConfig(t)
	if cfg.Application != "go-test-provider" {
		t.Errorf("application = %q, 期望 go-test-provider", cfg.Application)
	}
	if cfg.Port != 20099 {
		t.Errorf("port = %d, 期望 20099", cfg.Port)
	}
	m := cfg.Services[0].Methods[0]
	if len(m.Parameters) != 1 {
		t.Fatalf("自动补齐后 parameters 长度 = %d, 期望 1", len(m.Parameters))
	}
	p := m.Parameters[0]
	if p.Index != 0 || p.Name != "arg0" || p.Type != "java.lang.String" {
		t.Errorf("自动补齐的参数定义 = %+v, 期望 {0 arg0 java.lang.String}", p)
	}
}

func TestLoadConfig_DefaultsAndEnvOverride(t *testing.T) {
	minimal := `{"application": "go-min-provider", "services": [
		{"interface": "com.example.FooProvider", "methods": [
			{"name": "Echo", "parameterTypes": ["java.lang.String"], "returnType": "java.lang.String"}]}]}`
	path := writeTempConfig(t, minimal)

	cfg, err := LoadConfig(path)
	if err != nil {
		t.Fatalf("加载最小配置失败: %v", err)
	}
	if cfg.Port != defaultPort || cfg.NacosAddress != defaultNacosAddress || cfg.MetadataGroup != defaultMetadataGroup {
		t.Errorf("默认值不符: port=%d nacos=%s group=%s", cfg.Port, cfg.NacosAddress, cfg.MetadataGroup)
	}

	t.Setenv(envNacosAddress, "10.0.0.8:8848")
	t.Setenv(envDubboPort, "30001")
	cfg, err = LoadConfig(path)
	if err != nil {
		t.Fatalf("环境变量覆盖后加载失败: %v", err)
	}
	if cfg.NacosAddress != "10.0.0.8:8848" || cfg.Port != 30001 {
		t.Errorf("环境变量覆盖不生效: nacos=%s port=%d", cfg.NacosAddress, cfg.Port)
	}
}

func TestLoadConfig_Errors(t *testing.T) {
	cases := []struct {
		name    string
		json    string
		wantSub string
	}{
		{
			name:    "缺 application",
			json:    `{"services": [{"interface": "a.B", "methods": [{"name": "m", "parameterTypes": ["java.lang.String"], "returnType": "java.lang.String"}]}]}`,
			wantSub: "application 必填",
		},
		{
			name:    "缺 services",
			json:    `{"application": "app"}`,
			wantSub: "services 至少声明一个服务",
		},
		{
			name:    "缺 methods",
			json:    `{"application": "app", "services": [{"interface": "a.B"}]}`,
			wantSub: "methods 至少声明一个方法",
		},
		{
			name:    "类型串带泛型",
			json:    `{"application": "app", "services": [{"interface": "a.B", "methods": [{"name": "m", "parameterTypes": ["java.util.List<java.lang.String>"], "returnType": "void"}]}]}`,
			wantSub: "含泛型标记",
		},
		{
			name:    "returnType 为空",
			json:    `{"application": "app", "services": [{"interface": "a.B", "methods": [{"name": "m", "parameterTypes": [], "returnType": ""}]}]}`,
			wantSub: "returnType 必填",
		},
		{
			name: "parameters 与 parameterTypes 数量不一致",
			json: `{"application": "app", "services": [{"interface": "a.B", "methods": [
				{"name": "m", "parameterTypes": ["java.lang.String", "int"], "returnType": "void",
				 "parameters": [{"index": 0, "name": "a", "type": "java.lang.String"}]}]}]}`,
			wantSub: "数量不一致",
		},
		{
			name: "parameters index 不连续",
			json: `{"application": "app", "services": [{"interface": "a.B", "methods": [
				{"name": "m", "parameterTypes": ["java.lang.String", "int"], "returnType": "void",
				 "parameters": [{"index": 0, "name": "a", "type": "java.lang.String"}, {"index": 2, "name": "b", "type": "int"}]}]}]}`,
			wantSub: "应从 0 起连续",
		},
		{
			name: "parameters type 与 parameterTypes 不一致",
			json: `{"application": "app", "services": [{"interface": "a.B", "methods": [
				{"name": "m", "parameterTypes": ["java.lang.String"], "returnType": "void",
				 "parameters": [{"index": 0, "name": "a", "type": "int"}]}]}]}`,
			wantSub: "不一致",
		},
		{
			name: "接口重复声明",
			json: `{"application": "app", "services": [
				{"interface": "a.B", "methods": [{"name": "m", "parameterTypes": [], "returnType": "void"}]},
				{"interface": "a.B", "methods": [{"name": "m2", "parameterTypes": [], "returnType": "void"}]}]}`,
			wantSub: "重复声明",
		},
		{
			name: "方法重复声明",
			json: `{"application": "app", "services": [{"interface": "a.B", "methods": [
				{"name": "m", "parameterTypes": [], "returnType": "void"},
				{"name": "m", "parameterTypes": ["java.lang.String"], "returnType": "void"}]}]}`,
			wantSub: "重复声明",
		},
	}
	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			_, err := LoadConfig(writeTempConfig(t, tc.json))
			if err == nil {
				t.Fatalf("期望返回错误, 实际为 nil")
			}
			if !strings.Contains(err.Error(), tc.wantSub) {
				t.Errorf("错误信息 %q 未包含期望片段 %q", err.Error(), tc.wantSub)
			}
		})
	}
}

func TestProviderDataID(t *testing.T) {
	cfg := &Config{Application: "go-user-provider"}
	svc := &ServiceConfig{Interface: "org.apache.dubbo.samples.api.UserProvider", Version: "1.0.0", Group: "demo"}
	got := providerDataID(cfg, svc)
	want := "org.apache.dubbo.samples.api.UserProvider:1.0.0:demo:provider:go-user-provider"
	if got != want {
		t.Errorf("dataId = %q, 期望 %q", got, want)
	}
}

func TestBuildServiceDefinition(t *testing.T) {
	cfg := &Config{Application: "go-user-provider", Port: 20001}
	svc := &ServiceConfig{
		Interface: "com.example.FooProvider",
		Version:   "1.0.0",
		Group:     "demo",
		Methods: []MethodConfig{
			{
				Name:           "Echo",
				ParameterTypes: []string{"java.lang.String"},
				ReturnType:     "java.lang.String",
				Parameters:     []ParamConfig{{Index: 0, Name: "name", Type: "java.lang.String"}},
			},
			{
				Name:           "Ping",
				ParameterTypes: []string{},
				ReturnType:     "void",
				Parameters:     []ParamConfig{},
			},
		},
	}
	def := buildServiceDefinition(cfg, svc)
	if def.CanonicalName != svc.Interface {
		t.Errorf("canonicalName = %q, 期望 %q", def.CanonicalName, svc.Interface)
	}
	if len(def.Methods) != 2 {
		t.Fatalf("methods 数量 = %d, 期望 2", len(def.Methods))
	}
	echo := def.Methods[0]
	if echo.Name != "Echo" || len(echo.ParameterTypes) != 1 || echo.ParameterTypes[0] != "java.lang.String" {
		t.Errorf("Echo 方法签名映射错误: %+v", echo)
	}
	if len(echo.Parameters) != 1 || echo.Parameters[0].Name != "name" {
		t.Errorf("Echo 参数定义映射错误: %+v", echo.Parameters)
	}
	wantParams := map[string]string{
		"application":   "go-user-provider",
		"interface":     "com.example.FooProvider",
		"side":          "provider",
		"version":       "1.0.0",
		"group":         "demo",
		"methods":       "Echo,Ping",
		"protocol":      "dubbo",
		"port":          "20001",
		"serialization": "hessian2",
	}
	for k, want := range wantParams {
		if def.Parameters[k] != want {
			t.Errorf("Parameters[%s] = %q, 期望 %q", k, def.Parameters[k], want)
		}
	}
}

func TestValidateBindings(t *testing.T) {
	cfg := loadValidConfig(t)
	if err := validateBindings(cfg, []Binding{{Interface: "com.example.FooProvider", Impl: &DemoFooImpl{}}}); err != nil {
		t.Fatalf("合法绑定不应报错: %v", err)
	}

	err := validateBindings(cfg, []Binding{{Interface: "com.example.Other", Impl: &DemoFooImpl{}}})
	if err == nil || !strings.Contains(err.Error(), "未在配置中声明") {
		t.Errorf("未声明接口绑定应报错, got=%v", err)
	}

	err = validateBindings(cfg, nil)
	if err == nil || !strings.Contains(err.Error(), "bindings 为空") {
		t.Errorf("空绑定应报错, got=%v", err)
	}
}

func TestValidateImpl_SignatureErrors(t *testing.T) {
	cfg := loadValidConfig(t)
	base := Binding{Interface: "com.example.FooProvider", Impl: &DemoFooImpl{}}

	err := validateBindings(cfg, []Binding{base})
	if err != nil {
		t.Fatalf("合法签名不应报错: %v", err)
	}

	// 方法名不匹配：配置声明 Echo，实现上找不到 NotDeclared
	cfg2 := loadValidConfig(t)
	cfg2.Services[0].Methods[0].Name = "NotDeclared"
	err = validateBindings(cfg2, []Binding{base})
	if err == nil || !strings.Contains(err.Error(), "未找到导出方法") {
		t.Errorf("方法缺失应报错, got=%v", err)
	}

	// returnType 声明 void 但实现返回 string
	cfg3 := loadValidConfig(t)
	cfg3.Services[0].Methods[0].ReturnType = "void"
	err = validateBindings(cfg3, []Binding{base})
	if err == nil || !strings.Contains(err.Error(), "声明为 void") {
		t.Errorf("void 不符应报错, got=%v", err)
	}

	// 参数个数不一致：声明 3 个参数，实现含 ctx 也只有 2 个入参
	cfg4 := loadValidConfig(t)
	cfg4.Services[0].Methods[0].ParameterTypes = []string{"java.lang.String", "int", "long"}
	err = validateBindings(cfg4, []Binding{base})
	if err == nil || !strings.Contains(err.Error(), "与实现不一致") {
		t.Errorf("参数个数不符应报错, got=%v", err)
	}
}

// DemoBadReferenceImpl Reference 返回值与声明不一致
type DemoBadReferenceImpl struct{}

func (b *DemoBadReferenceImpl) Reference() string { return "com.example.Wrong" }

func (b *DemoBadReferenceImpl) Echo(ctx context.Context, name string) (string, error) {
	return name, nil
}

func TestValidateImpl_ReferenceMismatch(t *testing.T) {
	cfg := loadValidConfig(t)
	err := validateBindings(cfg, []Binding{{Interface: "com.example.FooProvider", Impl: &DemoBadReferenceImpl{}}})
	if err == nil || !strings.Contains(err.Error(), "Reference() 返回") {
		t.Errorf("Reference 不匹配应报错, got=%v", err)
	}
}

func TestRegisterPOJOs(t *testing.T) {
	cfg := loadValidConfig(t)
	bindings := []Binding{
		{Interface: "com.example.FooProvider", Impl: &DemoFooImpl{}},
		{Interface: "com.example.FooProvider", Impl: &DemoFooImpl{}, POJOs: []any{&testUser{}, testUser{}}},
	}
	// 同一 POJO 以指针/值两种形态出现、两个绑定重复引用同一实现类型：应全部去重且不报错
	if err := registerPOJOs(cfg, bindings); err != nil {
		t.Fatalf("POJO 注册不应报错: %v", err)
	}
}
