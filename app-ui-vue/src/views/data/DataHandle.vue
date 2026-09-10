<template>
  <div class="data-handle-container">
    <!-- 页面说明区 -->
    <el-card shadow="hover" class="handle-card">
      <div slot="header" class="card-header">
        <div class="title-area">
          <span class="card-title">{{ $t('dataHandle.title') }}</span>
          <span class="card-subtitle">{{ $t('dataHandle.subtitle') }}</span>
        </div>
        <el-button icon="el-icon-refresh" size="small" @click="fetchHandlers">
          {{ $t('dataHandle.refresh') }}
        </el-button>
      </div>

      <!-- 表单区 -->
      <el-form ref="handleForm" :model="form" label-width="120px" class="handle-form">
        <el-form-item :label="$t('dataHandle.handlerLabel')" required>
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
          <div v-if="isCustomHandler" class="validate-status" :class="validateState">
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
        </el-form-item>

        <el-form-item :label="$t('dataHandle.fileLabel')" required>
          <el-upload
            ref="upload"
            drag
            action="#"
            accept=".csv"
            :auto-upload="false"
            :limit="1"
            :on-change="onFileChange"
            :on-remove="() => (form.file = null)"
            :on-exceed="handleExceed"
            class="csv-upload"
          >
            <i class="el-icon-upload" />
            <div class="el-upload__text">
              {{ $t('dataHandle.dragText') }}<em>{{ $t('dataHandle.clickUpload') }}</em>
            </div>
            <div class="el-upload__tip" slot="tip">{{ $t('dataHandle.uploadTip') }}</div>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            icon="el-icon-upload2"
            :loading="submitting"
            @click="handleSubmit"
          >{{ $t('dataHandle.submit') }}</el-button>
          <el-button icon="el-icon-refresh-left" @click="handleReset">
            {{ $t('dataHandle.reset') }}
          </el-button>
        </el-form-item>
      </el-form>

      <el-progress
        v-if="submitting"
        :percentage="progress"
        :stroke-width="8"
        class="upload-progress"
      />
    </el-card>

    <!-- 结果展示区 -->
    <el-card v-if="result" shadow="hover" class="handle-card result-card">
      <div slot="header" class="card-header">
        <div class="title-area">
          <span class="card-title">{{ $t('dataHandle.resultTitle') }}</span>
          <span v-if="result.tableName" class="card-subtitle">{{ result.tableName }}</span>
        </div>
        <div class="result-stats">
          <el-tag size="small" type="info">
            {{ $t('dataHandle.rowCount', { count: result.rows.length }) }}
          </el-tag>
          <el-tag size="small" type="info">
            {{ $t('dataHandle.columnCount', { count: resultColumns.length }) }}
          </el-tag>
        </div>
      </div>
      <el-table
        v-if="resultColumns.length > 0"
        :data="resultRows"
        border
        stripe
        max-height="480"
        style="width: 100%"
      >
        <el-table-column type="index" :index="index => index + 1" width="60" fixed="left" />
        <el-table-column
          v-for="col in resultColumns"
          :key="col.index"
          :prop="`c${col.index}`"
          :label="col.name"
          min-width="140"
          show-overflow-tooltip
        />
      </el-table>
      <div v-else class="empty-result">
        <i class="el-icon-document" />
        <p>{{ $t('dataHandle.emptyResult') }}</p>
      </div>
    </el-card>
  </div>
</template>

<script>
import { listHandlers, validateHandler, handleCsv } from '@/api/data-handle'

const VALIDATE_DEBOUNCE_MS = 500

export default {
  name: 'DataHandle',
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
    },
    resultColumns() {
      if (!this.result || !this.result.columns) {
        return []
      }
      return [...this.result.columns].sort((a, b) => (a.index || 0) - (b.index || 0))
    },
    resultRows() {
      if (!this.result || !this.result.rows) {
        return []
      }
      return this.result.rows.map(row => {
        const item = {}
        ;(row.cells || []).forEach((cell, i) => {
          item[`c${i}`] = cell
        })
        return item
      })
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
      const isCsv = /\.csv$/i.test(file.name)
      if (!isCsv) {
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
            const target = document.querySelector('.result-card')
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
.data-handle-container {
  padding: 16px;
}

.handle-card {
  border-radius: 8px;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .title-area {
    display: flex;
    flex-direction: column;

    .card-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }

    .card-subtitle {
      margin-top: 4px;
      font-size: 13px;
      color: #909399;
    }
  }
}

.handler-select {
  width: 100%;
  max-width: 420px;
}

.option-name {
  float: left;
  margin-right: 12px;
}

.option-id {
  float: right;
  font-size: 12px;
  color: #909399;
}

.validate-status {
  margin-top: 6px;
  font-size: 12px;
  line-height: 20px;

  &.validating {
    color: #909399;
  }

  &.valid {
    color: #67c23a;
  }

  &.invalid {
    color: #f56c6c;
  }
}

.csv-upload {
  width: 100%;

  ::v-deep .el-upload-dragger {
    width: 100%;
  }
}

.upload-progress {
  margin-top: 4px;
}

.result-card {
  margin-top: 16px;

  .result-stats {
    display: flex;
    gap: 8px;
  }

  .empty-result {
    padding: 40px 0;
    text-align: center;
    color: #909399;

    i {
      font-size: 48px;
    }

    p {
      margin-top: 12px;
      font-size: 13px;
    }
  }
}
</style>
