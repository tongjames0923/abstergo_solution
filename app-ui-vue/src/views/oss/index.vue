<template>
  <div class="oss-container">
    <el-card shadow="hover" class="oss-card">
      <!-- 卡片头部：标题与主操作 -->
      <div slot="header" class="card-header">
        <div class="title-area">
          <span class="card-title">{{ $t('oss.title') }}</span>
          <span class="card-subtitle">{{ $t('oss.subtitle') }}</span>
        </div>
        <div class="header-actions">
          <el-input
            v-model="bucketInput"
            :placeholder="$t('oss.bucketPlaceholder')"
            clearable
            size="small"
            style="width: 180px; margin-right: 10px;"
            @clear="handleSearch"
            @keyup.enter.native="handleSearch"
          >
            <i slot="prefix" class="el-input__icon el-icon-box" />
          </el-input>
          <el-button
            icon="el-icon-upload2"
            size="small"
            type="primary"
            @click="openUploadDialog"
          >{{ $t('oss.upload') }}</el-button>
          <el-button
            icon="el-icon-refresh"
            size="small"
            @click="handleSearch"
          >{{ $t('oss.refresh') }}</el-button>
          <el-button
            :disabled="selectedKeys.length === 0"
            icon="el-icon-delete"
            size="small"
            type="danger"
            plain
            @click="handleBatchDelete"
          >{{ $t('oss.batchDelete') }}<span v-if="selectedKeys.length > 0">（{{ selectedKeys.length }}）</span></el-button>
        </div>
      </div>

      <!-- 工具栏：路径搜索与目录面包屑 -->
      <div class="oss-toolbar">
        <el-input
          v-model="keyword"
          :placeholder="$t('oss.searchPlaceholder')"
          clearable
          size="small"
          style="width: 320px;"
          @clear="keyword = ''"
          @keyup.enter.native="handleSearch"
        >
          <el-button slot="append" icon="el-icon-search" @click="handleSearch" />
        </el-input>
        <el-breadcrumb separator="/" class="path-breadcrumb">
          <el-breadcrumb-item>
            <span class="crumb-link" @click="gotoPrefix('')">
              <i class="el-icon-coin" /> {{ $t('oss.root') }}
            </span>
          </el-breadcrumb-item>
          <el-breadcrumb-item v-for="(seg, i) in prefixSegments" :key="i">
            <span class="crumb-link" @click="gotoSegment(i)">{{ seg }}</span>
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 对象列表 -->
      <el-table
        v-loading="listLoading"
        :data="displayRows"
        :empty-text="$t('oss.empty')"
        border
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="45" :selectable="row => !row.isFolder" />
        <el-table-column :label="$t('oss.objectKey')" min-width="320" show-overflow-tooltip>
          <template slot-scope="scope">
            <span class="name-cell" @click="rowClick(scope.row)">
              <i :class="row.isFolder ? 'el-icon-folder' : fileIcon(scope.row.displayName)" class="file-icon" />
              <span :class="['name-text', { 'folder-name': scope.row.isFolder }]">{{ scope.row.displayName }}</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('oss.size')" width="120">
          <template slot-scope="scope">
            <span v-if="scope.row.isFolder">-</span>
            <span v-else>{{ formatBytes(scope.row.contentLength) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('oss.lastModified')" width="180">
          <template slot-scope="scope">
            <span v-if="scope.row.isFolder">-</span>
            <span v-else>{{ formatTime(scope.row.lastModified) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('oss.operation')" width="260" fixed="right">
          <template slot-scope="scope">
            <template v-if="scope.row.isFolder">
              <el-button icon="el-icon-folder-opened" size="mini" type="text" @click="enterFolder(scope.row)">{{ $t('oss.open') }}</el-button>
            </template>
            <template v-else>
              <el-button icon="el-icon-download" size="mini" type="text" @click="handleDownload(scope.row)">{{ $t('oss.download') }}</el-button>
              <el-button icon="el-icon-link" size="mini" type="text" @click="openPresignedDialog(scope.row)">{{ $t('oss.presigned') }}</el-button>
              <el-button icon="el-icon-document" size="mini" type="text" @click="handleMetadata(scope.row)">{{ $t('oss.metadata') }}</el-button>
              <el-button icon="el-icon-delete" size="mini" type="text" class="danger-text" @click="handleDelete(scope.row)">{{ $t('oss.delete') }}</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 底部：统计与游标翻页 -->
      <div class="oss-footer">
        <span class="count-tip">{{ $t('oss.countTip', { count: displayRows.length }) }}</span>
        <div class="pager">
          <el-button size="small" icon="el-icon-arrow-left" :disabled="markerStack.length === 0" @click="prevPage">{{ $t('oss.prevPage') }}</el-button>
          <el-button size="small" :disabled="!truncated" @click="nextPage">{{ $t('oss.nextPage') }} <i class="el-icon-arrow-right el-icon--right" /></el-button>
        </div>
      </div>
    </el-card>

    <!-- 上传对话框 -->
    <el-dialog :title="$t('oss.uploadTitle')" :visible.sync="uploadVisible" width="600px" :close-on-click-modal="false">
      <el-form label-width="110px" size="small">
        <el-form-item :label="$t('oss.targetDir')">
          <el-input v-model="uploadForm.prefix" :placeholder="$t('oss.targetDirPlaceholder')" clearable />
        </el-form-item>
      </el-form>
      <el-upload
        ref="uploader"
        drag
        multiple
        action=""
        :http-request="customUpload"
        :before-upload="beforeUpload"
        :show-file-list="false"
        :disabled="uploading"
        class="oss-uploader"
      >
        <i class="el-icon-upload" />
        <div class="el-upload__text">{{ $t('oss.dragTip') }}</div>
        <div slot="tip" class="el-upload__tip">{{ $t('oss.uploadNote') }}</div>
      </el-upload>
      <div v-if="uploadFileList.length > 0" class="upload-file-list">
        <div v-for="item in uploadFileList" :key="item.uid" class="upload-file-item">
          <i :class="fileIcon(item.name)" class="file-icon" />
          <span class="upload-file-name" :title="item.name">{{ item.name }}</span>
          <span class="upload-file-size">{{ formatBytes(item.size) }}</span>
          <div class="upload-file-progress">
            <el-progress
              :percentage="item.percentage"
              :status="item.status === 'success' ? 'success' : (item.status === 'fail' ? 'exception' : undefined)"
              :stroke-width="6"
            />
          </div>
          <el-button
            v-if="item.status === 'pending' && !uploading"
            icon="el-icon-close"
            size="mini"
            type="text"
            @click="removeUploadFile(item.uid)"
          />
        </div>
      </div>
      <span slot="footer">
        <el-button size="small" @click="uploadVisible = false">{{ $t('oss.close') }}</el-button>
        <el-button
          :loading="uploading"
          :disabled="pendingCount === 0"
          size="small"
          type="primary"
          @click="startUpload"
        >{{ uploading ? $t('oss.uploading') : $t('oss.startUpload') }}</el-button>
      </span>
    </el-dialog>

    <!-- 预签名地址对话框 -->
    <el-dialog :title="$t('oss.presignedTitle')" :visible.sync="presignedVisible" width="620px">
      <el-form label-width="110px" size="small">
        <el-form-item :label="$t('oss.objectKey')">
          <span class="plain-text">{{ presignedForm.objectKey }}</span>
        </el-form-item>
        <el-form-item :label="$t('oss.linkType')">
          <el-radio-group v-model="presignedForm.type">
            <el-radio-button label="DOWNLOAD">{{ $t('oss.linkDownload') }}</el-radio-button>
            <el-radio-button label="UPLOAD">{{ $t('oss.linkUpload') }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('oss.expireSeconds')">
          <el-input-number v-model="presignedForm.expire" :min="1" :max="604800" size="small" />
          <el-button size="mini" style="margin-left: 8px;" @click="presignedForm.expire = 3600">{{ $t('oss.oneHour') }}</el-button>
          <el-button size="mini" @click="presignedForm.expire = 86400">{{ $t('oss.oneDay') }}</el-button>
          <el-button size="mini" @click="presignedForm.expire = 604800">{{ $t('oss.sevenDays') }}</el-button>
        </el-form-item>
        <el-form-item v-if="presignedUrl" :label="$t('oss.linkResult')">
          <el-input :value="presignedUrl" type="textarea" :rows="3" readonly />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button size="small" @click="presignedVisible = false">{{ $t('oss.close') }}</el-button>
        <el-button
          v-if="presignedUrl"
          icon="el-icon-copy-document"
          size="small"
          @click="copyText(presignedUrl)"
        >{{ $t('oss.copyLink') }}</el-button>
        <el-button
          :loading="presignedLoading"
          icon="el-icon-link"
          size="small"
          type="primary"
          @click="submitPresigned"
        >{{ $t('oss.generate') }}</el-button>
      </span>
    </el-dialog>

    <!-- 对象元数据对话框 -->
    <el-dialog :title="$t('oss.metadataTitle')" :visible.sync="metadataVisible" width="620px">
      <div v-loading="metadataLoading">
        <el-descriptions :column="1" border size="medium">
          <el-descriptions-item :label="$t('oss.objectKey')">
            <span class="plain-text">{{ metadataDetail.objectKey }}</span>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('oss.size')">{{ formatBytes(metadataDetail.contentLength) }}</el-descriptions-item>
          <el-descriptions-item :label="$t('oss.contentType')">{{ metadataDetail.contentType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="ETag">{{ metadataDetail.etag || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="$t('oss.lastModified')">{{ formatTime(metadataDetail.lastModified) }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="userMetadataEntries.length > 0" class="user-metadata-block">
          <div class="block-title">{{ $t('oss.userMetadata') }}</div>
          <el-descriptions :column="1" border size="medium">
            <el-descriptions-item v-for="entry in userMetadataEntries" :key="entry.key" :label="entry.key">
              {{ entry.value }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <span slot="footer">
        <el-button icon="el-icon-copy-document" size="small" @click="copyText(metadataDetail.objectKey)">{{ $t('oss.copyKey') }}</el-button>
        <el-button size="small" @click="metadataVisible = false">{{ $t('oss.close') }}</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  uploadObject,
  getUploadProgress,
  listObjects,
  getObjectMetadata,
  getPresignedUrl,
  deleteObject,
  deleteObjects,
  downloadObject
} from '@/api/oss'

export default {
  name: 'OssManage',
  data() {
    return {
      // 列表相关
      bucketInput: '',
      keyword: '',
      prefix: '',
      marker: '',
      markerStack: [],
      objects: [],
      commonPrefixes: [],
      truncated: false,
      nextMarker: '',
      listLoading: false,
      selectedKeys: [],

      // 上传相关
      uploadVisible: false,
      uploading: false,
      uploadForm: { prefix: '' },
      uploadFileList: [],

      // 预签名相关
      presignedVisible: false,
      presignedLoading: false,
      presignedUrl: '',
      presignedForm: { objectKey: '', type: 'DOWNLOAD', expire: 3600 },

      // 元数据相关
      metadataVisible: false,
      metadataLoading: false,
      metadataDetail: { objectKey: '' }
    }
  },
  computed: {
    // 当前前缀对应的目录层级
    prefixSegments() {
      if (!this.prefix) return []
      return this.prefix.split('/').filter(s => s.length > 0)
    },
    // 目录（公共前缀） + 文件（对象摘要）合并后的展示行
    tableRows() {
      const folders = (this.commonPrefixes || []).map(p => ({
        isFolder: true,
        objectKey: p,
        displayName: this.trimPrefix(p)
      }))
      const files = (this.objects || []).map(o => ({
        isFolder: false,
        objectKey: o.objectKey,
        displayName: this.trimPrefix(o.objectKey),
        contentLength: o.contentLength,
        lastModified: o.lastModified,
        etag: o.etag
      }))
      return [...folders, ...files]
    },
    // 客户端关键字过滤后的展示行
    displayRows() {
      if (!this.keyword) return this.tableRows
      const kw = this.keyword.toLowerCase()
      return this.tableRows.filter(r => r.displayName.toLowerCase().includes(kw))
    },
    // 待上传文件数
    pendingCount() {
      return this.uploadFileList.filter(f => f.status === 'pending').length
    },
    // 用户自定义元数据键值对
    userMetadataEntries() {
      const meta = this.metadataDetail.userMetadata
      if (!meta) return []
      return Object.keys(meta).map(key => ({ key, value: meta[key] }))
    }
  },
  created() {
    this.loadList()
  },
  methods: {
    // ===== 列表加载与目录导航 =====
    loadList(marker) {
      this.listLoading = true
      const params = { prefix: this.prefix || undefined, marker: marker || undefined }
      if (this.bucketInput) {
        params.bucket = this.bucketInput
      }
      listObjects(params)
        .then(res => {
          const data = res.data || {}
          this.objects = data.objects || []
          this.commonPrefixes = data.commonPrefixes || []
          this.truncated = !!data.truncated
          this.nextMarker = data.nextMarker || ''
          this.listLoading = false
        })
        .catch(() => {
          this.objects = []
          this.commonPrefixes = []
          this.truncated = false
          this.listLoading = false
        })
    },

    handleSearch() {
      const kw = (this.keyword || '').trim()
      // 输入的是完整路径（含“/”）时，直接定位到该路径所在目录
      if (kw.includes('/')) {
        this.prefix = kw.endsWith('/') ? kw : kw.substring(0, kw.lastIndexOf('/') + 1)
        this.keyword = ''
      } else {
        this.prefix = ''
      }
      this.marker = ''
      this.markerStack = []
      this.selectedKeys = []
      this.loadList()
    },

    gotoPrefix(prefix) {
      if (this.prefix === prefix) return
      this.prefix = prefix
      this.marker = ''
      this.markerStack = []
      this.selectedKeys = []
      this.loadList()
    },

    gotoSegment(index) {
      this.gotoPrefix(this.prefixSegments.slice(0, index + 1).join('/') + '/')
    },

    enterFolder(row) {
      const key = row.objectKey.endsWith('/') ? row.objectKey : row.objectKey + '/'
      this.gotoPrefix(key)
    },

    rowClick(row) {
      if (row.isFolder) {
        this.enterFolder(row)
      }
    },

    prevPage() {
      this.marker = this.markerStack.pop() || ''
      this.selectedKeys = []
      this.loadList(this.marker)
    },

    nextPage() {
      if (!this.truncated) return
      this.markerStack.push(this.marker)
      this.marker = this.nextMarker
      this.selectedKeys = []
      this.loadList(this.marker)
    },

    handleSelectionChange(rows) {
      this.selectedKeys = rows.filter(r => !r.isFolder).map(r => r.objectKey)
    },

    // ===== 下载与删除 =====
    handleDownload(row) {
      downloadObject(row.objectKey, this.bucketInput || undefined).catch(() => {})
    },

    handleDelete(row) {
      this.$confirm(
        this.$t('oss.confirmDelete', { name: row.displayName }),
        this.$t('common.tip'),
        {
          confirmButtonText: this.$t('common.confirm'),
          cancelButtonText: this.$t('common.cancel'),
          type: 'warning'
        }
      ).then(() => {
        deleteObject(row.objectKey, this.bucketInput || undefined)
          .then(() => {
            this.loadList(this.marker)
          })
          .catch(() => {})
      }).catch(() => {})
    },

    handleBatchDelete() {
      if (this.selectedKeys.length === 0) return
      this.$confirm(
        this.$t('oss.confirmBatchDelete', { count: this.selectedKeys.length }),
        this.$t('common.tip'),
        {
          confirmButtonText: this.$t('common.confirm'),
          cancelButtonText: this.$t('common.cancel'),
          type: 'warning'
        }
      ).then(() => {
        deleteObjects(this.selectedKeys, this.bucketInput || undefined)
          .then(() => {
            this.selectedKeys = []
            this.loadList(this.marker)
          })
          .catch(() => {})
      }).catch(() => {})
    },

    // ===== 预签名地址 =====
    openPresignedDialog(row) {
      this.presignedForm = { objectKey: row.objectKey, type: 'DOWNLOAD', expire: 3600 }
      this.presignedUrl = ''
      this.presignedVisible = true
    },

    submitPresigned() {
      this.presignedLoading = true
      const params = {
        objectKey: this.presignedForm.objectKey,
        type: this.presignedForm.type,
        expirationSeconds: this.presignedForm.expire
      }
      if (this.bucketInput) {
        params.bucket = this.bucketInput
      }
      getPresignedUrl(params)
        .then(res => {
          this.presignedUrl = res.data || ''
          this.presignedLoading = false
        })
        .catch(() => {
          this.presignedLoading = false
        })
    },

    // ===== 对象元数据 =====
    handleMetadata(row) {
      this.metadataVisible = true
      this.metadataLoading = true
      this.metadataDetail = { objectKey: row.objectKey }
      getObjectMetadata(row.objectKey, this.bucketInput || undefined)
        .then(res => {
          this.metadataDetail = { ...(res.data || {}), objectKey: row.objectKey }
          this.metadataLoading = false
        })
        .catch(() => {
          this.metadataLoading = false
        })
    },

    // ===== 上传 =====
    openUploadDialog() {
      this.uploadForm.prefix = this.prefix
      this.uploadFileList = []
      this.uploading = false
      this.uploadVisible = true
    },

    beforeUpload(file) {
      this.uploadFileList.push({
        uid: Date.now() + '-' + Math.random().toString(36).slice(2),
        name: file.name,
        size: file.size,
        raw: file,
        status: 'pending',
        percentage: 0
      })
      return true
    },

    customUpload(option) {
      // 实际的上传动作在 startUpload 中统一发起，这里仅返回占位 Promise
      return new Promise(() => {})
    },

    removeUploadFile(uid) {
      this.uploadFileList = this.uploadFileList.filter(f => f.uid !== uid)
    },

    doUploadOne(item) {
      let prefix = (this.uploadForm.prefix || '').trim()
      if (prefix && !prefix.endsWith('/')) {
        prefix += '/'
      }
      item.status = 'uploading'
      item.percentage = 0
      // 两阶段真实进度：浏览器传输阶段（XHR进度）占0~50%，
      // 服务端写入对象存储阶段每3秒轮询后端真实进度占50%~100%
      const taskId = Date.now().toString(36) + Math.random().toString(36).slice(2)
      let requestDone = false
      const timer = setInterval(() => {
        getUploadProgress(taskId).then(res => {
          const progress = res.data || {}
          if (progress.status === 'STORING' || progress.status === 'SUCCESS') {
            item.percentage = 50 + Math.round((progress.percentage || 0) / 2)
          }
          // RECEIVING：服务端仍在接收请求体，保持当前浏览器传输进度
        }).catch(() => {})
      }, 3000)
      const finish = success => {
        requestDone = true
        clearInterval(timer)
        item.status = success ? 'success' : 'fail'
        if (success) {
          item.percentage = 100
        }
      }
      return uploadObject(item.raw, prefix ? prefix + item.name : undefined, this.bucketInput || undefined,
        percent => {
          if (!requestDone) {
            item.percentage = Math.min(50, Math.round(percent / 2))
          }
        }, taskId)
        .then(() => finish(true))
        .catch(() => finish(false))
    },

    async startUpload() {
      // 校验自定义目录格式
      const prefix = (this.uploadForm.prefix || '').trim()
      if (prefix && !/^[^/].*$/.test(prefix)) {
        this.$message.warning(this.$t('oss.prefixInvalid'))
        return
      }
      const pending = this.uploadFileList.filter(f => f.status === 'pending' || f.status === 'fail')
      if (pending.length === 0) return
      this.uploading = true
      let hasFail = false
      for (const item of pending) {
        await this.doUploadOne(item)
        if (item.status === 'fail') {
          hasFail = true
        }
      }
      this.uploading = false
      if (!hasFail) {
        this.uploadVisible = false
      }
      this.loadList(this.marker)
    },

    // ===== 通用工具 =====
    trimPrefix(key) {
      if (this.prefix && key.startsWith(this.prefix)) {
        return key.substring(this.prefix.length) || key
      }
      return key
    },

    fileIcon(name) {
      const lower = (name || '').toLowerCase()
      if (/\.(png|jpe?g|gif|bmp|webp|svg|ico)$/.test(lower)) return 'el-icon-picture-outline'
      if (/\.(mp4|avi|mov|mkv|wmv|flv)$/.test(lower)) return 'el-icon-video-camera'
      if (/\.(mp3|wav|flac|aac|ogg)$/.test(lower)) return 'el-icon-headset'
      if (/\.(zip|rar|7z|tar|gz)$/.test(lower)) return 'el-icon-files'
      if (/\.(pdf)$/.test(lower)) return 'el-icon-reading'
      if (/\.(docx?|xlsx?|pptx?|txt|md|csv)$/.test(lower)) return 'el-icon-notebook-2'
      return 'el-icon-document'
    },

    formatBytes(bytes) {
      if (bytes === null || bytes === undefined) return '-'
      if (bytes === 0) return '0 B'
      const units = ['B', 'KB', 'MB', 'GB', 'TB']
      let i = 0
      let num = Number(bytes)
      while (num >= 1024 && i < units.length - 1) {
        num /= 1024
        i++
      }
      return num.toFixed(i === 0 ? 0 : 2) + ' ' + units[i]
    },

    formatTime(value) {
      if (!value) return '-'
      const date = new Date(value)
      if (isNaN(date.getTime())) return String(value)
      const pad = n => (n < 10 ? '0' + n : '' + n)
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
    },

    copyText(text) {
      if (!text) return
      if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
          this.$message.success(this.$t('oss.copySuccess'))
        }).catch(() => {
          this.fallbackCopy(text)
        })
      } else {
        this.fallbackCopy(text)
      }
    },

    fallbackCopy(text) {
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      try {
        document.execCommand('copy')
        this.$message.success(this.$t('oss.copySuccess'))
      } catch (e) {
        this.$message.error(this.$t('oss.copyFail'))
      }
      document.body.removeChild(textarea)
    }
  }
}
</script>

<style lang="scss" scoped>
.oss-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;

    .title-area {
      display: flex;
      flex-direction: column;
    }

    .card-title {
      font-weight: 600;
      font-size: 15px;
    }

    .card-subtitle {
      margin-top: 4px;
      font-size: 12px;
      color: #909399;
    }

    .header-actions {
      display: flex;
      align-items: center;
    }
  }

  .oss-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    margin-bottom: 14px;

    .path-breadcrumb {
      display: inline-flex;
      align-items: center;
      flex-wrap: wrap;

      .crumb-link {
        cursor: pointer;
        color: #606266;

        &:hover {
          color: #409eff;
        }
      }
    }
  }

  .name-cell {
    display: inline-flex;
    align-items: center;
    cursor: pointer;

    .file-icon {
      margin-right: 6px;
      font-size: 15px;
      color: #909399;
      flex-shrink: 0;
    }

    .name-text {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .folder-name {
      color: #409eff;
      font-weight: 600;
    }

    &:hover .name-text {
      color: #409eff;
    }
  }

  .danger-text {
    color: #f56c6c;
  }

  .oss-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;

    .count-tip {
      color: #909399;
      font-size: 12px;
    }
  }

  .plain-text {
    word-break: break-all;
    color: #303133;
  }

  // 上传对话框
  .oss-uploader {
    ::v-deep .el-upload {
      width: 100%;
    }

    ::v-deep .el-upload-dragger {
      width: 100%;
    }
  }

  .upload-file-list {
    margin-top: 12px;
    max-height: 220px;
    overflow-y: auto;
    border: 1px solid #ebeef5;
    border-radius: 4px;

    .upload-file-item {
      display: flex;
      align-items: center;
      padding: 8px 12px;
      border-bottom: 1px solid #f5f5f5;

      &:last-child {
        border-bottom: none;
      }

      .file-icon {
        margin-right: 8px;
        color: #909399;
        flex-shrink: 0;
      }

      .upload-file-name {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-size: 13px;
        color: #303133;
      }

      .upload-file-size {
        margin: 0 12px;
        font-size: 12px;
        color: #909399;
        flex-shrink: 0;
      }

      .upload-file-progress {
        width: 160px;
        flex-shrink: 0;
      }
    }
  }

  // 元数据对话框
  .user-metadata-block {
    margin-top: 16px;

    .block-title {
      margin-bottom: 8px;
      font-weight: 600;
      font-size: 13px;
      color: #606266;
    }
  }

  // 滚动条美化
  ::-webkit-scrollbar {
    width: 6px;
  }

  ::-webkit-scrollbar-thumb {
    background-color: #dcdfe6;
    border-radius: 3px;
  }

  ::-webkit-scrollbar-track {
    background-color: transparent;
  }
}
</style>
