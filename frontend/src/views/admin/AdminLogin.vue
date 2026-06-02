<template>
  <div class="login-wrapper">
    <div class="login-card">
      <h2>管理員登入</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>租戶 ID</label>
          <input type="number" v-model="form.tenantId" required />
        </div>
        <div class="form-group">
          <label>Email</label>
          <input type="email" v-model="form.email" required />
        </div>
        <div class="form-group">
          <label>手機號碼</label>
          <input type="tel" v-model="form.phone" required />
        </div>
        <button type="submit" :disabled="isLoading">登入</button>
      </form>
      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api/axios';
import { useLoading } from '@/composables/useLoading';

const router = useRouter();
const form = reactive({ tenantId: 2, email: 'boss@ysgs.com', phone: '0987654321' });
const errorMsg = ref('');

const { isLoading, withLoading } = useLoading();

const handleLogin = () => withLoading(async () => {
  const res = await api.post('/auth/admin/login', form);
  localStorage.setItem('token', res.data.token);
  router.push('/admin');
});
</script>

<style scoped>
/* 样式保持原样 */
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9eef3 100%);
  padding: 20px;
}
.login-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 35px rgba(0,0,0,0.1);
  padding: 2rem;
  width: 100%;
  max-width: 400px;
}
h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #2c3e50;
}
.form-group {
  margin-bottom: 1.2rem;
}
label {
  display: block;
  margin-bottom: 0.4rem;
  font-weight: 500;
  color: #4a5568;
}
input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  font-size: 1rem;
}
input:focus {
  outline: none;
  border-color: #409eff;
}
button {
  width: 100%;
  background: #409eff;
  color: white;
  border: none;
  padding: 0.8rem;
  border-radius: 40px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.2s;
  margin-top: 0.5rem;
}
button:disabled {
  background: #a0cfff;
  cursor: not-allowed;
}
button:hover:not(:disabled) {
  background: #66b1ff;
}
.error-msg {
  color: #f56c6c;
  text-align: center;
  margin-top: 1rem;
}
@media (max-width: 768px) {
  .login-card {
    padding: 1.5rem;
  }
}
</style>