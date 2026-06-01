<template>
  <router-view />
</template>

<script setup>
</script>

<!-- App.vue 中的 style 區塊 -->
<style>
/* 全域 RWD 基礎設定 */
* {
  box-sizing: border-box;
}
html, body {
  margin: 0;
  padding: 0;
  font-family: 'Segoe UI', 'Roboto', sans-serif;
  background-color: #f5f7fa;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}
/* 手機版表格水平滾動 */
.el-table {
  overflow-x: auto;
  display: block;
  width: 100%;
}
/* 表單在小螢幕上佔滿寬度 */
@media (max-width: 768px) {
  .el-form-item {
    flex-wrap: wrap;
  }
  .el-form-item__label {
    width: 100% !important;
    text-align: left;
  }
  .el-form-item__content {
    margin-left: 0 !important;
    width: 100%;
  }
  .el-dialog {
    width: 90% !important;
  }
}
</style>