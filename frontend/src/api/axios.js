import axios from 'axios';
import router from '../router';

const api = axios.create({
  baseURL: 'http://localhost:8080',
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
      // 清除已失效的 token
      localStorage.removeItem('token');
      // 根據當前路徑決定跳轉
      const currentPath = router.currentRoute.value.path;
      if (currentPath.startsWith('/admin')) {
        router.push('/admin/login');
      } else if (currentPath.startsWith('/inventory')) {
        router.push('/employee/login');
      } else {
        // 其他情況可預設跳轉到員工登入或首頁
        router.push('/employee/login');
      }
      // 可選：顯示提示訊息
      // ElMessage.error('登入已過期，請重新登入');
    }
    return Promise.reject(error);
  }
);

export default api;