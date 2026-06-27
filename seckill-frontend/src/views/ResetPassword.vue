<!--
  重置密码页面
  用户忘记密码时，输入用户名和新密码进行重置。
  调用 POST /user/reset-password 接口。
-->
<template>
  <div class="reset-page">
    <div class="reset-card">
      <h2>重置密码</h2>
      <p class="reset-desc">输入用户名和新密码</p>
      <form @submit.prevent="handleReset">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="form.username" placeholder="请输入用户名" required />
        </div>
        <div class="form-group">
          <label>新密码</label>
          <input v-model="form.newPassword" type="password" placeholder="新密码（至少6位）" required />
        </div>
        <button type="submit" class="btn-reset" :disabled="loading">
          {{ loading ? '重置中...' : '重置密码' }}
        </button>
      </form>
      <div class="reset-footer">
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 重置密码页面逻辑
 * - 输入用户名和新密码
 * - 调用 /user/reset-password 接口
 * - 成功后跳转登录页
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const loading = ref(false)
const form = ref({ username: '', newPassword: '' })

const handleReset = async () => {
  if (!form.value.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!form.value.newPassword || form.value.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  loading.value = true
  try {
    await request.post('/user/reset-password', form.value)
    ElMessage.success('密码重置成功，请登录')
    router.push('/login')
  } catch (e) {} finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reset-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e4393c, #ff6b81);
}
.reset-card {
  background: white;
  padding: 40px;
  border-radius: 16px;
  width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}
h2 {
  text-align: center;
  margin-bottom: 8px;
  font-size: 24px;
}
.reset-desc {
  text-align: center;
  color: #999;
  margin-bottom: 24px;
  font-size: 14px;
}
.form-group {
  margin-bottom: 16px;
}
.form-group label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}
.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}
.form-group input:focus {
  border-color: var(--red);
}
.btn-reset {
  width: 100%;
  padding: 12px;
  background: var(--red);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 8px;
}
.btn-reset:disabled {
  opacity: 0.6;
}
.reset-footer {
  text-align: center;
  margin-top: 16px;
}
.reset-footer a {
  color: var(--red);
  font-size: 14px;
}
</style>
