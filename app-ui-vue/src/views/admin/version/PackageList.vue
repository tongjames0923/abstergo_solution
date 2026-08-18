<template>
  <div>
    <div class="tab-toolbar">
      <div class="filter-row">
        <el-select v-model="filterVersionId" :placeholder="$t('version.selectVersion')" size="small" filterable clearable style="width:280px" @change="loadPackages">
          <el-option v-for="v in allVersions" :key="v.id" :label="`${v.appKey} - ${v.versionName} (${v.platform})`" :value="v.id" />
        </el-select>
      </div>
      <el-button type="primary" icon="el-icon-plus" size="small" @click="handleAdd">{{ $t('version.addPackage') }}</el-button>
    </div>
    <el-table v-loading="loading" :data="list" stripe border style="width:100%;margin-top:12px" size="small">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="bucketName" :label="$t('version.bucketName')" show-overflow-tooltip />
      <el-table-column prop="objectName" :label="$t('version.objectName')" show-overflow-tooltip />
      <el-table-column prop="md5" :label="$t('version.md5')" width="280">
        <template slot-scope="{ row }"><code class="mono-text">{{ row.md5 || '-' }}</code></template>
      </el-table-column>
      <el-table-column prop="downloadUrl" :label="$t('version.downloadUrl')" width="120">
        <template slot-scope="{ row }">
          <el-button v-if="row.downloadUrl" size="mini" type="text" @click="copyText(row.downloadUrl)"><i class="el-icon-link" /> URL</el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.operation')" width="190" fixed="right">
        <template slot-scope="{ row }">
          <el-button size="mini" type="text" @click="handleDownload(row)"><i class="el-icon-download" /> {{ $t('version.download') }}</el-button>
          <el-button size="mini" type="text" @click="handleEdit(row)"><i class="el-icon-edit-outline" /> {{ $t('common.edit') }}</el-button>
          <el-button size="mini" type="text" class="danger-text" @click="handleDelete(row)"><i class="el-icon-delete" /> {{ $t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 安装包编辑弹窗（文件经统一 OSS 接口直传，无需手填存储坐标） -->
    <el-dialog :title="form.id ? $t('version.editPackage') : $t('version.addPackage')" :visible.sync="dialogVisible" width="540px" :close-on-click-modal="false" @close="handleDialogClose">
      <el-form ref="pkgForm" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('version.versionLabel')" prop="versionId">
          <el-select v-model="form.versionId" :disabled="!!form.id" filterable style="width:100%">
            <el-option v-for="v in allVersions" :key="v.id" :label="`${v.appKey} - ${v.versionName} (${v.platform})`" :value="v.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('version.packageFile')">
          <div v-if="form.objectName" class="pkg-uploaded">
            <i class="el-icon-document-checked" />
            <div class="pkg-uploaded-info">
              <span class="mono-text">{{ form.objectName }}</span>
              <span v-if="form.md5" class="pkg-uploaded-md5">MD5: {{ form.md5 }}</span>
            </div>
            <el-button type="text" :disabled="uploading" @click="resetFile">{{ $t('version.replaceFile') }}</el-button>
          </div>
          <el-upload v-else drag action="" :http-request="handleUpload" :show-file-list="false" :disabled="uploading" class="pkg-uploader">
            <i class="el-icon-upload" />
            <div class="el-upload__text">{{ $t('version.uploadTip') }}</div>
            <div slot="tip" class="el-upload__tip">{{ $t('version.uploadNote') }}</div>
          </el-upload>
          <el-progress v-if="uploading" :percentage="uploadPercent" :stroke-width="6" style="margin-top:8px" />
        </el-form-item>
        <el-form-item :label="$t('version.downloadUrl')">
          <el-input v-model="form.downloadUrl" :placeholder="$t('version.externalUrlPh')" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" :disabled="uploading" @click="handleSave">{{ $t('common.confirm') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { queryVersions, savePackage, deletePackage, listPackagesByVersion } from '@/api/app-version'
import { uploadObject, getUploadProgress, deleteObject, downloadObject } from '@/api/oss'

export default {
  name: 'PackageList',
  inject: ['copyText'],
  props: {
    allVersions: { type: Array, default: () => [] }
  },
  data() {
    return {
      loading: false, list: [], filterVersionId: null,
      dialogVisible: false, saving: false, uploading: false, uploadPercent: 0,
      form: { id: null, versionId: null, bucketName: '', objectName: '', md5: '', downloadUrl: '' },
      rules: {
        versionId: [{ required: true, message: this.$t('version.versionRequired'), trigger: 'change' }]
      }
    }
  },
  methods: {
    async loadPackages() {
      if (!this.filterVersionId) { this.list = []; return }
      this.loading = true
      try {
        const res = await listPackagesByVersion(this.filterVersionId)
        this.list = res.data || []
      } catch (e) { console.error(e) } finally { this.loading = false }
    },
    handleAdd() {
      this.form = { id: null, versionId: this.filterVersionId, bucketName: '', objectName: '', md5: '', downloadUrl: '' }
      this.uploading = false; this.uploadPercent = 0
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.pkgForm && this.$refs.pkgForm.clearValidate())
    },
    handleEdit(row) {
      this.form = { id: row.id, versionId: row.versionId, bucketName: row.bucketName || '', objectName: row.objectName || '', md5: row.md5 || '', downloadUrl: row.downloadUrl || '' }
      this.uploading = false; this.uploadPercent = 0
      this.dialogVisible = true
    },
    resetFile() {
      this.$set(this.form, 'bucketName', '')
      this.$set(this.form, 'objectName', '')
      this.$set(this.form, 'md5', '')
    },
    /** 选中文件后经统一 OSS 接口直传，成功后回填存储坐标与 MD5 */
    async handleUpload({ file }) {
      if (!this.form.versionId) {
        this.$message.warning(this.$t('version.versionRequired'))
        return
      }
      this.uploading = true
      this.uploadPercent = 0
      const previousKey = this.form.objectName || null
      const objectKey = `app-package/${this.form.versionId}/${Date.now()}_${file.name}`
      // 两阶段真实进度（与 OSS 模块一致）：浏览器传输阶段（XHR进度）占0~50%，
      // 服务端写入对象存储阶段每3秒轮询后端真实进度占50%~100%
      const taskId = Date.now().toString(36) + Math.random().toString(36).slice(2)
      let requestDone = false
      const timer = setInterval(() => {
        getUploadProgress(taskId).then(res => {
          const progress = res.data || {}
          if (progress.status === 'STORING' || progress.status === 'SUCCESS') {
            this.uploadPercent = 50 + Math.round((progress.percentage || 0) / 2)
          }
          // RECEIVING：服务端仍在接收请求体，保持当前浏览器传输进度
        }).catch(() => {})
      }, 3000)
      try {
        const res = await uploadObject(file, objectKey, undefined, percent => {
          if (!requestDone) {
            this.uploadPercent = Math.min(50, Math.round(percent / 2))
          }
        }, taskId)
        requestDone = true
        clearInterval(timer)
        const result = res.data || {}
        this.$set(this.form, 'bucketName', result.bucketName || '')
        this.$set(this.form, 'objectName', result.objectKey || objectKey)
        this.$set(this.form, 'md5', result.etag || '')
        this.uploadPercent = 100
        this.$message.success(this.$t('version.uploadSuccess'))
        // 替换旧文件时同步清理 OSS 中的旧对象，避免残留孤儿文件
        if (previousKey && previousKey !== this.form.objectName) {
          deleteObject(previousKey, this.form.bucketName || undefined).catch(() => {})
        }
      } catch (e) {
        requestDone = true
        clearInterval(timer)
        console.error(e)
        this.$message.error(this.$t('version.uploadFailed'))
      } finally {
        this.uploading = false
      }
    },
    async handleSave() {
      try { await this.$refs.pkgForm.validate() } catch { return }
      if (!this.form.objectName && !this.form.downloadUrl) {
        this.$message.warning(this.$t('version.fileRequired'))
        return
      }
      this.saving = true
      try { await savePackage(this.form); this.dialogVisible = false; await this.loadPackages() } catch (e) { console.error(e) } finally { this.saving = false }
    },
    async handleDelete(row) {
      try {
        const tip = row.objectName ? this.$t('version.confirmDeletePackageOss') : this.$t('version.confirmDeletePackage')
        await this.$confirm(tip, this.$t('common.tip'), { type: 'error' })
        await deletePackage(row.id)
        // 记录删除后尽力清理 OSS 对象，失败仅提示不阻断
        if (row.objectName) {
          deleteObject(row.objectName, row.bucketName || undefined).catch(() => {
            this.$message.warning(this.$t('version.ossCleanFailed', { key: row.objectName }))
          })
        }
        await this.loadPackages()
      } catch (e) { if (e !== 'cancel') console.error(e) }
    },
    /** 优先走 OSS 预签名下载，外部托管的退回直链 */
    async handleDownload(row) {
      if (row.objectName) {
        try { await downloadObject(row.objectName, row.bucketName || undefined) } catch (e) { console.error(e) }
      } else if (row.downloadUrl) {
        window.open(row.downloadUrl, '_blank')
      }
    },
    handleDialogClose() {
      this.$refs.pkgForm && this.$refs.pkgForm.resetFields()
    }
  }
}
</script>

<style lang="scss" scoped>
.tab-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.mono-text {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 12px;
  color: #64748b;
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
}

.danger-text { color: #f56c6c !important; }

.pkg-uploader {
  width: 100%;

  ::v-deep .el-upload { width: 100%; }
  ::v-deep .el-upload-dragger { width: 100%; }
}

.pkg-uploaded {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 6px;

  > i {
    font-size: 20px;
    color: #67c23a;
  }

  .pkg-uploaded-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;

    .mono-text {
      display: inline-block;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      max-width: 100%;
    }
  }

  .pkg-uploaded-md5 {
    font-size: 12px;
    color: #909399;
  }
}
</style>
