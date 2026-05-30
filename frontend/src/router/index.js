import { createRouter, createWebHistory } from 'vue-router';
import AdminLogin from '../views/AdminLogin.vue';
import AdminDashboard from '../views/AdminDashboard.vue';
import Products from '../views/admin/Products.vue';
import SafeStocks from '../views/admin/SafeStocks.vue';

const routes = [
  { path: '/admin/login', component: AdminLogin },
  {
    path: '/admin',
    component: AdminDashboard,
    meta: { requiresAdmin: true },
    children: [
      { path: 'products', component: Products },
      { path: 'safe-stocks', component: SafeStocks },
      // 其他子路由可依此類推
    ]
  }
];

const router = createRouter({ history: createWebHistory(), routes });

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  if (to.meta.requiresAdmin && !token) next('/admin/login');
  else next();
});

export default router;