export default {
  // 通用
  common: {
    success: '操作成功',
    fail: '操作失败',
    error: '系统异常',
    confirm: '确认',
    cancel: '取消',
    save: '保存',
    delete: '删除',
    edit: '编辑',
    add: '新增',
    search: '搜索',
    reset: '重置',
    loading: '加载中...',
    noData: '暂无数据',
    tip: '提示',
    warning: '警告',
    info: '信息',
    yes: '是',
    no: '否'
  },

  // 登录
  login: {
    title: '系统登录',
    username: '用户名',
    password: '密码',
    login: '登录',
    logout: '退出登录',
    logoutSuccess: '已退出登录',
    usernameRequired: '请输入正确的用户名',
    passwordRequired: '密码不能少于6位',
    loginSuccess: '登录成功',
    loginFailed: '登录失败',
    sessionExpired: '登录已过期，请重新登录',
    sessionExpiredDetail: '登录已过期，请重新登录。详情：',
    tokenRefreshing: '登录状态即将过期，正在自动续期...',
    relogin: '重新登录',
    rememberMe: '记住我',
    loggingIn: '登录中...',
    copyright: '保留所有权利.'
  },

  // 导航
  nav: {
    home: '首页',
    dashboard: '仪表盘',
    profile: '个人中心',
    settings: '设置',
    fullscreen: '全屏',
    language: '语言',
    chinese: '中文',
    english: 'English'
  },

  // 侧边栏
  sidebar: {
    dashboard: '首页',
    admin: '系统管理',
    user: '用户管理',
    role: '角色管理',
    department: '部门管理',
    company: '公司管理',
    dic: '数据字典',
    systemlog: '系统日志',
    cache: '缓存管理',
    form: '表单管理',
    formCreator: '表单创建',
    formManager: '表单管理',
    genericApi: '泛化API'
  },

  // 用户管理
  user: {
    title: '用户管理',
    username: '用户名',
    nickname: '昵称',
    avatar: '头像',
    status: '状态',
    createTime: '创建时间',
    updateTime: '更新时间',
    operation: '操作',
    edit: '编辑',
    delete: '删除',
    resetPassword: '重置密码',
    enable: '启用',
    disable: '禁用',
    lock: '锁定',
    unlock: '解锁',
    addUser: '添加用户',
    editUser: '编辑用户',
    deleteUser: '删除用户',
    confirmDelete: '确定要删除该用户吗？',
    roleAssignment: '角色分配',
    loginInfo: '登录信息',
    noLoginInfo: '暂无登录信息'
  },

  // 角色管理
  role: {
    title: '角色管理',
    name: '角色名称',
    description: '角色描述',
    status: '状态',
    operation: '操作',
    addRole: '添加角色',
    editRole: '编辑角色',
    deleteRole: '删除角色',
    confirmDelete: '确定要删除该角色吗？'
  },

  // 部门管理
  department: {
    title: '部门管理',
    name: '部门名称',
    code: '部门编码',
    manager: '负责人',
    parent: '上级部门',
    company: '所属公司',
    status: '状态',
    operation: '操作',
    addDepartment: '添加部门',
    editDepartment: '编辑部门',
    deleteDepartment: '删除部门',
    confirmDelete: '确定要删除该部门吗？',
    addChild: '添加子部门'
  },

  // 公司管理
  company: {
    title: '公司管理',
    name: '公司名称',
    code: '公司编码',
    taxNumber: '税号',
    description: '描述',
    logo: 'Logo',
    status: '状态',
    operation: '操作',
    addCompany: '添加公司',
    editCompany: '编辑公司',
    deleteCompany: '删除公司',
    confirmDelete: '确定要删除该公司吗？'
  },

  // 数据字典
  dic: {
    title: '数据字典',
    name: '字典名称',
    code: '字典编码',
    value: '字典值',
    description: '描述',
    operation: '操作',
    addDic: '添加字典',
    editDic: '编辑字典',
    deleteDic: '删除字典',
    confirmDelete: '确定要删除该字典吗？'
  },

  // 系统日志
  systemlog: {
    title: '系统日志',
    level: '日志级别',
    message: '日志消息',
    timestamp: '时间',
    traceId: '追踪ID',
    search: '搜索日志',
    clear: '清空日志'
  },

  // 缓存管理
  cache: {
    title: '缓存管理',
    key: '缓存键',
    value: '缓存值',
    ttl: '过期时间',
    operation: '操作',
    clear: '清空缓存',
    refresh: '刷新',
    confirmClear: '确定要清空所有缓存吗？'
  },

  // 表单管理
  form: {
    title: '表单管理',
    name: '表单名称',
    code: '表单编码',
    description: '描述',
    operation: '操作',
    create: '创建表单',
    edit: '编辑表单',
    delete: '删除表单',
    preview: '预览',
    confirmDelete: '确定要删除该表单吗？'
  },

  // 错误页面
  error: {
    403: '抱歉，您没有权限访问此页面',
    404: '抱歉，您访问的页面不存在',
    backHome: '返回首页',
    backPrev: '返回上一页'
  },

  // 泛化API管理
  genericApi: {
    title: '泛化API管理',
    subtitle: '管理系统泛化API接口配置，支持动态服务调用',
    add: '新增API',
    addTitle: '新增泛化API配置',
    editTitle: '编辑泛化API配置',
    apiId: 'API标识',
    apiIdPlaceholder: '搜索API标识',
    apiIdRequired: '请输入API标识',
    apiName: 'API名称',
    apiNamePlaceholder: '搜索API名称',
    apiNameInputPlaceholder: '请输入API名称',
    apiNameRequired: '请输入API名称',
    appName: '应用名称',
    appNamePlaceholder: '搜索应用名称',
    appNameInputPlaceholder: '请输入应用名称（服务名）',
    appNameRequired: '请输入应用名称',
    interfaceName: '接口名称',
    interfaceNamePlaceholder: '搜索接口名称',
    interfaceNameInputPlaceholder: '请输入完整接口类名',
    interfaceNameRequired: '请输入接口名称',
    methodName: '方法名称',
    methodNamePlaceholder: '请输入调用方法名',
    methodNameRequired: '请输入方法名称',
    needAuth: '需要鉴权',
    version: '版本',
    versionPlaceholder: '如 1.0.0',
    description: '描述',
    descriptionPlaceholder: '请输入API描述信息（可选）',
    status: '状态',
    allStatus: '全部状态',
    enabled: '启用',
    disabled: '禁用',
    enable: '启用',
    disable: '禁用',
    operation: '操作',
    updateTime: '更新时间',
    saving: '保存中...',
    confirmToggle: '确认要{action}API「{name}」吗？',
    confirmDelete: '确认要删除API「{name}」吗？此操作不可恢复。',
    test: '测试',
    testPrivate: '私有API测试',
    testPublic: '公共API测试',
    testDialogTitle: '测试API - {apiName}',
    requestBody: '请求体',
    executeTest: '执行测试',
    responseResult: '响应结果',
    emptyTitle: '暂无API配置',
    emptyDesc: '点击「新增API」按钮创建第一个泛化API配置'
  },

  // 网络请求
  request: {
    networkError: '网络连接失败，请检查网络设置',
    configError: '返回配置错误，请检查网络设置',
    tokenExpired: 'Token失效且刷新Token失败',
    redirectTip: '跳转提示',
    redirectMessage: '即将跳转到: {path}'
  }
}
