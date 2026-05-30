<template>
  <div class="login-container">
    <h2>管理員登入</h2>
    <form @submit.prevent="handleLogin">
      <div><label>租戶 ID</label><input type="number" v-model="form.tenantId" required /></div>
      <div><label>Email</label><input type="email" v-model="form.email" required /></div>
      <div><label>手機號碼</label><input type="tel" v-model="form.phone" required /></div>
      <button type="submit">登入</button>
    </form>
    <p v-if="errorMsg" style="color: red">{{ errorMsg }}</p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api/axios';

const router = useRouter();
const form = reactive({ tenantId: 2, email: 'boss@ysgs.com', phone: '0987654321' });
const errorMsg = ref('');

const handleLogin = async () => {
  try {
    const res = await api.post('/auth/admin/login', form);
    localStorage.setItem('token', res.data.token);
    router.push('/admin');
  } catch (err) {
    errorMsg.value = err.response?.data?.message || '登入失敗';
  }
};
</script>

<style scoped>
.login-container { max-width: 300px; margin: 50px auto; }
</style>