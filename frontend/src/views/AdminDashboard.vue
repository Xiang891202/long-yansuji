<template>
  <div class="dashboard">
    <aside class="sidebar">
      <ul>
        <li><router-link to="/admin/products">商品管理</router-link></li>
        <li><router-link to="/admin/categories">分類管理</router-link></li>
        <li><router-link to="/admin/safe-stocks">安全庫存</router-link></li>
        <li><router-link to="/admin/statistics">統計報表</router-link></li>
      </ul>
    </aside>
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script setup></script>

<style scoped>
.dashboard { display: flex; height: 100vh; }
.sidebar { width: 200px; background: #f5f5f5; padding: 20px; }
.content { flex: 1; padding: 20px; }
</style>