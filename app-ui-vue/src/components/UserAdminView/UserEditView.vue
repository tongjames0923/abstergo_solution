<template>
  <div class="user-edit-view">
    <el-form
      :model="userForm"
      :rules="userRules"
      ref="userForm"
      label-width="80px"
      class="user-form"
    >
      <div class="form-section">
        <div class="section-header">
          <i class="el-icon-user"></i>
          <span class="section-title">基本信息</span>
        </div>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="用户名" prop="userName">
              <el-input
                v-model="userForm.userName"
                placeholder="请输入用户名"
                size="medium"
                prefix-icon="el-icon-user"
                :disabled="isEditMode"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="昵称" prop="nickName">
              <el-input
                v-model="userForm.nickName"
                placeholder="请输入昵称"
                size="medium"
                prefix-icon="el-icon-star-off"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="!isEditMode" :gutter="20">
          <el-col :span="24">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="userForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                size="medium"
                prefix-icon="el-icon-lock"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="!isEditMode" :gutter="20">
          <el-col :span="24">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="userForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                show-password
                size="medium"
                prefix-icon="el-icon-lock"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div class="form-section">
        <div class="section-header">
          <i class="el-icon-picture-outline"></i>
          <span class="section-title">附加信息</span>
        </div>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="userForm.remark"
                placeholder="请输入备注信息"
                type="textarea"
                :rows="2"
                size="medium"
              ></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="头像">
              <url-image v-model="userForm.avatar"></url-image>
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div class="form-actions">
        <el-button @click="handleCancel" size="medium">取消</el-button>
        <el-button type="primary" @click="handleSubmit" size="medium" icon="el-icon-check">保存用户</el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import UrlImage from "@/components/UrlImage/index.vue";

import {saveUser} from "@/api/admin_user";

export default {
  name: 'UserEditView',
  components: {UrlImage},
  props: {
    // 编辑模式下的用户数据
    userData: {
      type: Object,
      default: () => ({
        id: undefined,
        userName: '',
        password: '',
        confirmPassword: '',
        nickName: '',
        remark: '',
        avatar: ''
      })
    }
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
      userForm: {...this.userData},
      userRules: {
        userName: [
          {required: true, message: '请输入用户名', trigger: 'blur'},
          {min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur'}
        ],
        password: [
          {required: !this.isEditMode, message: '请输入密码', trigger: 'blur'},
          {min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur'}
        ],
        confirmPassword: [
          {required: !this.isEditMode, validator: validateConfirmPassword, trigger: 'blur'}
        ],
        nickName: [
          {required: true, message: '请输入昵称', trigger: 'blur'},
          {min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur'}
        ]
      }
    }
  },
  computed: {
    // 是否为编辑模式
    isEditMode() {
      return !!this.userForm.id
    }
  },
  watch: {
    userData: {
      handler(newVal) {
        this.userForm = {...newVal}
      },
      deep: true
    }
  },
  methods: {
    /**
     * 提交表单
     */
    handleSubmit() {
      this.$refs.userForm.validate(async (valid) => {
        if (valid) {
          try {
            // 构造用户数据
            const userData = {
              id: this.userForm.id,
              userName: this.userForm.userName,
              nickName: this.userForm.nickName,
              remark: this.userForm.remark,
              avatar: this.userForm.avatar
            }

            // 如果是新增模式，添加密码字段
            if (!this.isEditMode) {
              userData.password = this.userForm.password
            }
            const res = await saveUser(userData)
            this.$emit('success', res)
          } catch (error) {
            this.$emit('error', error)
          }
        } else {
          console.log('表单验证失败')
          this.$message.warning('请填写正确的表单信息')
          return false
        }
      })
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
.user-edit-view {
  padding: 10px 20px;

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

  .user-form {
    .form-actions {
      text-align: right;
      padding-top: 15px;
      border-top: 1px solid #ebeef5;
    }
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

</file_content>
