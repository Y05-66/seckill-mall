<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <span class="login-icon">⚡</span>
        <h2>账户登录</h2>
      </div>
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="form.username" placeholder="请输入用户名" autocomplete="username" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="请输入密码" autocomplete="current-password" @keyup.enter="handleLogin" />
        </div>
        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>
      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
        <span class="sep">|</span>
        <router-link to="/reset-password">忘记密码</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { login } from '../api/user'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = ref({ username: '', password: '' })

const handleLogin = async () => {
  if (!form.value.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!form.value.password) {
    ElMessage.warning('请输入密码')
    return
  }
  loading.value = true
  try {
    const r = await login(form.value)
    userStore.setUser(r.data)
    ElMessage.success('登录成功')
    // 登录后跳转到之前的目标页面
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    // 错误已由 request.js 拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { display: flex; align-items: center; justify-content: center; min-height: calc(100vh - 350px); }
.login-card { background: var(--white); width: 400px; border-radius: var(--radius); box-shadow: var(--shadow-md); padding: 40px 36px; }
.login-header { text-align: center; margin-bottom: 32px; }
.login-icon { font-size: 48px; display: block; margin-bottom: 8px; }
.login-header h2 { font-size: 24px; font-weight: 700; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: var(--text-666); margin-bottom: 6px; }
.form-group input { width: 100%; padding: 12px 14px; border: 1px solid var(--border); border-radius: 6px; font-size: 14px; outline: none; transition: border-color 0.2s; }
.form-group input:focus { border-color: var(--red); }
.login-btn { width: 100%; padding: 12px; border: none; border-radius: 6px; background: var(--red); color: white; font-size: 16px; font-weight: 600; cursor: pointer; margin-top: 8px; }
.login-btn:hover { background: var(--red-dark); }
.login-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.login-footer { text-align: center; margin-top: 20px; font-size: 13px; color: var(--text-666); }
.login-footer a { color: var(--red); font-weight: 600; }
</style>
