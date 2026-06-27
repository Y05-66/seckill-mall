<!--
  OrderItem.vue - 订单列表项组件

  功能：展示订单号、商品名称、价格、状态标签。
  未支付订单显示"取消"和"支付"操作按钮。

  Props:
    - order {Object} - 订单数据 { orderNo, goodsName, seckillPrice, status, statusDesc, createTime }

  Events:
    - pay(order) - 点击"支付"按钮
    - cancel(order) - 点击"取消"按钮

  用法：<OrderItem :order="order" @pay="handlePay" @cancel="handleCancel" />
-->
<template>
  <view class="order-card">
    <view class="order-header">
      <text class="order-no">{{ order.orderNo }}</text>
      <view class="status-tag" :class="'st-' + order.status">
        {{ order.statusDesc }}
      </view>
    </view>
    <view class="order-body">
      <view class="order-info">
        <text class="goods-name">{{ order.goodsName }}</text>
        <text class="order-time">{{ order.createTime }}</text>
      </view>
      <view class="order-price">¥{{ order.seckillPrice }}</view>
    </view>
    <view v-if="order.status === 0" class="order-actions">
      <view class="action-btn ghost" @tap="$emit('cancel', order)">取消订单</view>
      <view class="action-btn primary" @tap="$emit('pay', order)">立即支付</view>
    </view>
  </view>
</template>

<script setup>
defineProps({ order: { type: Object, required: true } })
defineEmits(['pay', 'cancel'])
</script>

<style scoped>
.order-card {
  background: var(--bg-card); border-radius: var(--radius);
  box-shadow: var(--shadow); margin-bottom: 20rpx; overflow: hidden;
}

.order-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20rpx 24rpx; border-bottom: 1rpx solid #f3f4f6;
}
.order-no { font-size: 22rpx; color: var(--text-hint); }
.status-tag {
  padding: 4rpx 16rpx; border-radius: 8rpx;
  font-size: 22rpx; font-weight: 600;
}
.st-0 { background: var(--warning-light); color: #b8860b; }
.st-1 { background: var(--success-light); color: var(--success); }
.st-2 { background: #f3f4f6; color: var(--text-hint); }
.st-3 { background: #f3f4f6; color: var(--text-hint); }

.order-body {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20rpx 24rpx;
}
.order-info { flex: 1; }
.goods-name { font-size: 28rpx; font-weight: 600; color: var(--text-primary); display: block; }
.order-time { font-size: 22rpx; color: var(--text-hint); display: block; margin-top: 6rpx; }
.order-price { font-size: 34rpx; font-weight: 700; color: var(--danger); }

.order-actions {
  display: flex; justify-content: flex-end; gap: 16rpx;
  padding: 16rpx 24rpx; border-top: 1rpx solid #f3f4f6;
}
.action-btn {
  padding: 10rpx 28rpx; border-radius: 20rpx;
  font-size: 24rpx; font-weight: 600;
}
.action-btn.ghost { background: #f3f4f6; color: var(--text-secondary); }
.action-btn.primary {
  background: linear-gradient(135deg, #4361ee, #3a56d4); color: #fff;
}
</style>
