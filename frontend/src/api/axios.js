import axios from 'axios';
import router from '../router';

// 从环境变量获取 API 基础地址，如果没有则回退到 localhost（开发环境）
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

// 請求攔截器：自動帶入 JWT
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 回應攔截器：處理 Token 過期
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      const currentPath = router.currentRoute.value.path;
      if (currentPath.startsWith('/admin')) {
        router.push('/admin/login');
      } else if (currentPath.startsWith('/inventory')) {
        router.push('/employee/login');
      } else {
        router.push('/employee/login');
      }
    }
    return Promise.reject(error);
  }
);

export default api;