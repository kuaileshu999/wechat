<script setup lang="ts">
import { ref } from 'vue';
import axios from 'axios';

const phone = ref('');
const password = ref('');
const message = ref('');
const isLoggedIn = ref(false);

const handleLogin = async () => {
  try {
    const response = await axios.post('http://localhost:8080/api/login', {
      phone: phone.value,
      password: password.value,
    });

    if (response.data.success) {
      isLoggedIn.value = true;
      message.value = response.data.message;
    } else {
      isLoggedIn.value = false;
      message.value = response.data.message;
      alert(message.value);
    }
  } catch (error) {
    console.error('Login error:', error);
    alert('登录失败，请检查后端服务是否启动');
  }
};
</script>

<template>
  <div class="login-container">
    <div v-if="!isLoggedIn">
      <h1>登录</h1>
      <div class="form-item">
        <label>手机号：</label>
        <input v-model="phone" type="text" placeholder="请输入手机号" />
      </div>
      <div class="form-item">
        <label>密码：</label>
        <input v-model="password" type="password" placeholder="请输入密码" />
      </div>
      <button @click="handleLogin">登录</button>
    </div>
    <div v-else>
      <h1>{{ message }}</h1>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 100px auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
  text-align: center;
}

.form-item {
  margin-bottom: 15px;
  text-align: left;
}

.form-item label {
  display: block;
  margin-bottom: 5px;
}

.form-item input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
}

button {
  width: 100%;
  padding: 10px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #3aa876;
}
</style>
