<!--
  管理后台 - 仪表盘

  功能：
  1. 订单统计卡片（总数、今日、已支付、营收、待支付、已取消、已超时）
  2. 快捷导航入口（商品管理、秒杀管理、订单管理、用户管理）

  权限：需管理员（role=1），onMounted 中校验
  数据流：onMounted → getOrderStatistics() → stats
-->
<template>
  <view class="container">
    <view class="stats-grid">
      <view class="stat-card">
        <text class="stat-value">{{ stats.totalOrders || 0 }}</text>
        <text class="stat-label">订单总数</text>
      </view>
      <view class="stat-card">
        <text class="stat-value warning">{{ stats.todayOrders || 0 }}</text>
        <text class="stat-label">今日订单</text>
      </view>
      <view class="stat-card">
        <text class="stat-value success">{{ stats.paidCount || 0 }}</text>
        <text class="stat-label">已支付</text>
      </view>
      <view class="stat-card">
        <text class="stat-value danger">¥{{ stats.totalRevenue || '0.00' }}</text>
        <text class="stat-label">总营收</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ stats.unpaidCount || 0 }}</text>
        <text class="stat-label">待支付</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ stats.cancelledCount || 0 }}</text>
        <text class="stat-label">已取消</text>
      </view>
      <view class="stat-card">
        <text class="stat-value">{{ stats.timeOutCount || 0 }}</text>
        <text class="stat-label">已超时</text>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-nav">
      <view class="nav-item" @tap="goPage('/pages-admin/goods/list')">
        <text class="nav-icon">📦</text><text class="nav-text">商品管理</text>
      </view>
      <view class="nav-item" @tap="goPage('/pages-admin/seckill/list')">
        <text class="nav-icon">⚡</text><text class="nav-text">秒杀管理</text>
      </view>
      <view class="nav-item" @tap="goPage('/pages-admin/order/list')">
        <text class="nav-icon">📋</text><text class="nav-text">订单管理</text>
      </view>
      <view class="nav-item" @tap="goPage('/pages-admin/user/list')">
        <text class="nav-icon">👥</text><text class="nav-text">用户管理</text>
      </view>
    </view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref, onMounted } from 'vue'
import { getOrderStatistics } from '../../api/admin'
import { useUserStore } from '../../store/user'
import { navigateTo, navigateBack, showToast } from '../../utils/nav'

const userStore = useUserStore()
const stats = ref({})
const goPage = (url) => navigateTo(url)

onMounted(async () => {
  if (!userStore.isAdmin) {
    showToast({ title: '无权限', icon: 'none' })
    navigateBack()
    return
  }
  try {
    const res = await getOrderStatistics()
    stats.value = res.data || {}
  } catch (e) {}
})
</script>

<style scoped>
.container { padding: 20rpx; }
.stats-grid { display: flex; flex-wrap: wrap; gap: 16rpx; }
.stat-card {
  width: calc(50% - 8rpx); background: #fff; border-radius: 12rpx;
  padding: 24rpx; text-align: center;
}
.stat-value { font-size: 36rpx; font-weight: bold; color: #333; display: block; }
.stat-value.success { color: #67c23a; }
.stat-value.warning { color: #e6a23c; }
.stat-value.danger { color: #f56c6c; }
.stat-label { font-size: 24rpx; color: #999; margin-top: 8rpx; display: block; }
.quick-nav { display: flex; flex-wrap: wrap; gap: 16rpx; margin-top: 30rpx; }
.nav-item {
  width: calc(50% - 8rpx); background: #fff; border-radius: 12rpx;
  padding: 30rpx; text-align: center;
}
.nav-icon { font-size: 48rpx; display: block; margin-bottom: 10rpx; }
.nav-text { font-size: 26rpx; color: #333; }
</style>
