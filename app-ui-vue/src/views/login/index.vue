<template>
  <div class="login-container">
    <div class="login-box" :class="{ 'fade-in': isLoaded }">
      <div class="login-form-container">
        <!-- 语言切换按钮 -->
        <div class="login-lang-switch">
          <lang-select />
        </div>

        <div class="login-header">
          <h2 class="login-title">{{ systemTitle }}</h2>
          <p class="login-subtitle">{{ $t('login.title') }}</p>
        </div>

        <el-form
          ref="loginForm"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          auto-complete="on"
          label-position="left"
        >

          <el-form-item prop="username">
            <el-input
              ref="username"
              v-model="loginForm.username"
              :placeholder="$t('login.username')"
              name="username"
              type="text"
              tabindex="1"
              auto-complete="on"
              prefix-icon="el-icon-user"
              size="medium"
              class="login-input"
              @focus="handleInputFocus"
              @blur="handleInputBlur"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              :key="passwordType"
              ref="password"
              v-model="loginForm.password"
              :type="passwordType"
              :placeholder="$t('login.password')"
              name="password"
              tabindex="2"
              auto-complete="on"
              prefix-icon="el-icon-lock"
              size="medium"
              class="login-input"
              @keyup.enter.native="handleLogin"
              @focus="handleInputFocus"
              @blur="handleInputBlur"
            />
            <span class="show-pwd" @click="showPwd">
              <i :class="passwordType === 'password' ? 'el-icon-view' : 'el-icon-hide'" />
            </span>
          </el-form-item>

          <div class="login-options">
            <el-checkbox v-model="loginForm.rememberMe">{{ $t('login.rememberMe') || '记住我' }}</el-checkbox>
          </div>

          <el-button :loading="loading" type="primary" class="login-button" @click.native.prevent="handleLogin">
            {{ loading ? ($t('login.loggingIn') || '登录中...') : $t('login.login') }}
          </el-button>

          <div class="login-footer">
            <p>© 2025 {{ systemTitle }}. {{ $t('login.copyright') || '保留所有权利.' }}</p>
          </div>
        </el-form>
      </div>

      <div class="login-decoration">
        <div class="decoration-content">
          <div class="decoration-icon">
            <i class="el-icon-office-building float-animation" />
          </div>
          <!--          <h3>高效企业管理</h3>-->
          <!--          <p>一站式解决方案，提升企业运营效率</p>-->
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { validUsername } from '@/utils/validate'
import { getRememberMe } from '@/utils/auth'
import settings from '@/settings'
import { updateRouter } from '@/router'
import LangSelect from '@/components/LangSelect'

