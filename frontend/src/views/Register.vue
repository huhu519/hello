<template>
  <div class="register-container">
    <h2>私人聊天系统 - 注册</h2>
    <el-form :model="form" label-width="80px" style="width: 400px; margin: 40px auto;">
      <el-form-item label="手机号">
        <el-input v-model="form.phone" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" placeholder="请输入密码" />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="邀请码">
        <el-input v-model="form.inviteCode" placeholder="请输入邀请码" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleRegister" style="width: 100%;">注册</el-button>
      </el-form-item>
      <el-form-item>
        <div style="text-align: center;">
          <router-link to="/login">已有账号？去登录</router-link>
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
  phone: '',
  password: '',
  nickname: '',
  avatar: '',
  inviteCode: ''
})

const handleRegister = async () => {
  try {
    const res = await userApi.register(form.value.phone, form.value.password, form.value.nickname, form.value.avatar, form.value.inviteCode)
    if (res.data.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('注册失败')
  }
}
</script>

<style scoped>
.register-container {
  padding: 60px 0;
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 20px;
}
</style>
