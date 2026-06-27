<!--
  SeckillCard.vue - 秒杀商品卡片组件

  功能：展示秒杀商品图片、价格、折扣、进度条、倒计时、抢购按钮。
  根据 status 显示不同状态：
  - 0=未开始（灰色标签，距开始倒计时）
  - 1=进行中（红色标签，距结束倒计时，可抢购）
  - 2=已结束（灰色标签，按钮禁用）

  Props:
    - item {Object} - 秒杀商品数据 { id, goodsName, seckillPrice, goodsPrice, stockCount, status, remainSeconds }

  Events:
    - seckill(item) - 点击"立即抢购"时触发
    - click(item) - 点击卡片时触发

  用法：<SeckillCard :item="goods" @seckill="handleSeckill" @click="goDetail" />
-->
<template>
  <view class="seckill-card" @tap="$emit('click', item)">
    <view class="img-wrap">
      <image :src="getFullImageUrl(item.goodsImg)" mode="aspectFill" class="card-img" @error="handleImageError" />
      <view class="status-badge" :class="'badge-' + item.status">
        {{ ['未开始', '进行中', '已结束'][item.status] }}
      </view>
    </view>
    <view class="card-body">
      <view class="card-name">{{ item.goodsName }}</view>
      <view class="price-row">
        <view class="seckill-price">
          <text class="price-symbol">¥</text>
          <text class="price-value">{{ item.seckillPrice }}</text>
        </view>
        <text class="original-price">¥{{ item.goodsPrice }}</text>
        <view class="discount-tag">{{ discount }}折</view>
      </view>
      <view class="progress-row">
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: soldPercent + '%' }"></view>
        </view>
        <text class="progress-text">已抢{{ soldPercent }}%</text>
      </view>
      <view class="bottom-row">
        <view class="stock-info">
          <text class="stock-num">剩余 {{ item.stockCount }} 件</text>
        </view>
        <view v-if="item.status !== 2" class="countdown-wrap">
          <Countdown
            :seconds="item.remainSeconds"
            :type="item.status === 0 ? 'start' : 'end'"
          />
        </view>
      </view>
      <view
        class="seckill-btn"
        :class="{ 'btn-disabled': item.status !== 1, 'btn-danger': item.status === 1 }"
        @tap.stop="item.status === 1 && $emit('seckill', item)"
      >
        {{ item.status === 0 ? '⏰ 即将开始' : item.status === 2 ? '已结束' : '🔥 立即抢购' }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import Countdown from './Countdown.vue'
import { handleImageError, getFullImageUrl } from '../utils/image'

const props = defineProps({ item: { type: Object, required: true } })
defineEmits(['seckill', 'click'])

const discount = computed(() => {
  if (!props.item.goodsPrice || !props.item.seckillPrice) return '--'
  return (Number(props.item.seckillPrice) / Number(props.item.goodsPrice) * 10).toFixed(1)
})

const soldPercent = computed(() => {
  const item = props.item
  if (!item.goodsStock || !item.stockCount) return 0
  const sold = item.goodsStock - item.stockCount
  return Math.min(99, Math.round((sold / item.goodsStock) * 100))
})
</script>

<style scoped>
.seckill-card {
  background: var(--bg-card);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  overflow: hidden;
  margin-bottom: 20rpx;
}
.seckill-card:active { transform: scale(0.99); }

.img-wrap { position: relative; }
.card-img { width: 100%; height: 320rpx; display: block; }
.status-badge {
  position: absolute; top: 16rpx; right: 16rpx;
  padding: 6rpx 20rpx; border-radius: 20rpx;
  font-size: 22rpx; font-weight: 600; color: #fff;
}
.badge-0 { background: rgba(107, 114, 128, 0.85); }
.badge-1 { background: rgba(239, 71, 111, 0.9); }
.badge-2 { background: rgba(107, 114, 128, 0.6); }

.card-body { padding: 24rpx; }
.card-name { font-size: 30rpx; font-weight: 600; color: var(--text-primary); margin-bottom: 16rpx; }

.price-row { display: flex; align-items: baseline; gap: 12rpx; margin-bottom: 16rpx; }
.seckill-price { display: flex; align-items: baseline; }
.price-symbol { font-size: 24rpx; color: var(--danger); font-weight: 600; }
.price-value { font-size: 44rpx; color: var(--danger); font-weight: 700; }
.original-price { font-size: 24rpx; color: var(--text-hint); text-decoration: line-through; }
.discount-tag {
  padding: 2rpx 12rpx; background: var(--danger-light); color: var(--danger);
  font-size: 20rpx; font-weight: 600; border-radius: 6rpx;
}

.progress-row { display: flex; align-items: center; gap: 12rpx; margin-bottom: 16rpx; }
.progress-bar { flex: 1; height: 12rpx; background: #f3f4f6; border-radius: 6rpx; overflow: hidden; }
.progress-fill {
  height: 100%; border-radius: 6rpx;
  background: linear-gradient(90deg, #ef476f, #f78da7);
  transition: width 0.3s;
}
.progress-text { font-size: 20rpx; color: var(--text-hint); white-space: nowrap; }

.bottom-row {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx;
}
.stock-num { font-size: 24rpx; color: var(--text-secondary); }

.seckill-btn {
  display: flex; align-items: center; justify-content: center;
  height: 80rpx; border-radius: 16rpx;
  font-size: 28rpx; font-weight: 600; color: #fff;
}
.btn-danger {
  background: linear-gradient(135deg, #ef476f, #d63d63);
  box-shadow: 0 8rpx 20rpx rgba(239, 71, 111, 0.3);
}
.btn-disabled { background: #e5e7eb; color: #9ca3af; }
</style>
