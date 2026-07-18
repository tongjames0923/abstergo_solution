export default {
  // Common
  common: {
    success: 'Operation successful',
    fail: 'Operation failed',
    error: 'System error',
    confirm: 'Confirm',
    cancel: 'Cancel',
    save: 'Save',
    delete: 'Delete',
    edit: 'Edit',
    add: 'Add',
    search: 'Search',
    reset: 'Reset',
    loading: 'Loading...',
    noData: 'No data',
    tip: 'Tip',
    warning: 'Warning',
    info: 'Info'
  },

  // Login
  login: {
    title: 'System Login',
    username: 'Username',
    password: 'Password',
    login: 'Login',
    logout: 'Logout',
    logoutSuccess: 'Logged out successfully',
    usernameRequired: 'Please enter a valid username',
    passwordRequired: 'Password must be at least 6 characters',
    loginSuccess: 'Login successful',
    loginFailed: 'Login failed',
    sessionExpired: 'Session expired, please login again',
    sessionExpiredDetail: 'Session expired, please login again. Details: ',
    tokenRefreshing: 'Session expiring soon, refreshing...',
    relogin: 'Relogin',
    rememberMe: 'Remember me',
    loggingIn: 'Logging in...',
    copyright: 'All rights reserved.'
  },

  // Navigation
  nav: {
    home: 'Home',
    dashboard: 'Dashboard',
    profile: 'Profile',
    settings: 'Settings',
    fullscreen: 'Fullscreen',
    language: 'Language',
    chinese: '中文',
    english: 'English'
  },

  // Sidebar
  sidebar: {
    dashboard: 'Dashboard',
    admin: 'System Admin',
    user: 'User Management',
    role: 'Role Management',
    department: 'Department Management',
    company: 'Company Management',
    dic: 'Data Dictionary',
    systemlog: 'System Log',
    cache: 'Cache Management',
    form: 'Form Management',
    formCreator: 'Form Creator',
    formManager: 'Form Manager'
  },

  // User Management
  user: {
    title: 'User Management',
    username: 'Username',
    nickname: 'Nickname',
    avatar: 'Avatar',
    status: 'Status',
    createTime: 'Create Time',
    updateTime: 'Update Time',
    operation: 'Operation',
    edit: 'Edit',
    delete: 'Delete',
    resetPassword: 'Reset Password',
    enable: 'Enable',
    disable: 'Disable',
    lock: 'Lock',
    unlock: 'Unlock',
    addUser: 'Add User',
    editUser: 'Edit User',
    deleteUser: 'Delete User',
    confirmDelete: 'Are you sure to delete this user?',
    roleAssignment: 'Role Assignment',
    loginInfo: 'Login Info',
    noLoginInfo: 'No login info'
  },

  // Role Management
  role: {
    title: 'Role Management',
    name: 'Role Name',
    description: 'Description',
    status: 'Status',
    operation: 'Operation',
    addRole: 'Add Role',
    editRole: 'Edit Role',
    deleteRole: 'Delete Role',
    confirmDelete: 'Are you sure to delete this role?'
  },

  // Department Management
  department: {
    title: 'Department Management',
    name: 'Department Name',
    code: 'Department Code',
    manager: 'Manager',
    parent: 'Parent Department',
    company: 'Company',
    status: 'Status',
    operation: 'Operation',
    addDepartment: 'Add Department',
    editDepartment: 'Edit Department',
    deleteDepartment: 'Delete Department',
    confirmDelete: 'Are you sure to delete this department?',
    addChild: 'Add Child Department'
  },

  // Company Management
  company: {
    title: 'Company Management',
    name: 'Company Name',
    code: 'Company Code',
    taxNumber: 'Tax Number',
    description: 'Description',
    logo: 'Logo',
    status: 'Status',
    operation: 'Operation',
    addCompany: 'Add Company',
    editCompany: 'Edit Company',
    deleteCompany: 'Delete Company',
    confirmDelete: 'Are you sure to delete this company?'
  },

  // Data Dictionary
  dic: {
    title: 'Data Dictionary',
    name: 'Dictionary Name',
    code: 'Dictionary Code',
    value: 'Dictionary Value',
    description: 'Description',
    operation: 'Operation',
    addDic: 'Add Dictionary',
    editDic: 'Edit Dictionary',
    deleteDic: 'Delete Dictionary',
    confirmDelete: 'Are you sure to delete this dictionary?'
  },

  // System Log
  systemlog: {
    title: 'System Log',
    level: 'Log Level',
    message: 'Log Message',
    timestamp: 'Timestamp',
    traceId: 'Trace ID',
    search: 'Search Logs',
    clear: 'Clear Logs'
  },

  // Cache Management
  cache: {
    title: 'Cache Management',
    key: 'Cache Key',
    value: 'Cache Value',
    ttl: 'TTL',
    operation: 'Operation',
    clear: 'Clear Cache',
    refresh: 'Refresh',
    confirmClear: 'Are you sure to clear all caches?'
  },

  // Form Management
  form: {
    title: 'Form Management',
    name: 'Form Name',
    code: 'Form Code',
    description: 'Description',
    operation: 'Operation',
    create: 'Create Form',
    edit: 'Edit Form',
    delete: 'Delete Form',
    preview: 'Preview',
    confirmDelete: 'Are you sure to delete this form?'
  },

  // Error Pages
  error: {
    403: 'Sorry, you do not have permission to access this page',
    404: 'Sorry, the page you visited does not exist',
    backHome: 'Back to Home',
    backPrev: 'Back to Previous'
  },

  // Network Request
  request: {
    networkError: 'Network connection failed, please check network settings',
    configError: 'Response configuration error, please check network settings',
    tokenExpired: 'Token expired and refresh failed',
    redirectTip: 'Redirect Notice',
    redirectMessage: 'Redirecting to: {path}'
  }
}
