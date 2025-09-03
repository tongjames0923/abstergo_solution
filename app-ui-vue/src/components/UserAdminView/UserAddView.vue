<template>
  <div class="user-add-view">
    <DynamicFormWithApi
      ref="dynamicForm"
      config-name="userAddForm"
      :custom-components="{ urlImage: UrlImage }"
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
    </DynamicFormWithApi>
  </div>
</template>

<script>
import DynamicFormWithApi from "@/components/DynamicForm/DynamicFormWithApi.vue";
import UrlImage from "@/components/UrlImage/index.vue";
import {saveUser} from "@/api/admin_user";

export default {
  name: 'UserAddView',
  components: {
    DynamicFormWithApi,
    UrlImage
  },
  computed: {
    UrlImage() {
      return UrlImage
    }
  },
  methods: {
    /**
     * 提交表单
     */
    async handleSubmit(formData) {
      try {
        // 构造用户数据（排除confirmPassword字段）
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
     * 重置表单
     */
    resetForm() {
      this.$refs.dynamicForm.resetForm()
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

  .form-actions {
    text-align: right;
    padding-top: 15px;
    border-top: 1px solid #ebeef5;
  }

  ::v-deep .el-form-item__label {
    font-weight: 500;
  }

  ::v-deep .el-form-item {
    margin-bottom: 18px;
  }
}
</style>
