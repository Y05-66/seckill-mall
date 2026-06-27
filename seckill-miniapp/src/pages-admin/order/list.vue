<!--
  管理后台 - 订单管理

  功能：
  1. 订单列表（订单号、用户ID、商品、价格、状态、时间）
  2. 按状态筛选（全部/待支付/已支付/已取消/已超时）
  3. 按订单号筛选

  权限：需管理员（role=1），onMounted 中校验
  数据流：onMounted → getAdminOrderList(params) → orderList
-->
<template>
  <view class="container">
    <view class="title">订单管理</view>

    <!-- 筛选 -->
    <view class="filter-bar">
      <picker :range="statusOptions" range-key="label" @change="onStatusChange">
        <view class="filter-item">{{ currentStatus || '全部状态' }} ▾</view>
      </picker>
    </view>

    <view v-for="order in orderList" :key="order.orderNo" class="order-item">
      <view class="order-row">
        <text class="order-no">{{ order.orderNo }}</text>
        <text class="order-status" :class="'s-' + order.status">{{ order.statusDesc }}</text>
      </view>
      <view class="order-row">
        <text class="order-goods">{{ order.goodsName }}</text>
        <text class="order-price">¥{{ order.seckillPrice }}</text>
      </view>
      <text class="order-time">{{ order.createTime }}</text>
    </view>

    <view v-if="orderList.length === 0" class="empty">暂无数据</view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref, onMounted } from 'vue'
import { getAdminOrderList } from '../../api/admin'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const orderList = ref([])
const statusOptions = [
  { label: '全部状态', value: null },
  { label: '待支付', value: 0 },
  { label: '已支付', value: 1 },
  { label: '已取消', value: 2 },
  { label: '已超时', value: 3 }
]
const currentStatus = ref('')
const filterStatus = ref(null)

import { showToast, navigateBack } from '../../utils/nav'

const loadOrders = async () => {
  const params = {}
  if (filterStatus.value !== null) params.status = filterStatus.value
  try { const res = await getAdminOrderList(params); orderList.value = res.data || [] } catch (e) {}
}

onMounted(() => {
  if (!userStore.isAdmin) { showToast({ title: '无权限', icon: 'none' }); navigateBack(); return }
  loadOrders()
})

const onStatusChange = (e) => {
  const idx = e.detail.value
  currentStatus.value = statusOptions[idx].label
  filterStatus.value = statusOptions[idx].value
  loadOrders()
}
</script>

<style scoped>
.container { padding: 20rpx; }
.title { font-size: 32rpx; font-weight: bold; margin-bottom: 20rpx; }
.filter-bar { display: flex; gap: 16rpx; margin-bottom: 20rpx; }
.filter-item { background: #fff; padding: 12rpx 24rpx; border-radius: 8rpx; font-size: 24rpx; }
.order-item { background: #fff; border-radius: 12rpx; padding: 20rpx; margin-bottom: 12rpx; }
.order-row { display: flex; justify-content: space-between; align-items: center; }
.order-no { font-size: 22rpx; color: #999; }
.order-status { font-size: 22rpx; font-weight: bold; }
.s-0 { color: #e6a23c; }
.s-1 { color: #67c23a; }
.s-2, .s-3 { color: #909399; }
.order-goods { font-size: 26rpx; margin-top: 8rpx; }
.order-price { font-size: 28rpx; color: #f56c6c; font-weight: bold; }
.order-time { font-size: 20rpx; color: #999; margin-top: 6rpx; }
.empty { text-align: center; color: #999; padding: 80rpx 0; }
</style>
