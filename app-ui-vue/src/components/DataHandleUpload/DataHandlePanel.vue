<template>
  <div class="dhu-panel">
    <!-- 页面说明区 -->
    <el-card v-if="showHeader" shadow="hover" class="dhu-card dhu-fade-up">
      <div slot="header" class="dhu-header">
        <div class="dhu-title-area">
          <span class="dhu-title">{{ $t('dataHandle.title') }}</span>
          <span class="dhu-subtitle">{{ $t('dataHandle.subtitle') }}</span>
        </div>
        <el-button icon="el-icon-refresh" size="small" class="dhu-refresh" @click="fetchHandlers">
          {{ $t('dataHandle.refresh') }}
        </el-button>
      </div>

      <!-- 表单区 -->
      <el-form label-position="top" class="dhu-form">
        <div class="dhu-field">
          <label class="dhu-label">
            {{ $t('dataHandle.handlerLabel') }}
            <span class="dhu-required">*</span>
          </label>
          <el-select
            v-model="form.handlerId"
            filterable
            allow-create
            default-first-option
            clearable
            :loading="handlersLoading"
            :placeholder="$t('dataHandle.handlerPlaceholder')"
            class="handler-select"
            @change="onHandlerChange"
            @clear="resetValidation"
          >
            <el-option
              v-for="item in handlers"
              :key="item.id"
              :label="item.name ? `${item.name}（${item.id}）` : item.id"
              :value="item.id"
            >
              <span class="option-name">{{ item.name || item.id }}</span>
              <span class="option-id">{{ item.id }}</span>
            </el-option>
          </el-select>
          <!-- 自定义输入实时校验状态 -->
          <transition name="dhu-fade">
            <div v-if="isCustomHandler" class="validate-pill" :class="validateState">
              <template v-if="validateState === 'validating'">
                <i class="el-icon-loading" /> {{ $t('dataHandle.validating') }}
              </template>
              <template v-else-if="validateState === 'valid'">
                <i class="el-icon-circle-check" />
                {{ validateSource === 'remote' ? $t('dataHandle.validRemote') : $t('dataHandle.validLocal') }}
              </template>
              <template v-else-if="validateState === 'invalid'">
                <i class="el-icon-circle-close" /> {{ $t('dataHandle.invalid') }}
              </template>
            </div>
          </transition>
        </div>

        <div class="dhu-field">
          <label class="dhu-label">
            {{ $t('dataHandle.fileLabel') }}
            <span class="dhu-required">*</span>
          </label>
          <el-upload
            ref="upload"
            drag
            action="#"
            accept=".csv"
            :auto-upload="false"
            :limit="1"
            :disabled="submitting"
            :on-change="onFileChange"
            :on-remove="() => (form.file = null)"
            :on-exceed="handleExceed"
            class="csv-upload"
          >
            <div class="upload-inner">
              <i class="el-icon-upload upload-icon" />
              <div class="el-upload__text upload-text">
                {{ $t('dataHandle.dragText') }}<em>{{ $t('dataHandle.clickUpload') }}</em>
              </div>
            </div>
            <div slot="tip" class="el-upload__tip upload-tip">{{ $t('dataHandle.uploadTip') }}</div>
          </el-upload>
        </div>

        <div class="dhu-actions">
          <el-button
            type="primary"
            icon="el-icon-upload2"
            :loading="submitting"
            class="dhu-submit"
            @click="handleSubmit"
          >{{ $t('dataHandle.submit') }}</el-button>
          <el-button icon="el-icon-refresh-left" class="dhu-reset" @click="handleReset">
            {{ $t('dataHandle.reset') }}
          </el-button>
        </div>
      </el-form>

      <transition name="dhu-fade">
        <el-progress
          v-if="submitting"
          :percentage="progress"
          :stroke-width="6"
          class="upload-progress"
        />
      </transition>
    </el-card>

    <!-- 结果展示区 -->
    <transition name="dhu-rise">
      <el-card v-if="result" shadow="hover" class="dhu-card result-card dhu-fade-up">
        <div slot="header" class="dhu-header">
          <div class="dhu-title-area">
            <span class="dhu-title result-title">{{ $t('dataHandle.resultTitle') }}</span>
            <span v-if="result.tableName" class="dhu-subtitle">{{ result.tableName }}</span>
          </div>
        </div>
        <result-data-table :table-data="result" />
      </el-card>
    </transition>
  </div>
</template>

<script>
import {handleCsv, listHandlers, validateHandler} from '@/api/data-handle'
import ResultDataTable from './ResultDataTable'
import {isCsvFile, VALIDATE_DEBOUNCE_MS} from './shared'

