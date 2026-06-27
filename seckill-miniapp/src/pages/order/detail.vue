<!--
  订单详情页

  功能：
  1. 状态栏（根据状态显示不同颜色）
  2. 订单信息卡片（订单号、商品、价格、时间）
  3. 未支付订单显示支付/取消按钮

  路径参数：?orderNo=订单号
  数据流：onMounted → getOrderDetail(orderNo) → order
-->
<template>
  <view class="container" v-if="order">
    <view class="status-bar" :class="'status-' + order.status">
      <text class="status-text">{{ order.statusDesc }}</text>
    </view>

    <view class="info-card">
      <view class="info-row">
        <text class="label">订单号</text>
        <text class="value">{{ order.orderNo }}</text>
      </view>
      <view class="info-row">
        <text class="label">商品名称</text>
        <text class="value">{{ order.goodsName }}</text>
      </view>
      <view class="info-row">
        <text class="label">秒杀价</text>
        <text class="value price">¥{{ order.seckillPrice }}</text>
      </view>
      <view class="info-row">
        <text class="label">下单时间</text>
        <text class="value">{{ order.createTime }}</text>
      </view>
      <view v-if="order.payTime" class="info-row">
        <text class="label">支付时间</text>
        <text class="value">{{ order.payTime }}</text>
      </view>
    </view>

    <view v-if="order.status === 0" class="actions">
      <view class="action-btn cancel" @tap="handleCancel">取消订单</view>
      <view class="action-btn pay" @tap="handlePay">立即支付</view>
    </view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref, onMounted } from 'vue'
import { getOrderDetail, payOrder, cancelOrder } from '../../api/order'
import { showToast, showModal } from '../../utils/nav'

const order = ref(null)

onMounted(async () => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  const orderNo = page.$page?.options?.orderNo || page.options?.orderNo
  if (orderNo) {
    try {
      const res = await getOrderDetail(orderNo)
      order.value = res.data
    } catch (e) {
      showToast({ title: '加载失败', icon: 'none' })
    }
  }
})

const handlePay = async () => {
  const res = await showModal({ title: '确认支付', content: '确认支付该订单？' })
  if (res.confirm) {
    try {
      await payOrder(order.value.orderNo)
      showToast({ title: '支付成功', icon: 'success' })
      order.value.status = 1
      order.value.statusDesc = '已支付'
    } catch (e) {}
  }
}

const handleCancel = async () => {
  const res = await showModal({ title: '确认取消', content: '确认取消该订单？' })
  if (res.confirm) {
    try {
      await cancelOrder(order.value.orderNo)
      showToast({ title: '已取消', icon: 'success' })
      order.value.status = 2
      order.value.statusDesc = '已取消'
    } catch (e) {}
  }
}
</script>

<style scoped>
.container { min-height: 100vh; background: #f5f5f5; }
.status-bar { padding: 40rpx; text-align: center; }
.status-0 { background: #e6a23c; }
.status-1 { background: #67c23a; }
.status-2, .status-3 { background: #909399; }
.status-text { color: #fff; font-size: 36rpx; font-weight: bold; }
.info-card { background: #fff; margin: 20rpx; border-radius: 12rpx; padding: 24rpx; }
.info-row { display: flex; justify-content: space-between; padding: 16rpx 0; border-bottom: 1rpx solid #f0f0f0; }
.info-row:last-child { border-bottom: none; }
.label { color: #999; font-size: 26rpx; }
.value { color: #333; font-size: 26rpx; }
.value.price { color: #f56c6c; font-weight: bold; font-size: 30rpx; }
.actions { display: flex; gap: 20rpx; padding: 30rpx; }
.action-btn { flex: 1; text-align: center; padding: 22rpx; border-radius: 12rpx; font-size: 28rpx; }
.action-btn.cancel { background: #f5f5f5; color: #666; }
.action-btn.pay { background: #409eff; color: #fff; }
</style>
