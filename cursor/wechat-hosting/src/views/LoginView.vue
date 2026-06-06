<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <span class="logo">托</span>
        <h1>消息托管系统</h1>
        <p>企微消息托管工作台</p>
      </div>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
          登录
        </el-button>
      </el-form>
      <div class="login-tip">
        测试账号：admin / liting / zhang，密码均为 123456
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api'
import { setUser } from '../utils/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: 'liting',
  password: '123456',
})

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const user = await authApi.login(form)
    setUser(user)
    ElMessage.success(`欢迎，${user.realName}`)
    router.push('/workbench')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e6f4ff 0%, #f0f2f5 50%, #fff7e6 100%);
}

.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.login-brand {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  display: inline-flex;
  width: 56px;
  height: 56px;
  align-items: center;
  justify-content: center;
  background: var(--primary);
  color: #fff;
  font-size: 24px;
  font-weight: 700;
  border-radius: 12px;
  margin-bottom: 16px;
}

.login-brand h1 {
  font-size: 22px;
  margin-bottom: 8px;
}

.login-brand p {
  color: var(--text-secondary);
  font-size: 14px;
}

.login-btn {
  width: 100%;
  margin-top: 8px;
}

.login-tip {
  margin-top: 20px;
  font-size: 12px;
  color: var(--text-secondary);
  text-align: center;
  line-height: 1.6;
}
</style>