export default {
  name: 'DataHandlePanel',
  components: {
    ResultDataTable
  },
  props: {
    /**
     * 是否渲染带标题的说明区（嵌入宿主页面时可关闭）
     */
    showHeader: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      handlers: [],
      handlersLoading: false,
      form: {
        handlerId: '',
        file: null
      },
      validateState: '',
      validateSource: '',
      validateTimer: null,
      validateSeq: 0,
      submitting: false,
      progress: 0,
      result: null
    }
  },
  computed: {
    isCustomHandler() {
      return this.form.handlerId !== '' &&
        !this.handlers.some(item => item.id === this.form.handlerId)
    }
  },
  created() {
    this.fetchHandlers()
  },
  beforeDestroy() {
    if (this.validateTimer) {
      clearTimeout(this.validateTimer)
    }
  },
  methods: {
    fetchHandlers() {
      this.handlersLoading = true
      listHandlers().then(res => {
        this.handlers = res.data || []
      }).finally(() => {
        this.handlersLoading = false
      })
    },
    onHandlerChange(value) {
      this.resetValidation()
      if (!value) {
        return
      }
      if (this.handlers.some(item => item.id === value)) {
        return
      }
      // 自定义输入：防抖后校验
      if (this.validateTimer) {
        clearTimeout(this.validateTimer)
      }
      this.validateState = 'validating'
      this.validateTimer = setTimeout(() => {
        this.doValidate(value)
      }, VALIDATE_DEBOUNCE_MS)
    },
    doValidate(handlerId) {
      const seq = ++this.validateSeq
      validateHandler(handlerId).then(res => {
        if (seq !== this.validateSeq) {
          return
        }
        const data = res.data || {}
        if (data.available) {
          this.validateState = 'valid'
          this.validateSource = data.source
        } else {
          this.validateState = 'invalid'
        }
      }).catch(() => {
        if (seq === this.validateSeq) {
          this.validateState = ''
        }
      })
    },
    resetValidation() {
      this.validateSeq++
      if (this.validateTimer) {
        clearTimeout(this.validateTimer)
        this.validateTimer = null
      }
      this.validateState = ''
      this.validateSource = ''
    },
    onFileChange(file, fileList) {
      if (!isCsvFile(file.name)) {
        this.$message.error(this.$t('dataHandle.onlyCsv'))
        fileList.pop()
        this.form.file = null
        return
      }
      this.form.file = file.raw
    },
    handleExceed(files) {
      this.$refs.upload.clearFiles()
      this.$refs.upload.handleStart(files[0])
      this.$message.warning(this.$t('dataHandle.fileExceed'))
    },
    ensureValidated() {
      if (!this.isCustomHandler || this.validateState === 'invalid') {
        return Promise.resolve(this.validateState !== 'invalid')
      }
      if (this.validateState === 'valid') {
        return Promise.resolve(true)
      }
      return validateHandler(this.form.handlerId).then(res => {
        const data = (res && res.data) || {}
        if (data.available) {
          this.validateState = 'valid'
          this.validateSource = data.source
          return true
        }
        this.validateState = 'invalid'
        this.$message.error(this.$t('dataHandle.invalid'))
        return false
      }).catch(() => false)
    },
    handleSubmit() {
      if (!this.form.handlerId) {
        this.$message.warning(this.$t('dataHandle.handlerRequired'))
        return
      }
      if (!this.form.file) {
        this.$message.warning(this.$t('dataHandle.fileRequired'))
        return
      }
      this.ensureValidated().then(ok => {
        if (!ok) {
          return
        }
        this.submitting = true
        this.progress = 0
        handleCsv(this.form.handlerId, this.form.file, p => {
          this.progress = p
        }).then(res => {
          this.result = res.data
          this.$notify({
            title: this.$t('common.tip'),
            message: this.$t('dataHandle.submitSuccess', { count: (res.data.rows || []).length }),
            type: 'success',
            duration: 3000
          })
          this.$nextTick(() => {
            const target = this.$el && this.$el.querySelector('.result-card')
            if (target) {
              target.scrollIntoView({ behavior: 'smooth' })
            }
          })
        }).finally(() => {
          this.submitting = false
        })
      })
    },
    handleReset() {
      this.resetValidation()
      this.form.handlerId = ''
      this.form.file = null
      this.result = null
      this.progress = 0
      if (this.$refs.upload) {
        this.$refs.upload.clearFiles()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
$dhu-ink: #2c3e50;
$dhu-text: #34495e;
$dhu-muted: #94a3b3;
$dhu-primary: #4a7de0;
$dhu-line: #e6ecf3;

.dhu-panel {
  padding: 4px;
}

.dhu-card {
  position: relative;
  overflow: hidden;
  border: 1px solid $dhu-line;
  border-radius: 12px;
  box-shadow: 0 6px 24px rgba(31, 45, 61, 0.06);
  transition: box-shadow 0.25s ease, transform 0.25s ease;

  &:hover {
    box-shadow: 0 10px 32px rgba(31, 45, 61, 0.1);
  }

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, #4a7de0 0%, #7fb1f5 45%, rgba(127, 177, 245, 0) 100%);
  }

  ::v-deep .el-card__header {
    padding: 18px 24px 16px;
    border-bottom: 1px solid $dhu-line;
  }

  ::v-deep .el-card__body {
    padding: 24px;
  }
}

.dhu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dhu-title-area {
  display: flex;
  flex-direction: column;
}

.dhu-title {
  font-size: 19px;
  font-weight: 600;
  letter-spacing: 0.2px;
  color: $dhu-ink;
}

.dhu-subtitle {
  margin-top: 5px;
  font-size: 13px;
  color: $dhu-muted;
}

.dhu-refresh {
  border-radius: 8px;
}

.dhu-form {
  max-width: 720px;
}

.dhu-field {
  margin-bottom: 22px;
}

.dhu-label {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 500;
  color: $dhu-text;

  .dhu-required {
    margin-left: 2px;
    color: #f56c6c;
  }
}

.handler-select {
  width: 100%;
  max-width: 460px;

  ::v-deep .el-input__inner {
    border-radius: 8px;
  }
}

.option-name {
  float: left;
  margin-right: 12px;
}

.option-id {
  float: right;
  font-size: 12px;
  color: $dhu-muted;
}

.validate-pill {
  display: inline-flex;
  align-items: center;
  margin-top: 8px;
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 20px;

  &.validating {
    color: #8a6d1a;
    background: rgba(230, 162, 60, 0.12);
  }

  &.valid {
    color: #2e7d43;
    background: rgba(103, 194, 58, 0.14);
  }

  &.invalid {
    color: #c45656;
    background: rgba(245, 108, 108, 0.12);
  }
}

.csv-upload {
  width: 100%;

  ::v-deep .el-upload,
  ::v-deep .el-upload-dragger {
    width: 100%;
  }

  ::v-deep .el-upload-dragger {
    height: auto;
    padding: 30px 20px;
    border: 1.5px dashed #c9d6e4;
    border-radius: 10px;
    background: linear-gradient(180deg, #fbfdff 0%, #f5f9fd 100%);
    transition: border-color 0.25s ease, box-shadow 0.25s ease, transform 0.25s ease;

    &:hover {
      border-color: #8db4f0;
      box-shadow: 0 4px 18px rgba(74, 125, 224, 0.12);
      transform: translateY(-1px);

      .upload-icon {
        color: $dhu-primary;
        transform: translateY(-3px);
      }
    }
  }
}

.upload-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.upload-icon {
  font-size: 44px;
  color: #b6c6da;
  transition: color 0.25s ease, transform 0.25s ease;
}

.upload-text {
  margin-top: 6px;
  color: $dhu-muted;

  em {
    color: $dhu-primary;
    font-style: normal;
  }
}

.upload-tip {
  margin-top: 6px;
  font-size: 12px;
  color: $dhu-muted;
}

.dhu-actions {
  display: flex;
  gap: 4px;

  .dhu-submit {
    border-radius: 8px;
    box-shadow: 0 4px 14px rgba(74, 125, 224, 0.25);
    transition: transform 0.2s ease, box-shadow 0.2s ease;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 6px 18px rgba(74, 125, 224, 0.32);
    }
  }

  .dhu-reset {
    border-radius: 8px;
  }
}

.upload-progress {
  margin-top: 6px;

  ::v-deep .el-progress-bar__outer {
    border-radius: 999px;
  }

  ::v-deep .el-progress-bar__inner {
    border-radius: 999px;
    background: linear-gradient(90deg, #4a7de0 0%, #7fb1f5 100%);
    transition: width 0.2s ease;
  }
}

.result-card {
  margin-top: 18px;
}

.result-title {
  font-size: 17px;
}

.dhu-fade-up {
  animation: dhu-fade-up 0.45s ease both;
}

.dhu-fade-enter-active,
.dhu-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.dhu-fade-enter,
.dhu-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.dhu-rise-enter-active {
  transition: opacity 0.35s ease, transform 0.35s ease;
}

.dhu-rise-enter {
  opacity: 0;
  transform: translateY(14px);
}

@keyframes dhu-fade-up {
  from {
    opacity: 0;
    transform: translateY(12px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
