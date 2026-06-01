<template>
  <div class="login-wrapper">
    <div class="login-card">
      <h2>員工登入</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>租戶 ID</label>
          <input type="number" v-model="form.tenantId" required />
        </div>
        <div class="form-group">
          <label>身分證字號</label>
          <input type="text" v-model="form.identityNumber" required />
        </div>
        <div class="form-group">
          <label>生日</label>
          <input type="date" v-model="form.birthDate" required />
        </div>
        <button type="submit">登入</button>
      </form>
      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api/axios';

const router = useRouter();
const form = reactive({
  tenantId: 2,
  identityNumber: 'B987654321',
  birthDate: '1990-01-02'
});
const errorMsg = ref('');

const handleLogin = async () => {
  try {
    const res = await api.post('/auth/employee/login', form);
    localStorage.setItem('token', res.data.token);
    router.push('/inventory');
  } catch (err) {
    errorMsg.value = err.response?.data?.message || '登入失敗，請檢查帳號與生日';
  }
};
</script>

<style scoped>
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
  box-shadow: 0 20px 35px rgba(0, 0, 0, 0.1);
  padding: 2rem;
  width: 100%;
  max-width: 400px;
  transition: all 0.3s;
}
h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #2c3e50;
  font-weight: 600;
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
  transition: border 0.2s;
}
input:focus {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
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
button:hover {
  background: #66b1ff;
}
.error-msg {
  color: #f56c6c;
  text-align: center;
  margin-top: 1rem;
  font-size: 0.9rem;
}

/* 平板 / 手機調整 */
@media (max-width: 768px) {
  .login-card {
    padding: 1.5rem;
    max-width: 360px;
  }
  h2 {
    font-size: 1.5rem;
  }
}
@media (max-width: 480px) {
  .login-card {
    padding: 1.2rem;
    max-width: 100%;
  }
  input, button {
    font-size: 0.9rem;
  }
}
</style>