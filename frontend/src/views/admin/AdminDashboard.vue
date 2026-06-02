<template>
  <div class="dashboard">
    <!-- 手机版菜单按钮 -->
    <div class="mobile-menu-btn" @click="drawerVisible = true">
      <el-icon><Menu /></el-icon>
    </div>

    <!-- 侧边栏（电脑/平板显示） -->
    <aside class="sidebar">
      <div class="logo-area">
        <h2>龍鹽酥雞</h2>
        <p>管理後台</p>
      </div>
      <ul class="nav-menu">
        <li>
          <router-link to="/admin/products" active-class="active">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </router-link>
        </li>
        <li>
          <router-link to="/admin/categories" active-class="active">
            <el-icon><Collection /></el-icon>
            <span>分類管理</span>
          </router-link>
        </li>
        <li>
          <router-link to="/admin/safe-stocks" active-class="active">
            <el-icon><Lock /></el-icon>
            <span>安全庫存</span>
          </router-link>
        </li>
        <li>
          <router-link to="/admin/statistics" active-class="active">
            <el-icon><DataAnalysis /></el-icon>
            <span>統計報表</span>
          </router-link>
        </li>
        <li>
          <router-link to="/admin/employees" active-class="active">
            <el-icon><User /></el-icon>
            <span>人資管理</span>
          </router-link>
        </li>
      </ul>
    </aside>

    <!-- 主内容区 -->
    <main class="content">
      <router-view />
    </main>

    <!-- 手机版 Drawer 菜单 -->
    <el-drawer v-model="drawerVisible" direction="ltr" size="70%" title="選單" class="mobile-drawer">
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
import { Menu, Goods, Collection, Lock, DataAnalysis, User } from '@element-plus/icons-vue';

const drawerVisible = ref(false);
const closeDrawer = () => {
  drawerVisible.value = false;
};
</script>

<style scoped>
.dashboard {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* ========== 侧边栏样式 ========== */
.sidebar {
  width: 260px;
  background: linear-gradient(180deg, #1e2a3a 0%, #0f1724 100%);
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 12px rgba(0,0,0,0.1);
  transition: all 0.3s;
  flex-shrink: 0;
}

.logo-area {
  padding: 30px 20px 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  margin-bottom: 20px;
}

.logo-area h2 {
  margin: 0;
  font-size: 1.6rem;
  color: #ffd966;
  letter-spacing: 2px;
}

.logo-area p {
  margin: 8px 0 0;
  font-size: 0.8rem;
  opacity: 0.7;
}

.nav-menu {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-menu li {
  margin: 8px 12px;
}

.nav-menu li a {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 12px;
  color: #cbd5e1;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s;
}

.nav-menu li a:hover {
  background: rgba(255,255,255,0.1);
  color: white;
  transform: translateX(4px);
}

.nav-menu li a.active {
  background: #409eff;
  color: white;
  box-shadow: 0 4px 8px rgba(64,158,255,0.3);
}

.nav-menu li a .el-icon {
  font-size: 1.2rem;
}

/* ========== 内容区域 ========== */
.content {
  flex: 1;
  padding: 24px 32px;
  overflow-x: auto;
  background: #f5f7fa;
}

/* ========== 手机版菜单按钮 ========== */
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
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
}

/* ========== 手机版 Drawer 样式 ========== */
.drawer-menu {
  list-style: none;
  padding: 0;
  margin: 0;
}
.drawer-menu li {
  padding: 12px 20px;
  border-bottom: 1px solid #eee;
}
.drawer-menu li a {
  text-decoration: none;
  color: #333;
  display: block;
  font-size: 1rem;
}

/* ========== 响应式布局 ========== */
/* 平板设备（768px - 1024px） */
@media (max-width: 1024px) {
  .sidebar {
    width: 220px;
  }
  .logo-area h2 {
    font-size: 1.3rem;
  }
  .nav-menu li a {
    padding: 10px 12px;
    font-size: 0.9rem;
  }
  .content {
    padding: 20px;
  }
}

/* 手机设备（< 768px） */
@media (max-width: 768px) {
  .mobile-menu-btn {
    display: block;
  }
  .sidebar {
    display: none; /* 侧边栏隐藏，由 drawer 替代 */
  }
  .content {
    padding: 60px 16px 16px;
    max-width: 100%;
  }
}
</style>