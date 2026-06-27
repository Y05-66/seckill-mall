<template>
  <view class="page">
    <!-- 用户头部 -->
    <view class="header">
      <view class="header-content">
        <view class="avatar" @tap="userStore.isLoggedIn && goSettings()">
          <text class="avatar-text">{{ userStore.isLoggedIn ? userStore.nickname[0] : '?' }}</text>
        </view>
        <view v-if="userStore.isLoggedIn" class="user-info" @tap="goSettings">
          <text class="nickname">{{ userStore.nickname }}</text>
          <text class="username">@{{ userStore.userInfo.username }}</text>
          <view v-if="userStore.isAdmin" class="admin-tag">管理员</view>
        </view>
        <view v-else class="user-info" @tap="goLogin">
          <text class="nickname">点击登录</text>
          <text class="username">登录后享受更多权益</text>
        </view>
      </view>
    </view>

    <!-- 订单快捷入口 -->
    <view class="quick-row">
      <view class="quick-item" @tap="goOrders()">
        <text class="qi-icon">📋</text>
        <text class="qi-text">全部订单</text>
      </view>
      <view class="quick-item" @tap="goOrders(0)">
        <text class="qi-icon">💳</text>
        <text class="qi-text">待支付</text>
      </view>
      <view class="quick-item" @tap="goOrders(1)">
        <text class="qi-icon">✅</text>
        <text class="qi-text">已支付</text>
      </view>
      <view class="quick-item" @tap="goOrders(2)">
        <text class="qi-icon">❌</text>
        <text class="qi-text">已取消</text>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-card">
      <view class="menu-item" @tap="goSettings">
        <text class="mi-icon">⚙️</text>
        <text class="mi-text">设置</text>
        <text class="mi-arrow">›</text>
      </view>
      <view class="menu-item" @tap="goOrders">
        <text class="mi-icon">📋</text>
        <text class="mi-text">我的订单</text>
        <text class="mi-arrow">›</text>
      </view>
      <view class="menu-item" @tap="goCart">
        <text class="mi-icon">🛒</text>
        <text class="mi-text">购物车</text>
        <text class="mi-arrow">›</text>
      </view>
      <view class="menu-item" @tap="goFavorites">
        <text class="mi-icon">❤️</text>
        <text class="mi-text">我的收藏</text>
        <text class="mi-arrow">›</text>
      </view>
      <view class="menu-item" @tap="goCoupons">
        <text class="mi-icon">🎫</text>
        <text class="mi-text">优惠券</text>
        <text class="mi-arrow">›</text>
      </view>
      <view class="menu-item" @tap="goNotifications">
        <text class="mi-icon">🔔</text>
        <text class="mi-text">消息通知</text>
        <text class="mi-arrow">›</text>
      </view>
      <view class="menu-item" @tap="goAddress">
        <text class="mi-icon">📍</text>
        <text class="mi-text">收货地址</text>
        <text class="mi-arrow">›</text>
      </view>
      <view v-if="userStore.isAdmin" class="menu-item" @tap="goAdmin">
        <text class="mi-icon">⚙️</text>
        <text class="mi-text">管理后台</text>
        <text class="mi-arrow">›</text>
      </view>
    </view>

    <!-- 退出 -->
    <view v-if="userStore.isLoggedIn" class="logout-wrap">
      <view class="logout-btn" @tap="handleLogout">退出登录</view>
    </view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { useUserStore } from '../../store/user'
import { navigateTo, switchTab, showModal, showToast } from '../../utils/nav'

const userStore = useUserStore()

const goLogin = () => navigateTo('/pages/login/login')
const goSettings = () => navigateTo('/pages/settings/index')
const goOrders = (status) => {
  // tabBar 页面不能用 navigateTo，通过全局事件传递 tab 参数
  if (status !== undefined && status !== null) {
    uni.$emit('switchOrderTab', status)
  }
  switchTab('/pages/order/list')
}
const goCart = () => navigateTo('/pages/cart/index')
const goFavorites = () => navigateTo('/pages/favorites/index')
const goCoupons = () => navigateTo('/pages/coupons/index')
const goNotifications = () => navigateTo('/pages/notifications/index')
const goAddress = () => navigateTo('/pages/address/index')
const goAdmin = () => navigateTo('/pages-admin/dashboard/index')

const handleLogout = async () => {
  const res = await showModal({ title: '确认退出', content: '退出后需要重新登录', confirmColor: '#e4393c' })
  if (res.confirm) {
    userStore.logout()
    showToast({ title: '已退出', icon: 'success' })
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f5f5; }

.header {
  background: linear-gradient(135deg, #e4393c, #ff6b81);
  padding: 60rpx 32rpx 32rpx;
}
.header-content { display: flex; align-items: center; gap: 20rpx; }
.avatar {
  width: 88rpx; height: 88rpx; border-radius: 50%;
  background: rgba(255,255,255,0.25);
  display: flex; align-items: center; justify-content: center;
  border: 3rpx solid rgba(255,255,255,0.4); flex-shrink: 0;
}
.avatar-text { font-size: 36rpx; color: #fff; font-weight: 700; }
.user-info { flex: 1; }
.nickname { font-size: 30rpx; font-weight: 700; color: #fff; display: block; }
.username { font-size: 22rpx; color: rgba(255,255,255,0.8); display: block; margin-top: 4rpx; }
.admin-tag {
  display: inline-block; margin-top: 6rpx;
  padding: 2rpx 12rpx; background: rgba(255,255,255,0.25);
  border-radius: 10rpx; font-size: 18rpx; color: #fff;
}

.quick-row {
  display: flex; justify-content: space-around; background: #fff;
  margin: 20rpx 24rpx; border-radius: 12rpx; padding: 24rpx 0;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}
.quick-item { display: flex; flex-direction: column; align-items: center; gap: 6rpx; }
.qi-icon { font-size: 36rpx; }
.qi-text { font-size: 20rpx; color: #666; }

.menu-card {
  background: #fff; margin: 16rpx 24rpx; border-radius: 12rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06); overflow: hidden;
}
.menu-item {
  display: flex; align-items: center; gap: 14rpx;
  padding: 24rpx 20rpx; border-bottom: 1rpx solid #f5f5f5;
}
.menu-item:last-child { border-bottom: none; }
.menu-item:active { background: #fafafa; }
.mi-icon { font-size: 32rpx; }
.mi-text { flex: 1; font-size: 26rpx; color: #222; font-weight: 500; }
.mi-arrow { font-size: 28rpx; color: #999; }

.logout-wrap { padding: 32rpx 24rpx; }
.logout-btn {
  height: 80rpx; line-height: 80rpx; text-align: center;
  background: #fff; border-radius: 12rpx;
  font-size: 26rpx; color: #e4393c; font-weight: 600;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}
.logout-btn:active { background: #fff0f0; }

</style>
