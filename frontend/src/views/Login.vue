<template>
  <div class="login-container">
    <h2>私人聊天系统 - 登录</h2>
    <el-form :model="form" label-width="80px" style="width: 400px; margin: 40px auto;">
      <el-form-item label="账号/手机号">
        <el-input v-model="form.account" placeholder="请输入账号或手机号" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" placeholder="请输入密码" @keyup.enter="handleLogin" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleLogin" style="width: 100%;">登录</el-button>
      </el-form-item>
      <el-form-item>
        <div style="text-align: center;">
          <router-link to="/register">还没有账号？去注册</router-link>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '../api'

const router = useRouter()
const form = ref({
  account: '',
  password: ''
})

const handleLogin = async () => {
  try {
    const res = await userApi.login(form.value.account, form.value.password)
    if (res.data.code === 200) {
      localStorage.setItem('user', JSON.stringify(res.data.data))
      ElMessage.success('登录成功')
      router.push('/chat')
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('登录失败')
  }
}
</script>

<style scoped>
.login-container {
  padding: 60px 0;
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 20px;
}
</style>
