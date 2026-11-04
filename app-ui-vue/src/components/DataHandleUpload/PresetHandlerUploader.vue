<template>
  <div class="dhu-preset">
    <el-upload
      ref="upload"
      drag
      action="#"
      :accept="accept"
      :limit="limit"
      :auto-upload="false"
      :disabled="disabled || submitting"
      :on-change="onFileChange"
      :on-remove="onFileRemove"
      :on-exceed="handleExceed"
      class="csv-upload"
    >
      <div class="upload-inner">
        <i class="el-icon-upload upload-icon" />
        <div class="el-upload__text upload-text">
          {{ $t('dataHandle.dragText') }}<em>{{ $t('dataHandle.clickUpload') }}</em>
        </div>
      </div>
      <div slot="tip" class="el-upload__tip upload-tip">{{ uploadTipText }}</div>
    </el-upload>

    <div v-if="showActions" class="dhu-actions">
      <el-button
        type="primary"
        icon="el-icon-upload2"
        :loading="submitting"
        :disabled="disabled"
        class="dhu-submit"
        @click="submit"
      >{{ $t('dataHandle.submit') }}</el-button>
      <el-button icon="el-icon-refresh-left" class="dhu-reset" @click="reset">
        {{ $t('dataHandle.reset') }}
      </el-button>
    </div>

    <transition name="dhu-fade">
      <el-progress
        v-if="showProgress && submitting"
        :percentage="progress"
        :stroke-width="6"
        class="upload-progress"
      />
    </transition>
  </div>
</template>

<script>
import {handleCsv} from '@/api/data-handle'
import {isCsvFile} from './shared'

/**
 * 预设处理器上传适配组件
 *
 * 仅渲染文件上传界面即可独立运行：通过 handlerId 配置预设数据处理器，
 * 直接调用 CSV 上传解析接口，不依赖基础组件 DataHandlePanel。
 * 支持回调函数与事件双通道通知，便于在不同宿主场景下复用。
 */
export default {
  name: 'PresetHandlerUploader',
  props: {
    /**
     * 预设的数据处理器ID（必填）
     */
    handlerId: {
      type: String,
      required: true
    },
    /**
     * 回调函数配置：{ onProgress, onSuccess, onError }
     */
    callbacks: {
      type: Object,
      default() {
        return {}
      }
    },
    /**
     * 允许的文件类型
     */
    accept: {
      type: String,
      default: '.csv'
    },
    /**
     * 最大文件数量
     */
    limit: {
      type: Number,
      default: 1
    },
    /**
     * 是否禁用
     */
    disabled: {
      type: Boolean,
      default: false
    },
    /**
     * 是否渲染内置提交/重置按钮
     */
    showActions: {
      type: Boolean,
      default: true
    },
    /**
     * 是否渲染上传进度条
     */
    showProgress: {
      type: Boolean,
      default: true
    },
    /**
     * 选择文件后是否自动提交上传
     */
    autoSubmit: {
      type: Boolean,
      default: false
    },
    /**
     * 上传提示文案（缺省使用 i18n 默认提示）
     */
    uploadTip: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      file: null,
      submitting: false,
      progress: 0
    }
  },
  computed: {
    uploadTipText() {
      return this.uploadTip || this.$t('dataHandle.uploadTip')
    }
  },
  beforeDestroy() {
    this.file = null
  },
  methods: {
    onFileChange(file, fileList) {
      if (!isCsvFile(file.name)) {
        this.$message.error(this.$t('dataHandle.onlyCsv'))
        fileList.pop()
        this.file = null
        return
      }
      this.file = file.raw
      this.notifyFileChange(file.raw)
      if (this.autoSubmit) {
        this.submit()
      }
    },
    onFileRemove() {
      this.file = null
      this.notifyFileChange(null)
    },
    handleExceed(files) {
      this.$refs.upload.clearFiles()
      this.$refs.upload.handleStart(files[0])
      this.$message.warning(this.$t('dataHandle.fileExceed'))
    },
    notifyFileChange(file) {
      this.$emit('file-change', file)
      if (typeof this.callbacks.onFileChange === 'function') {
        this.callbacks.onFileChange(file)
      }
    },
    /**
     * 提交上传（供外部通过 ref 调用）
     */
    submit() {
      if (this.submitting || this.disabled) {
        return
      }
      if (!this.handlerId) {
        const err = new Error(this.$t('dataHandle.handlerRequired'))
        this.notifyError(err)
        return
      }
      if (!this.file) {
        this.$message.warning(this.$t('dataHandle.fileRequired'))
        return
      }
      this.submitting = true
      this.progress = 0
      handleCsv(this.handlerId, this.file, p => {
        this.progress = p
        this.$emit('progress', p)
        if (typeof this.callbacks.onProgress === 'function') {
          this.callbacks.onProgress(p)
        }
      }).then(res => {
        const data = res.data
        this.$emit('success', data)
        if (typeof this.callbacks.onSuccess === 'function') {
          this.callbacks.onSuccess(data)
        }
      }).catch(err => {
        this.notifyError(err)
      }).finally(() => {
        this.submitting = false
      })
    },
    /**
     * 重置上传状态（供外部通过 ref 调用）
     */
    reset() {
      this.file = null
      this.progress = 0
      if (this.$refs.upload) {
        this.$refs.upload.clearFiles()
      }
      this.notifyFileChange(null)
    },
    notifyError(err) {
      this.$emit('error', err)
      if (typeof this.callbacks.onError === 'function') {
        this.callbacks.onError(err)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
$dhu-muted: #94a3b3;
$dhu-primary: #4a7de0;

.dhu-preset {
  width: 100%;
}

.csv-upload {
  width: 100%;

  ::v-deep .el-upload,
  ::v-deep .el-upload-dragger {
    width: 100%;
  }

  ::v-deep .el-upload-dragger {
    height: auto;
    padding: 28px 20px;
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
  font-size: 42px;
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
  margin-top: 14px;

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
  margin-top: 8px;

  ::v-deep .el-progress-bar__outer,
  ::v-deep .el-progress-bar__inner {
    border-radius: 999px;
  }

  ::v-deep .el-progress-bar__inner {
    background: linear-gradient(90deg, #4a7de0 0%, #7fb1f5 100%);
    transition: width 0.2s ease;
  }
}

.dhu-fade-enter-active,
.dhu-fade-leave-active {
  transition: opacity 0.25s ease;
}

.dhu-fade-enter,
.dhu-fade-leave-to {
  opacity: 0;
}
</style>
