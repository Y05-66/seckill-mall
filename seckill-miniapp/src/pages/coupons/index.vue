<template>
  <view class="page">
    <view class="section-title">可领取优惠券</view>
    <view v-if="available.length > 0">
      <view class="coupon-card" v-for="item in available" :key="item.id">
        <view class="coupon-left">
          <text class="coupon-value">
            {{ item.type === 1 ? '¥' + item.value : (item.value * 10) + '折' }}
          </text>
          <text class="coupon-cond">{{ item.minAmount > 0 ? '满' + item.minAmount + '可用' : '无门槛' }}</text>
        </view>
        <view class="coupon-right">
          <text class="coupon-name">{{ item.name }}</text>
          <view class="coupon-btn" @tap="handleClaim(item)">领取</view>
        </view>
      </view>
    </view>
    <view v-else class="empty">暂无可领取的优惠券</view>

    <view class="section-title" style="margin-top: 40rpx;">我的优惠券</view>
    <view v-if="myCoupons.length > 0">
      <view class="coupon-card" v-for="item in myCoupons" :key="item.id" :class="{ used: item.status === 1 }">
        <view class="coupon-left">
          <text class="coupon-value">
            {{ item.type === 1 ? '¥' + item.value : (item.value * 10) + '折' }}
          </text>
          <text class="coupon-cond">{{ item.minAmount > 0 ? '满' + item.minAmount + '可用' : '无门槛' }}</text>
        </view>
        <view class="coupon-right">
          <text class="coupon-name">{{ item.couponName }}</text>
          <text class="coupon-status">{{ item.status === 0 ? '未使用' : '已使用' }}</text>
        </view>
      </view>
    </view>
    <view v-else class="empty">暂无优惠券</view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAvailableCoupons, claimCoupon, getMyCoupons } from '../../api/coupon'
import { showToast } from '../../utils/nav'

const available = ref([])
const myCoupons = ref([])

onShow(async () => {
  try {
    const [a, m] = await Promise.all([getAvailableCoupons(), getMyCoupons()])
    available.value = a.data || []
    myCoupons.value = m.data || []
  } catch (e) {}
})

const handleClaim = async (coupon) => {
  try {
    await claimCoupon(coupon.id)
    showToast({ title: '领取成功', icon: 'success' })
    const [a, m] = await Promise.all([getAvailableCoupons(), getMyCoupons()])
    available.value = a.data || []
    myCoupons.value = m.data || []
  } catch (e) {}
}
</script>

<style scoped>
.page { background: #f5f5f5; min-height: 100vh; padding: 20rpx; }
.section-title { font-size: 30rpx; font-weight: 700; margin-bottom: 16rpx; }

.coupon-card {
  display: flex; background: #fff; border-radius: 16rpx;
  overflow: hidden; margin-bottom: 16rpx;
  border-left: 8rpx solid #e4393c;
}
.coupon-card.used { opacity: 0.6; border-left-color: #ccc; }
.coupon-left {
  width: 200rpx; padding: 20rpx; text-align: center;
  background: #fff0f0; display: flex; flex-direction: column;
  justify-content: center;
}
.coupon-value { font-size: 36rpx; color: #e4393c; font-weight: 800; }
.coupon-cond { font-size: 22rpx; color: #999; margin-top: 4rpx; }
.coupon-right { flex: 1; padding: 20rpx; display: flex; flex-direction: column; justify-content: center; }
.coupon-name { font-size: 28rpx; font-weight: 600; margin-bottom: 12rpx; }
.coupon-btn {
  align-self: flex-start; padding: 8rpx 24rpx;
  background: #e4393c; color: #fff; border-radius: 8rpx; font-size: 24rpx;
}
.coupon-status { font-size: 24rpx; color: #999; }

.empty { text-align: center; padding: 60rpx 0; color: #999; font-size: 26rpx; }
</style>
