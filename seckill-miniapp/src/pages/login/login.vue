<template>
  <view class="page">
    <!-- 顶部 -->
    <view class="header">
      <view class="header-bg"></view>
      <view class="header-content">
        <view class="logo">⚡</view>
        <view class="app-name">秒杀商城</view>
        <view class="app-desc">限时抢购，低至1折</view>
      </view>
    </view>

    <!-- 表单 -->
    <view class="form-card">
      <view class="form-title">欢迎回来</view>
      <view class="form-sub">登录你的账号</view>

      <view class="form-group">
        <view class="input-wrap" :class="{ focus: focusUser }">
          <text class="input-icon">👤</text>
          <input v-model="form.username" placeholder="请输入用户名" class="input"
            @focus="focusUser = true" @blur="focusUser = false" />
        </view>
      </view>

      <view class="form-group">
        <view class="input-wrap" :class="{ focus: focusPass }">
          <text class="input-icon">🔒</text>
          <input v-model="form.password" type="password" placeholder="请输入密码" class="input"
            @focus="focusPass = true" @blur="focusPass = false" />
        </view>
      </view>

      <view class="btn" :class="{ disabled: loading }" @tap="handleLogin">
        {{ loading ? '登录中...' : '登 录' }}
      </view>

      <view class="form-footer">
        <text class="footer-text">还没有账号？</text>
        <text class="footer-link" @tap="goRegister">立即注册</text>
      </view>
    </view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref } from 'vue'
import { login } from '../../api/user'
import { useUserStore } from '../../store/user'
import { navigateTo, navigateBack, switchTab, showToast } from '../../utils/nav'

const userStore = useUserStore()
const loading = ref(false)
const form = ref({ username: '', password: '' })
const focusUser = ref(false)
const focusPass = ref(false)

const goRegister = () => navigateTo('/pages/register/register')

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    showToast({ title: '请输入用户名和密码', icon: 'none' }); return
  }
  loading.value = true
  try {
    const res = await login(form.value)
    userStore.setUser(res.data)
    showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      const pages = getCurrentPages()
      if (pages.length > 1) navigateBack()
      else switchTab('/pages/index/index')
    }, 1000)
  } catch (e) {} finally { loading.value = false }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; }

.header { position: relative; height: 360rpx; overflow: hidden; }
.header-bg {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(135deg, #e4393c, #ff6b81);
  border-radius: 0 0 60rpx 60rpx;
}
.header-content {
  position: relative; z-index: 1;
  display: flex; flex-direction: column; align-items: center;
  padding-top: 80rpx;
}
.logo { font-size: 72rpx; margin-bottom: 12rpx; }
.app-name { font-size: 36rpx; font-weight: 700; color: #fff; }
.app-desc { font-size: 24rpx; color: rgba(255,255,255,0.8); margin-top: 8rpx; }

.form-card {
  background: #fff; margin: -60rpx 32rpx 0; position: relative;
  border-radius: 24rpx; padding: 48rpx 36rpx;
  box-shadow: 0 4rpx 24rpx rgba(0,0,0,0.1); z-index: 2;
}
.form-title { font-size: 36rpx; font-weight: 700; color: #222; }
.form-sub { font-size: 26rpx; color: #999; margin-top: 8rpx; margin-bottom: 40rpx; }

.form-group { margin-bottom: 28rpx; }
.input-wrap {
  display: flex; align-items: center; height: 96rpx;
  padding: 0 24rpx; background: #f5f5f5; border-radius: 12rpx;
  border: 2rpx solid transparent;
}
.input-wrap.focus { background: #fff; border-color: rgba(228,57,60,0.3); }
.input-icon { font-size: 32rpx; margin-right: 16rpx; }
.input { flex: 1; height: 100%; font-size: 28rpx; }

.btn {
  margin-top: 16rpx; height: 96rpx; line-height: 96rpx;
  text-align: center; background: #e4393c; color: #fff;
  font-size: 30rpx; font-weight: 600; border-radius: 12rpx;
}
.btn.disabled { opacity: 0.6; }

.form-footer { display: flex; justify-content: center; gap: 8rpx; margin-top: 32rpx; }
.footer-text { font-size: 26rpx; color: #999; }
.footer-link { font-size: 26rpx; color: #e4393c; font-weight: 600; }
</style>
