<template>
  <div class="user-add-view">
    <el-form
      :model="userForm"
      :rules="userRules"
      ref="userForm"
      label-width="100px"
      class="user-form"
    >
      <el-row :gutter="30">
        <el-col :span="24">
          <div class="form-section">
            <div class="section-title">基本信息</div>
            <el-row :gutter="30">
              <el-col :span="12">
                <el-form-item label="用户名" prop="userName">
                  <el-input
                    v-model="userForm.userName"
                    placeholder="请输入用户名"
                    size="medium"
                  ></el-input>
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="昵称" prop="nickName">
                  <el-input
                    v-model="userForm.nickName"
                    placeholder="请输入昵称"
                    size="medium"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="30">
              <el-col :span="12">
                <el-form-item label="密码" prop="password">
                  <el-input
                    v-model="userForm.password"
                    type="password"
                    placeholder="请输入密码"
                    show-password
                    size="medium"
                  ></el-input>
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="userForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入密码"
                    show-password
                    size="medium"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="30">
        <el-col :span="24">
          <div class="form-section">
            <div class="section-title">其他信息</div>
            <el-row :gutter="30">
              <el-col :span="12">
                <el-form-item label="状态" prop="flag">
                  <el-select
                    v-model="userForm.flag"
                    placeholder="请选择用户状态"
                    style="width: 100%"
                    size="medium"
                  >
                    <el-option label="正常" :value="1"></el-option>
                    <el-option label="禁用" :value="0"></el-option>
                    <el-option label="锁定" :value="2"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="备注" prop="remark">
                  <el-input
                    v-model="userForm.remark"
                    placeholder="请输入备注信息"
                    size="medium"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="30">
              <el-col :span="12">
                <el-form-item label="头像">
                  <url-image v-model="userForm.avatar"></url-image>
                  
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="24" class="form-actions">
          <el-button @click="handleCancel" size="medium">取消</el-button>
          <el-button type="primary" @click="handleSubmit" size="medium">保存</el-button>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>
<script>
import request from '@/utils/request'
import UrlImage from "@/components/UrlImage/index.vue";

export default {
  name: 'UserAddView',
  components: {UrlImage},
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
        flag: 1, // 默认正常状态
        avatar: ''
      },
      userRules: {
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
        ],
        flag: [
          {required: true, message: '请选择用户状态', trigger: 'change'}
        ]
      }
    }
  },
  methods: {
    /**
     * 处理头像URL变化
     */
    handleAvatarChange(value) {
      // 通过v-model自动更新userForm.avatar，这里可以添加额外的处理逻辑
      console.log('头像URL已更新:', value)
      this.userForm.avatar = value
    },

    /**
     * 提交表单
     */
    handleSubmit() {
      this.$refs.userForm.validate(async (valid) => {
        if (valid) {
          try {
            // 构造用户数据
            const userData = {
              userName: this.userForm.userName,
              password: this.userForm.password,
              nickName: this.userForm.nickName,
              remark: this.userForm.remark,
              flag: this.userForm.flag,
              avatar: this.userForm.avatar
            }

            // 调用后端保存接口
            const response = await request({
              url: '/private/admin/user/save',
              method: 'post',
              data: userData
            })

            if (response.code === 200) {
              this.$message.success('用户创建成功')
              this.$emit('success')
            } else {
              this.$message.error(response.message || '用户创建失败')
            }
          } catch (error) {
            console.error('创建用户失败:', error)
            this.$message.error('创建用户失败')
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
.user-add-view {
  padding: 20px 30px;

  .form-section {
    margin-bottom: 30px;

    .section-title {
      font-size: 16px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 20px;
      padding-left: 10px;
      border-left: 4px solid #409EFF;
    }
  }

  .user-form {
    .form-actions {
      text-align: right;
      margin-top: 20px;
      padding-right: 10px;
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
}
</style>
