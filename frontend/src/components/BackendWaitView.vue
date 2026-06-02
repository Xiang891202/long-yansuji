<template>
  <div class="wait-container">
    <div class="wait-card">
      <h1>🐉 龍鹽酥雞</h1>
      <p class="status-text">{{ statusMessage }}</p>
      <div class="countdown">
        <span class="seconds">{{ countdown }}</span> 秒後自動重試
      </div>
      <div class="retry-count">
        已執行 <strong>{{ retryCount }}</strong> / 3 次
      </div>
      <el-button type="primary" @click="manualRefresh" :loading="refreshing">
        立即刷新
      </el-button>
      <p class="tip">後端服務可能需要 30~60 秒啟動，請耐心等候</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import api from '@/api/axios';
import { useBackendState } from '@/stores/backend';

const { setBackendReady } = useBackendState();

const originalPath = ref(window.location.pathname || '/');
const statusMessage = ref('後端啟動中...');
const countdown = ref(20);
const retryCount = ref(0);
const refreshing = ref(false);
let timer = null;
let isRetrying = false;

const checkBackend = async () => {
  try {
    const res = await api.get('/health', { timeout: 5000 });
    return res.status === 200;
  } catch {
    return false;
  }
};

const stopTimer = () => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
};

const startCountdown = (seconds) => {
  if (timer) stopTimer();
  countdown.value = seconds;
  timer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--;
    } else {
      stopTimer();
      performRetry();
    }
  }, 1000);
};

const performRetry = async () => {
  if (isRetrying) return;
  isRetrying = true;
  retryCount.value++;
  
  const success = await checkBackend();
  if (success) {
    // 通知 App.vue 后端已就绪
    setBackendReady(true);
    // 不需要刷新页面，App.vue 会自动显示 router-view
    // 但需要手动跳转到原始路径（路由改变）
    if (originalPath.value !== window.location.pathname) {
      window.history.pushState(null, '', originalPath.value);
      // 触发路由更新
      window.dispatchEvent(new PopStateEvent('popstate'));
    }
    return;
  }
  
  if (retryCount.value >= 3) {
    statusMessage.value = '後端啟動失敗，請稍後再試';
    stopTimer();
    countdown.value = 0;
  } else {
    statusMessage.value = '後端啟動中...';
    startCountdown(20);
  }
  isRetrying = false;
};

const manualRefresh = async () => {
  if (refreshing.value) return;
  refreshing.value = true;
  stopTimer();
  retryCount.value = 0;
  countdown.value = 20;
  statusMessage.value = '後端啟動中...';
  isRetrying = false;
  
  const success = await checkBackend();
  if (success) {
    setBackendReady(true);
    if (originalPath.value !== window.location.pathname) {
      window.history.pushState(null, '', originalPath.value);
      window.dispatchEvent(new PopStateEvent('popstate'));
    }
  } else {
    startCountdown(20);
  }
  refreshing.value = false;
};

onMounted(async () => {
  const success = await checkBackend();
  if (success) {
    setBackendReady(true);
    // 如果当前路径不是原始路径，更新地址
    if (originalPath.value !== window.location.pathname) {
      window.history.pushState(null, '', originalPath.value);
      window.dispatchEvent(new PopStateEvent('popstate'));
    }
  } else {
    startCountdown(20);
  }
});

onUnmounted(() => {
  stopTimer();
});
</script>

<style scoped>
.wait-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9eef3 100%);
  padding: 20px;
}
.wait-card {
  background: white;
  border-radius: 24px;
  box-shadow: 0 20px 35px rgba(0,0,0,0.1);
  padding: 2rem;
  width: 100%;
  max-width: 420px;
  text-align: center;
}
h1 {
  font-size: 1.8rem;
  margin-bottom: 0.5rem;
  color: #d32f2f;
}
.status-text {
  font-size: 1.2rem;
  margin: 20px 0 10px;
  color: #333;
  font-weight: bold;
}
.countdown {
  font-size: 2rem;
  font-weight: bold;
  color: #409eff;
  margin: 15px 0;
}
.seconds {
  font-size: 2.5rem;
}
.retry-count {
  margin: 15px 0;
  color: #666;
}
.tip {
  margin-top: 20px;
  font-size: 0.8rem;
  color: #999;
}
button {
  margin-top: 10px;
}
</style>