import { createRouter, createWebHistory } from 'vue-router';
import AdminLogin from '../views/admin/AdminLogin.vue';
import AdminDashboard from '../views/admin/AdminDashboard.vue';
import Products from '../views/admin/Products.vue';
import SafeStocks from '../views/admin/SafeStocks.vue';
import Categories from '../views/admin/Categories.vue';
import Employees from '../views/admin/Employees.vue';
import EmployeeLogin from '../views/employee/EmployeeLogin.vue';
import Inventory from '../views/employee/Inventory.vue';
import Statistics from '../views/admin/Statistics.vue';   // 修正這一行
import Home from '../views/Home.vue';

const routes = [
  { path: '/', component: Home },
  // 員工路由
  { path: '/employee/login', component: EmployeeLogin },
  {
    path: '/inventory',
    component: Inventory,
    meta: { requiresEmployee: true }
  },
  { path: '/admin/login', component: AdminLogin },
  {
    path: '/admin',
    component: AdminDashboard,
    meta: { requiresAdmin: true },
    redirect: '/admin/statistics',   // 新增這一行
    children: [
      { path: 'products', component: Products },
      { path: 'safe-stocks', component: SafeStocks },
      { path: 'categories', component: Categories },  // 新增
      { path: 'employees', component: Employees },  // 新增
      { path: 'statistics', component: Statistics },  // 新增
      // 其他子路由可依此類推
    ]
  }
];

const router = createRouter({ history: createWebHistory(), routes });

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  
  // 員工路由權限檢查
  if (to.meta.requiresEmployee) {
    if (!token) {
      next('/employee/login');
      return;
    }
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      if (!payload.permissions?.includes('inventory_access')) {
        next('/employee/login');
        return;
      }
    } catch (e) {
      next('/employee/login');
      return;
    }
  }
  
  // 管理員路由權限檢查
  if (to.meta.requiresAdmin) {
    if (!token) {
      next('/admin/login');
      return;
    }
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      if (!payload.permissions?.includes('admin')) {
        next('/admin/login');
        return;
      }
    } catch (e) {
      next('/admin/login');
      return;
    }
  }
  
  next();
});

export default router;