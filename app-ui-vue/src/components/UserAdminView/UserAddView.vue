<template>
  <div class="user-add-view">
    <DynamicForm
      ref="dynamicForm"
      :config="formConfig"
      :value="userForm"
      :custom-components="{ urlImage: UrlImage }"
      @input="handleFormChange"
      @submit="handleSubmit"
    >
      <template #form-item-confirmPassword="{ field, value, setValue }">
        <el-form-item :label="field.label" :prop="field.prop">
          <el-input
            :value="value"
            type="password"
            :placeholder="field.placeholder"
            show-password
            size="medium"
            prefix-icon="el-icon-lock"
            @input="setValue"
          ></el-input>
        </el-form-item>
      </template>

      <template #form-item-avatar="{ field, value, setValue }">
        <el-form-item :label="field.label" :prop="field.prop">
          <url-image :value="value" @input="setValue"></url-image>
        </el-form-item>
      </template>

      <template #actions="{ submit, reset }">
        <div class="form-actions">
          <el-button @click="handleCancel" size="medium">取消</el-button>
          <el-button type="primary" @click="submit" size="medium" icon="el-icon-check">保存用户</el-button>
        </div>
      </template>
    </DynamicForm>
  </div>
</template>

<script>
import DynamicForm from "@/components/DynamicForm";
import UrlImage from "@/components/UrlImage";
import {saveUser} from '@/api/user'

export default {
  name: 'UserAddView',
  components: {
    DynamicForm,
    UrlImage
  },
  data() {
    // 确认密码验证规则
    const validateConfirmPassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== this.userForm.password) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }

    return {
      userForm: {
        userName: '',
        password: '',
        confirmPassword: '',
        nickName: '',
        remark: '',
        avatar: ''
      },
      formConfig: {
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
  },
  computed: {
    // 表单验证规则
    userRules() {
      // 确认密码验证规则
      const validateConfirmPassword = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请再次输入密码'))
        } else if (value !== this.userForm.password) {
          callback(new Error('两次输入密码不一致!'))
        } else {
          callback()
        }
      }

      return {
        userName: [
          {required: true, message: '请输入用户名', trigger: 'blur'},
          {min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur'}
        ],
        password: [
          {required: true, message: '请输入密码', trigger: 'blur'},
          {min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur'}
        ],
        confirmPassword: [
          {required: true, validator: validateConfirmPassword, trigger: 'blur'}
        ],
        nickName: [
          {required: true, message: '请输入昵称', trigger: 'blur'},
          {min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur'}
        ]
      }
    }
  },
  methods: {
    /**
     * 处理表单变更
     */
    handleFormChange(value) {
      this.userForm = {...value};
    },

    /**
     * 提交表单
     */
    async handleSubmit(formData) {
      try {
        // 手动验证表单
        const isValid = await this.validateForm();
        if (!isValid) {
          this.$message.warning('请填写正确的表单信息');
          return;
        }

        // 构造用户数据
        const userData = {
          userName: formData.userName,
          password: formData.password,
          nickName: formData.nickName,
          remark: formData.remark,
          avatar: formData.avatar
        }

        const res = await saveUser(userData)
        this.$emit('success', res)
      } catch (error) {
        this.$emit('error', error)
      }
    },

    /**
     * 验证表单
     */
    validateForm() {
      return new Promise((resolve) => {
        this.$nextTick(() => {
          // 创建临时表单引用以进行验证
          const tempForm = this.$refs.dynamicForm.$refs.form;
          if (tempForm) {
            // 设置验证规则
            tempForm.rules = this.userRules;
            tempForm.validate((valid) => {
              resolve(valid);
            });
          } else {
            resolve(false);
          }
        });
      });
    },

    /**
     * 取消操作
     */
    handleCancel() {
      this.$emit('cancel')
    }
  }
}
</script>

<style lang="scss" scoped>
.user-add-view {
  padding: 10px 20px;

  .form-section-header {
    display: flex;
    align-items: center;
    margin-bottom: 15px;

    i {
      font-size: 16px;
      color: #409EFF;
      margin-right: 8px;
    }

    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #606266;
    }
  }

  .form-section {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 10px;
    }

    .section-header {
      display: flex;
      align-items: center;
      margin-bottom: 15px;
      padding-bottom: 8px;
      border-bottom: 1px solid #ebeef5;

      i {
        font-size: 16px;
        color: #409EFF;
        margin-right: 8px;
      }

      .section-title {
        font-size: 15px;
        font-weight: 600;
        color: #606266;
      }
    }
  }

  .form-actions {
    text-align: right;
    padding-top: 15px;
    border-top: 1px solid #ebeef5;
  }

  .avatar-uploader {
    ::v-deep .el-upload {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: border-color 0.2s ease;
      width: 120px;
      height: 120px;

      &:hover {
        border-color: #409EFF;
      }
    }

    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 120px;
      height: 120px;
      line-height: 120px;
      text-align: center;
    }

    .avatar {
      width: 120px;
      height: 120px;
      display: block;
    }
  }

  ::v-deep .el-form-item__label {
    font-weight: 500;
  }

  ::v-deep .el-form-item {
    margin-bottom: 18px;
  }
}
</style>
