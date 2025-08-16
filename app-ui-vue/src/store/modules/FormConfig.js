// 表单配置状态管理模块
const state = {
  // 所有表单配置
  formConfigs: {
    userAddForm: {
      expandable: false,
      fields: [
        {
          prop: 'userName',
          label: '用户名',
          type: 'input',
          placeholder: '请输入用户名',
          props: {
            size: 'medium',
            prefixIcon: 'el-icon-user'
          }
        },
        {
          prop: 'nickName',
          label: '昵称',
          type: 'input',
          placeholder: '请输入昵称',
          props: {
            size: 'medium',
            prefixIcon: 'el-icon-star-off'
          }
        },
        {
          prop: 'password',
          label: '密码',
          type: 'input',
          placeholder: '请输入密码',
          props: {
            type: 'password',
            showPassword: true,
            size: 'medium',
            prefixIcon: 'el-icon-lock'
          }
        },
        {
          prop: 'confirmPassword',
          label: '确认密码',
          type: 'custom',
          placeholder: '请再次输入密码'
        },
        {
          prop: 'remark',
          label: '备注',
          type: 'input',
          placeholder: '请输入备注信息',
          props: {
            type: 'textarea',
            rows: 2,
            size: 'medium'
          }
        },
        {
          prop: 'avatar',
          label: '头像',
          type: 'urlImage'
        }
      ],
      span: 24,
      labelWidth: '80px'
    }
  }
}

const mutations = {
  // 设置表单配置
  SET_FORM_CONFIG(state, {configName, config}) {
    state.formConfigs[configName] = config
  },

  // 更新表单配置
  UPDATE_FORM_CONFIG(state, {configName, config}) {
    if (state.formConfigs[configName]) {
      state.formConfigs[configName] = {
        ...state.formConfigs[configName],
        ...config
      }
    }
  }
}

const actions = {
  // 设置表单配置
  setFormConfig({commit}, {configName, config}) {
    commit('SET_FORM_CONFIG', {configName, config})
  },

  // 更新表单配置
  updateFormConfig({commit}, {configName, config}) {
    commit('UPDATE_FORM_CONFIG', {configName, config})
  }
}

const getters = {
  // 获取指定名称的表单配置
  formConfig: (state) => (configName) => {
    return state.formConfigs[configName] || null
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
