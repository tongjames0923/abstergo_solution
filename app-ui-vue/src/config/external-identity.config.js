/**
 * 外部身份绑定配置
 *
 * providers：可绑定的外部身份平台（顺序即控件内展示顺序），字段说明：
 * - identityType：外部身份类型，与后端绑定表 identity_type 一致
 * - labelKey：平台展示名 i18n key
 * - icon：平台图标（Element UI icon class），可省略
 * - authorize.builder：授权跳转构造器名称（见 src/identity/authorize.js）
 * - authorize.state：授权回传状态标识，约定为「{identityType}-bind」
 * - bindLabelKey：绑定按钮自定义文案 i18n key（可省略，缺省为「绑定 + 平台名」）
 *
 * 新增可绑定平台：在此追加一项 + 注册对应授权构造器即可，控件零改动。
 */
export default {
  providers: [
    {
      identityType: 'wecom',
      labelKey: 'identity.type.wecom',
      icon: 'el-icon-chat-line-round',
      authorize: {
        builder: 'wecom-web-login',
        state: 'wecom-bind'
      }
    }
  ]
}
