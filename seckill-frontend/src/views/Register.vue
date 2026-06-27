<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h2>免费注册</h2>
      </div>
      <form @submit.prevent="handleRegister" class="login-form">
        <div class="form-group">
          <input v-model="form.username" placeholder="用户名（3-20个字符）" />
        </div>
        <div class="form-group">
          <input v-model="form.password" type="password" placeholder="密码（至少6位）" />
        </div>
        <div class="form-group">
          <input v-model="form.nickname" placeholder="昵称" />
        </div>
        <div class="form-group">
          <input v-model="form.phone" placeholder="手机号（选填）" />
        </div>
        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? '注册中...' : '注 册' }}
        </button>
      </form>
      <div class="login-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { register } from '../api/user'

const router = useRouter()
const loading = ref(false)
const form = ref({ username: '', password: '', nickname: '', phone: '' })

const handleRegister = async () => {
  if (!form.value.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (form.value.username.length < 3 || form.value.username.length > 20) {
    ElMessage.warning('用户名长度应为3-20个字符')
    return
  }
  if (!form.value.password) {
    ElMessage.warning('请输入密码')
    return
  }
  if (form.value.password.length < 6) {
    ElMessage.warning('密码长度不能少于6位')
    return
  }
  if (!form.value.nickname) {
    ElMessage.warning('请输入昵称')
    return
  }
  loading.value = true
  try {
    await register(form.value)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    // 错误已由 request.js 拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { display: flex; align-items: center; justify-content: center; min-height: calc(100vh - 400px); }
.login-card { background: var(--white); width: 400px; border-radius: var(--radius); box-shadow: var(--shadow); padding: 40px 36px; }
.login-header { text-align: center; margin-bottom: 32px; }
.login-header h2 { font-size: 24px; font-weight: 700; }
.form-group { margin-bottom: 16px; }
.form-group input { width: 100%; padding: 12px 14px; border: 1px solid var(--border); border-radius: 6px; font-size: 14px; outline: none; }
.form-group input:focus { border-color: var(--red); }
.login-btn { width: 100%; padding: 12px; border: none; border-radius: 6px; background: var(--red); color: white; font-size: 16px; font-weight: 600; cursor: pointer; margin-top: 8px; }
.login-btn:hover { background: var(--red-dark); }
.login-btn:disabled { opacity: 0.6; }
.login-footer { text-align: center; margin-top: 20px; font-size: 13px; color: var(--text-sub); }
.login-footer a { color: var(--red); font-weight: 600; }
</style>
