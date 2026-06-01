<template>
  <div class="dashboard">
    <!-- 手機版選單按鈕 -->
    <div class="mobile-menu-btn" @click="drawerVisible = true">
      <el-icon><Menu /></el-icon>
    </div>
    <!-- 側邊欄 (桌機顯示，手機隱藏) -->
    <aside class="sidebar">
      <ul>
        <li><router-link to="/admin/products">商品管理</router-link></li>
        <li><router-link to="/admin/categories">分類管理</router-link></li>
        <li><router-link to="/admin/safe-stocks">安全庫存</router-link></li>
        <li><router-link to="/admin/statistics">統計報表</router-link></li>
        <li><router-link to="/admin/employees">人資管理</router-link></li>
      </ul>
    </aside>
    <main class="content">
      <router-view />
    </main>
    <!-- 手機版 Drawer -->
    <el-drawer v-model="drawerVisible" direction="ltr" size="60%" title="選單">
      <ul class="drawer-menu">
        <li><router-link to="/admin/products" @click="closeDrawer">商品管理</router-link></li>
        <li><router-link to="/admin/categories" @click="closeDrawer">分類管理</router-link></li>
        <li><router-link to="/admin/safe-stocks" @click="closeDrawer">安全庫存</router-link></li>
        <li><router-link to="/admin/statistics" @click="closeDrawer">統計報表</router-link></li>
        <li><router-link to="/admin/employees" @click="closeDrawer">人資管理</router-link></li>
      </ul>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { Menu } from '@element-plus/icons-vue';

const drawerVisible = ref(false);

const closeDrawer = () => {
  drawerVisible.value = false;
};
</script>

<style scoped>
.dashboard {
  display: flex;
  min-height: 100vh;
  max-width: 100%;
  overflow-x: hidden;
}
.mobile-menu-btn {
  display: none;
  position: fixed;
  top: 16px;
  left: 16px;
  z-index: 100;
  background: #409eff;
  color: white;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}
.sidebar {
  width: 220px;
  background: #fff;
  padding: 20px;
  box-shadow: 2px 0 12px rgba(0,0,0,0.05);
  flex-shrink: 0;
}
.content {
  flex: 1;
  padding: 20px;
  max-width: calc(100% - 220px);
  overflow-x: auto;
}
@media (max-width: 768px) {
  .mobile-menu-btn {
    display: block;
  }
  .sidebar {
    display: none;
  }
  .content {
    max-width: 100%;
    padding-top: 60px;
  }
}
.drawer-menu {
  list-style: none;
  padding: 0;
}
.drawer-menu li {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
}
.drawer-menu li a {
  text-decoration: none;
  color: #333;
  display: block;
}
</style>