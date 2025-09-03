<template>
  <div class="url-image-preview">
    <!-- URL输入框 -->
    <div v-if="showInput" class="url-input-container">
      <el-input
        v-model="imageUrl"
        :placeholder="placeholder"
        :disabled="disabled"
        class="url-input"
        type="text"
        @input="handleInput"
        @blur="handleBlur"
      >
        <el-button
          v-if="showClearButton"
          slot="append"
          :disabled="disabled || !imageUrl"
          size="small"
          @click="clearUrl"
        >
          {{ clearButtonText }}
        </el-button>
      </el-input>
    </div>

    <!-- 图片预览区域 -->
    <div class="image-preview-container">
      <!-- 加载状态和错误状态由 el-image 处理 -->
      <el-image
        v-if="imageUrl"
        ref="previewImage"
        :src="imageUrl"
        :alt="altText"
        :style="imageStyle"
        class="preview-image"
        fit="contain"
        :lazy="false"
        @load="handleImageLoad"
        @error="handleImageError"
      >
        <div slot="placeholder" class="loading-placeholder">
          <div class="spinner" />
          <p>{{ loadingText }}</p>
        </div>
        <div slot="error" class="error-placeholder">
          <div class="error-icon">⚠️</div>
          <p>{{ errorText }}</p>
          <el-button v-if="showRetryButton" class="retry-button" size="small" @click="retryLoad">
            {{ retryButtonText }}
          </el-button>
        </div>
      </el-image>

      <!-- 空状态 -->
      <div v-else class="empty-placeholder">
        <div class="empty-icon">{{ emptyIcon }}</div>
        <p>{{ emptyText }}</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UrlImage',
  props: {
    // 初始URL
    value: {
      type: String,
      default: ''
    },
    // 占位符文本
    placeholder: {
      type: String,
      default: '请输入图片URL...'
    },
    // 是否显示输入框
    showInput: {
      type: Boolean,
      default: true
    },
    // 是否禁用组件
    disabled: {
      type: Boolean,
      default: false
    },
    // 图片的alt属性
    altText: {
      type: String,
      default: '预览图片'
    },
    // 是否显示清除按钮
    showClearButton: {
      type: Boolean,
      default: true
    },
    // 清除按钮文字
    clearButtonText: {
      type: String,
      default: '清除'
    },
    // 是否显示重试按钮
    showRetryButton: {
      type: Boolean,
      default: true
    },
    // 重试按钮文字
    retryButtonText: {
      type: String,
      default: '重试'
    },
    // 加载中文本
    loadingText: {
      type: String,
      default: '加载中...'
    },
    // 错误文本
    errorText: {
      type: String,
      default: '图片加载失败'
    },
    // 空状态文本
    emptyText: {
      type: String,
      default: '暂无图片'
    },
    // 空状态图标
    emptyIcon: {
      type: String,
      default: '🖼️'
    },
    // 图片样式
    imageStyle: {
      type: Object,
      default: () => ({})
    },
    // 是否启用懒加载（已移除该功能）
    lazy: {
      type: Boolean,
      default: false
    },
    // 验证URL的正则表达式
    urlPattern: {
      type: RegExp,
      default: () => /\.(jpeg|jpg|gif|png|webp|bmp|svg)$/i
    }
  },
  data() {
    return {
      imageUrl: this.value,
      loading: false,
      error: false
    }
  },
  watch: {
    value(newVal) {
      if (newVal !== this.imageUrl) {
        this.imageUrl = newVal
      }
    }
  },
  mounted() {
    if (this.imageUrl) {
      // 当组件挂载时，如果有图片URL，则不需要额外处理，让el-image处理加载
    }
  },
  methods: {
    handleInput() {
      this.$emit('input', this.imageUrl)
      this.$emit('change', this.imageUrl)
    },
    handleBlur() {
      this.$emit('blur', this.imageUrl)
    },
    handleImageLoad() {
      this.error = false
      this.loading = false
      this.$emit('load', this.imageUrl)
    },
    handleImageError(err) {
      this.error = true
      this.loading = false
      this.$emit('error', err || new Error('Image load failed'))
    },
    clearUrl() {
      this.imageUrl = ''
      this.error = false
      this.loading = false
      this.$emit('input', this.imageUrl)
      this.$emit('change', this.imageUrl)
      this.$emit('clear')
    },
    retryLoad() {
      // 通过重新设置imageUrl触发重新加载
      const url = this.imageUrl
      this.imageUrl = ''
      this.$nextTick(() => {
        this.imageUrl = url
      })
    }
  }
}
</script>

<style scoped>
.url-image-preview {
  font-family: Arial, sans-serif;
  width: 100%;
}

.url-input-container {
  margin-bottom: 10px;
}

.image-preview-container {
  position: relative;
  min-height: 200px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.loading-placeholder,
.error-placeholder,
.empty-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  text-align: center;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 4px solid rgba(64, 158, 255, 0.2);
  border-top: 4px solid #409eff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 10px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.error-icon,
.empty-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.retry-button {
  margin-top: 10px;
}

.preview-image {
  max-width: 100%;
  max-height: 400px;
}

.preview-image ::v-deep .el-image__inner {
  max-height: 400px;
}
</style>