export default {
  name: 'Login',
  components: {
    LangSelect
  },
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!validUsername(value)) {
        callback(new Error(this.$t('login.usernameRequired')))
      } else {
        callback()
      }
    }
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error(this.$t('login.passwordRequired')))
      } else {
        callback()
      }
    }
    return {
      isLoaded: false,
      loginForm: {
        username: '',
        password: '',
        rememberMe: false
      },
      loginRules: {
        // username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        // password: [{ required: true, trigger: 'blur', validator: validatePassword }]
      },
      loading: false,
      passwordType: 'password',
      redirect: undefined
    }
  },
  computed: {
    systemTitle() {
      return settings.title || '企业管理平台'
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  mounted() {
    this.loginForm.rememberMe = getRememberMe()
    // 添加淡入动画
    this.$nextTick(() => {
      setTimeout(() => {
        this.isLoaded = true
      }, 100)
    })
  },
  methods: {
    handleInputFocus(event) {
      event.target.closest('.el-form-item').classList.add('input-focused')
    },
    handleInputBlur(event) {
      event.target.closest('.el-form-item').classList.remove('input-focused')
    },
    showPwd() {
      if (this.passwordType === 'password') {
        this.passwordType = 'text'
      } else {
        this.passwordType = 'password'
      }
      this.$nextTick(() => {
        this.$refs.password.focus()
      })
    },
    async handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true

          this.$store.dispatch('user/login', this.loginForm).then(async() => {
            // 等待一小段时间确俜token已经设置到请求拦截器中
            await new Promise(resolve => setTimeout(resolve, 100))

            try {
              // 先获取用户信息，再更新路由
              await this.$store.dispatch('user/getInfo')
              // 登录成功后更新路由数据
              await updateRouter()
              // 确保路由更新完成后才跳转
              await this.$nextTick()
              // 登录后始终跳转到dashboard
              this.$router.push({ path: '/Welcome' })
            } catch (error) {
              console.error('路由更新失败:', error)
              // 即使路由更新失败，也允许跳转到dashboard
              this.$router.push({ path: '/Welcome' })
            }
            this.loading = false
          }).catch(() => {
            this.loading = false
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  box-sizing: border-box;
}

.login-box {
  display: flex;
  width: 900px;
  height: 500px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.5s ease-out;

  &.fade-in {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-form-container {
  flex: 1;
  padding: 50px 40px;
  display: flex;
  flex-direction: column;
  position: relative;
}

.login-lang-switch {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
}

.login-header {
  margin-bottom: 40px;
  text-align: center;

  .login-title {
    font-size: 28px;
    font-weight: 600;
    color: #333;
    margin-bottom: 10px;
    animation: title-pop-in 0.8s ease-out;
  }

  .login-subtitle {
    font-size: 14px;
    color: #666;
    animation: subtitle-fade-in 1s ease-out;
  }
}

.login-form {
  flex: 1;
  display: flex;
  flex-direction: column;
}

::v-deep .el-form-item {
  margin-bottom: 24px;
  transition: transform 0.3s ease;

  &.input-focused {
    transform: translateX(5px);
  }
}

.login-input {
  ::v-deep .el-input__inner {
    height: 46px;
    line-height: 46px;
    padding-left: 40px;
    border-radius: 6px;
    border: 1px solid #dcdfe6;
    transition: all 0.3s ease;

    &:focus {
      border-color: #409eff;
      box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
    }
  }
}

::v-deep .el-input--medium .el-input__icon {
  line-height: 46px;
  width: 40px;
}

.show-pwd {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  color: #909399;
  font-size: 16px;
  transition: all 0.3s ease;

  &:hover {
    color: #409eff;
    transform: translateY(-50%) scale(1.1);
  }
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;

  ::v-deep .el-checkbox__label {
    color: #666;
    font-size: 14px;
  }

  .forgot-password {
    color: #409eff;
    font-size: 14px;
    text-decoration: none;

    &:hover {
      color: #66b1ff;
      text-decoration: underline;
    }
  }
}

.login-button {
  height: 46px;
  font-size: 16px;
  border-radius: 6px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  letter-spacing: 1px;
  margin-bottom: 30px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;

  &:hover {
    opacity: 0.9;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    transform: translateY(-2px);
  }

  &:active {
    transform: translateY(1px);
  }

  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 5px;
    height: 5px;
    background: rgba(255, 255, 255, 0.5);
    opacity: 0;
    border-radius: 100%;
    transform: scale(1, 1) translate(-50%);
    transform-origin: 50% 50%;
  }

  &:focus:not(:active)::after {
    animation: ripple 0.6s ease-out;
  }
}

.login-footer {
  text-align: center;
  margin-top: auto;

  p {
    font-size: 12px;
    color: #909399;
    margin: 0;
  }
}

.login-decoration {
  width: 40%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  padding: 30px;
  box-sizing: border-box;
}

.decoration-content {
  text-align: center;
}

.decoration-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.decoration-content h3 {
  font-size: 24px;
  font-weight: 500;
  margin-bottom: 15px;
}

.decoration-content p {
  font-size: 14px;
  line-height: 1.6;
  opacity: 0.9;
}

.float-animation {
  animation: float 3s ease-in-out infinite;
  display: inline-block;
}

@media (max-width: 768px) {
  .login-box {
    flex-direction: column;
    height: auto;
    width: 90%;
  }

  .login-decoration {
    width: 100%;
    padding: 30px 20px;
  }

  .login-form-container {
    padding: 30px 20px;
  }
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-15px);
  }
  100% {
    transform: translateY(0px);
  }
}

@keyframes title-pop-in {
  0% {
    opacity: 0;
    transform: translateY(-20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes subtitle-fade-in {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

@keyframes ripple {
  0% {
    transform: scale(0, 0);
    opacity: 0.5;
  }
  100% {
    transform: scale(50, 50);
    opacity: 0;
  }
}
</style>
