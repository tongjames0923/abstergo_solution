<template>
  <div class="app-container">
    <company-admin-view></company-admin-view>
  </div>
</template>

<script>
import CompanyAdminView from "@/components/CompanyAdminView/index.vue";

export default {
  name: "Company",
  components: {CompanyAdminView}
};
</script>
