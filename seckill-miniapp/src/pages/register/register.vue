<!--
  注册页

  功能：
  1. 渐变头部装饰
  2. 用户名/密码/昵称/手机号注册表单
  3. 注册成功后自动返回登录页

  数据流：用户输入 → handleRegister() → register API → navigateBack
-->
<template>
  <view class="page">
    <!-- 顶部装饰 -->
    <view class="header">
      <view class="header-bg"></view>
      <view class="header-content">
        <view class="logo">✨</view>
        <view class="app-name">创建账号</view>
        <view class="app-desc">注册后即可参与秒杀</view>
      </view>
    </view>

    <!-- 注册表单 -->
    <view class="form-card">
      <view class="form-group">
        <view class="input-wrap">
          <text class="input-icon">👤</text>
          <input v-model="form.username" placeholder="用户名 (3-20位)" class="input" />
        </view>
      </view>
      <view class="form-group">
        <view class="input-wrap">
          <text class="input-icon">🔒</text>
          <input v-model="form.password" type="password" placeholder="密码 (6-20位)" class="input" />
        </view>
      </view>
      <view class="form-group">
        <view class="input-wrap">
          <text class="input-icon">😊</text>
          <input v-model="form.nickname" placeholder="昵称 (可选)" class="input" />
        </view>
      </view>
      <view class="form-group">
        <view class="input-wrap">
          <text class="input-icon">📱</text>
          <input v-model="form.phone" placeholder="手机号 (可选)" class="input" />
        </view>
      </view>

      <view class="btn btn-primary btn-block" :class="{ 'btn-disabled': loading }" @tap="handleRegister">
        {{ loading ? '注册中...' : '注 册' }}
      </view>

      <view class="form-footer">
        <text class="footer-text">已有账号？</text>
        <text class="footer-link" @tap="goBack">去登录</text>
      </view>
    </view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref } from 'vue'
import { register } from '../../api/user'
import { navigateBack, showToast } from '../../utils/nav'

const loading = ref(false)
const form = ref({ username: '', password: '', nickname: '', phone: '' })

const goBack = () => navigateBack()

const handleRegister = async () => {
  if (!form.value.username || form.value.username.length < 3) {
    showToast({ title: '用户名至少3位', icon: 'none' }); return
  }
  if (!form.value.password || form.value.password.length < 6) {
    showToast({ title: '密码至少6位', icon: 'none' }); return
  }
  loading.value = true
  try {
    await register(form.value)
    showToast({ title: '注册成功', icon: 'success' })
    setTimeout(() => navigateBack(), 1000)
  } catch (e) {} finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f8f9fb; }

.header { position: relative; height: 360rpx; overflow: hidden; }
.header-bg {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(135deg, #7209b7, #4361ee);
  border-radius: 0 0 60rpx 60rpx;
}
.header-content {
  position: relative; z-index: 1;
  display: flex; flex-direction: column; align-items: center;
  padding-top: 80rpx;
}
.logo { font-size: 72rpx; margin-bottom: 16rpx; }
.app-name { font-size: 36rpx; font-weight: 700; color: #fff; }
.app-desc { font-size: 24rpx; color: rgba(255,255,255,0.8); margin-top: 8rpx; }

.form-card {
  background: #fff; margin: -50rpx 32rpx 0; position: relative;
  border-radius: var(--radius-lg); padding: 40rpx 36rpx;
  box-shadow: var(--shadow-lg); z-index: 2;
}

.form-group { margin-bottom: 24rpx; }
.input-wrap {
  display: flex; align-items: center; height: 96rpx;
  padding: 0 28rpx; background: #f3f4f6; border-radius: 16rpx;
  border: 2rpx solid transparent; transition: all 0.2s;
}
.input-wrap:focus-within {
  background: #fff; border-color: rgba(67, 97, 238, 0.3);
}
.input-icon { font-size: 28rpx; margin-right: 16rpx; }
.input { flex: 1; height: 100%; font-size: 28rpx; }

.form-footer { display: flex; justify-content: center; gap: 8rpx; margin-top: 28rpx; }
.footer-text { font-size: 26rpx; color: var(--text-hint); }
.footer-link { font-size: 26rpx; color: var(--primary); font-weight: 600; }
</style>
