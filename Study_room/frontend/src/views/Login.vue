<template>
  <div class="login-page">
    <div class="login-card">
      <h1>自习室管理系统</h1>
      <p class="subtitle">学科培训机构收费与消课管理</p>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large"
                    prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleLogin">
          登录
        </el-button>
      </el-form>
      <p class="tip">默认账号：admin / Admin@123</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await request.post('/auth/login', form)
    userStore.setLogin(data)
    ElMessage.success('登录成功')
    const redirect = router.currentRoute.value.query.redirect || '/employee'
    router.push(String(redirect))
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-card h1 {
  margin: 0 0 8px;
  text-align: center;
  font-size: 24px;
  color: #303133;
}

.subtitle {
  text-align: center;
  color: #909399;
  margin: 0 0 32px;
  font-size: 14px;
}

.tip {
  margin-top: 16px;
  text-align: center;
  color: #909399;
  font-size: 12px;
}
</style>
