// frontend/src/composables/useLogin.js
import { reactive, ref, computed, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api/axios';

export function useLogin(loginType) {
  const router = useRouter();
  const isLoading = ref(false);

  const storageKey = loginType === 'admin' ? 'adminLockedUntil' : 'employeeLockedUntil';
  const loginUrl = loginType === 'admin' ? '/auth/admin/login' : '/auth/employee/login';
  const successPath = loginType === 'admin' ? '/admin' : '/inventory';

  const form = reactive({
    tenantId: 2,
    ...(loginType === 'admin'
      ? { email: '', password: '' }
      : { identityNumber: '', password: '' })
  });

  const errorMsg = ref('');
  const lockUntil = ref(null);
  let lockTimer = null;

  const isLocked = computed(() => lockUntil.value !== null && Date.now() < lockUntil.value);

  const clearLockTimer = () => {
    if (lockTimer) {
      clearInterval(lockTimer);
      lockTimer = null;
    }
  };

  const startLockTimer = (until) => {
    if (lockTimer) clearInterval(lockTimer);
    lockTimer = setInterval(() => {
      const now = Date.now();
      if (now >= until) {
        clearLockTimer();
        lockUntil.value = null;
        errorMsg.value = '';
        localStorage.removeItem(storageKey);
      } else {
        const remainSeconds = Math.ceil((until - now) / 1000);
        const minutes = Math.floor(remainSeconds / 60);
        const seconds = remainSeconds % 60;
        if (minutes > 0) {
          errorMsg.value = `账号已锁定，请 ${minutes} 分 ${seconds} 秒后重试`;
        } else {
          errorMsg.value = `账号已锁定，请 ${seconds} 秒后重试`;
        }
      }
    }, 1000);
  };

  const handleLogin = async () => {
    // 每次请求前主动检查 localStorage 中的锁定时间戳是否过期，若过期则立即清除本地状态
    const stored = localStorage.getItem(storageKey);
    if (stored) {
      const until = parseInt(stored, 10);
      if (until <= Date.now()) {
        localStorage.removeItem(storageKey);
        if (lockUntil.value !== null) {
          lockUntil.value = null;
          clearLockTimer();
        }
      } else {
        // 尚未过期，确保 lockUntil 和定时器已启动
        if (lockUntil.value !== until) {
          lockUntil.value = until;
          startLockTimer(until);
        }
      }
    }

    // 拦截锁定状态
    if (isLocked.value) {
      errorMsg.value = '账号已锁定，请等待锁定时间结束';
      return;
    }

    errorMsg.value = ''; // 清空旧错误消息
    isLoading.value = true;
    try {
      const res = await api.post(loginUrl, form);
      localStorage.setItem('token', res.data.token);
      localStorage.removeItem(storageKey);
      clearLockTimer();
      lockUntil.value = null;
      await router.push(successPath);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || '登录失败';
      console.log('[DEBUG] 收到錯誤訊息:', msg);
      if (msg.includes('账号已锁定')) {
        const match = msg.match(/(\d+)\s*[分钟分鐘]/);
        let minutes = 15;
        if (match) minutes = parseInt(match[1], 10);
        if (minutes <= 0) minutes = 1;   // 防御：避免 0 分钟
        const until = Date.now() + minutes * 60 * 1000;
        console.log('[DEBUG] 鎖定截止時間:', new Date(until));
        lockUntil.value = until;
        localStorage.setItem(storageKey, until.toString());
        startLockTimer(until);
        // 不再调用 updateLockMessage，因为 startLockTimer 会负责倒计时显示
      } else {
        errorMsg.value = msg;
      }
    } finally {
      isLoading.value = false;
    }
  };

  const initLockState = () => {
    const stored = localStorage.getItem(storageKey);
    if (stored) {
      const until = parseInt(stored, 10);
      if (until > Date.now()) {
        lockUntil.value = until;
        startLockTimer(until);
      } else {
        localStorage.removeItem(storageKey);
      }
    }
  };

  onUnmounted(() => {
    clearLockTimer();
  });

  return {
    form,
    errorMsg,
    isLoading,
    isLocked,
    handleLogin,
    initLockState
  };
}