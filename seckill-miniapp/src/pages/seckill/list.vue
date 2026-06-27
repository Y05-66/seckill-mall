<template>
  <view class="page">
    <!-- 秒杀头部 -->
    <view class="header">
      <view class="header-bg"></view>
      <view class="header-content">
        <view class="header-left">
          <text class="header-title">🔥 限时秒杀</text>
          <text class="header-sub">全场低至1折</text>
        </view>
        <view class="header-right">
          <text class="cd-label">距结束</text>
          <view class="cd-box">
            <text class="cd-num">{{ hh }}</text>
            <text class="cd-sep">:</text>
            <text class="cd-num">{{ mm }}</text>
            <text class="cd-sep">:</text>
            <text class="cd-num">{{ ss }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 秒杀列表 -->
    <view class="list">
      <view class="sk-card" v-for="item in seckillList" :key="item.id" @tap="goDetail(item)">
        <image :src="getFullImageUrl(item.goodsImg)" mode="aspectFill" class="sk-img" />
        <view class="sk-info">
          <text class="sk-name">{{ item.goodsName }}</text>
          <view class="sk-price-row">
            <view class="sk-price">
              <text class="sk-yen">¥</text>
              <text class="sk-now">{{ item.seckillPrice }}</text>
            </view>
            <text class="sk-old">¥{{ item.goodsPrice }}</text>
            <text class="sk-discount">{{ getDiscount(item) }}折</text>
          </view>
          <view class="sk-progress">
            <view class="sk-bar">
              <view class="sk-bar-fill" :style="{ width: getProgress(item) + '%' }"></view>
            </view>
            <text class="sk-pct">已抢{{ getProgress(item) }}%</text>
          </view>
          <view class="sk-btn" :class="{ active: item.status === 1 }" @tap.stop="goDetail(item)">
            {{ item.status === 0 ? '未开始' : item.status === 1 ? '立即抢购' : '已结束' }}
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-if="!loading && seckillList.length === 0" class="empty">
      <text class="empty-icon">⚡</text>
      <text class="empty-text">暂无秒杀活动</text>
    </view>

    <!-- 加载中 -->
    <view v-if="loading" class="loading">
      <text class="loading-text">加载中...</text>
    </view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getSeckillGoodsList } from '../../api/goods'
import { navigateTo } from '../../utils/nav'
import { getFullImageUrl } from '../../utils/image'

const seckillList = ref([])
const loading = ref(true)
let refreshTimer = null

const countdown = ref(0)
const hh = computed(() => String(Math.floor(countdown.value / 3600)).padStart(2, '0'))
const mm = computed(() => String(Math.floor((countdown.value % 3600) / 60)).padStart(2, '0'))
const ss = computed(() => String(countdown.value % 60).padStart(2, '0'))
let cdTimer = null

const loadList = async () => {
  try {
    const r = await getSeckillGoodsList()
    seckillList.value = r.data || []
    // 使用实际秒杀活动的最小剩余时间
    const activeSeckills = seckillList.value.filter(i => i.status === 1 && i.remainSeconds > 0)
    if (activeSeckills.length > 0) {
      countdown.value = Math.min(...activeSeckills.map(i => i.remainSeconds))
    }
  } catch (e) {}
  finally { loading.value = false }
}

onMounted(() => {
  loadList()
  refreshTimer = setInterval(loadList, 30000)
  cdTimer = setInterval(() => { if (countdown.value > 0) countdown.value-- }, 1000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  if (cdTimer) clearInterval(cdTimer)
})

const getDiscount = (item) => {
  if (!item.goodsPrice || !item.seckillPrice) return '?'
  // 计算折扣（X折 = 秒杀价/原价 * 10）
  return (Number(item.seckillPrice) / Number(item.goodsPrice) * 10).toFixed(1)
}
const getProgress = (item) => {
  if (item.stockCount == null || !item.goodsStock) return 0
  const sold = item.goodsStock - item.stockCount
  return Math.min(99, Math.round((sold / item.goodsStock) * 100))
}

const goDetail = (item) => navigateTo('/pages/seckill/detail?id=' + item.id)
</script>

<style scoped>
.page { background: #f5f5f5; min-height: 100vh; }

/* 头部 */
.header { position: relative; padding: 32rpx; margin-bottom: 20rpx; }
.header-bg {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(135deg, #e4393c, #ff6b81);
}
.header-content {
  position: relative; z-index: 1; display: flex;
  justify-content: space-between; align-items: center;
}
.header-title { font-size: 36rpx; font-weight: 700; color: #fff; display: block; margin-bottom: 4rpx; }
.header-sub { font-size: 24rpx; color: rgba(255,255,255,0.8); }
.cd-label { font-size: 22rpx; color: rgba(255,255,255,0.8); display: block; text-align: center; margin-bottom: 6rpx; }
.cd-box { display: flex; gap: 4rpx; align-items: center; }
.cd-num {
  background: rgba(0,0,0,0.25); padding: 6rpx 10rpx; border-radius: 8rpx;
  font-size: 28rpx; font-weight: 700; color: #fff;
  font-family: 'Courier New', monospace; min-width: 44rpx; text-align: center;
}
.cd-sep { font-size: 24rpx; font-weight: 700; color: #fff; }

/* 列表 */
.list { padding: 0 20rpx; }
.sk-card {
  display: flex; gap: 20rpx; background: #fff;
  border-radius: 16rpx; padding: 20rpx; margin-bottom: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}
.sk-img { width: 200rpx; height: 200rpx; border-radius: 12rpx; flex-shrink: 0; }
.sk-info { flex: 1; display: flex; flex-direction: column; }
.sk-name {
  font-size: 28rpx; font-weight: 500; color: #222;
  margin-bottom: 8rpx; overflow: hidden; text-overflow: ellipsis;
  white-space: nowrap;
}
.sk-price-row { display: flex; align-items: baseline; gap: 10rpx; margin-bottom: 12rpx; }
.sk-price { display: flex; align-items: baseline; }
.sk-yen { font-size: 24rpx; color: #e4393c; font-weight: 600; }
.sk-now { font-size: 40rpx; color: #e4393c; font-weight: 800; }
.sk-old { font-size: 22rpx; color: #999; text-decoration: line-through; }
.sk-discount {
  background: #fff0f0; color: #e4393c; padding: 4rpx 10rpx;
  border-radius: 6rpx; font-size: 20rpx; font-weight: 600;
}
.sk-progress { margin-bottom: 12rpx; }
.sk-bar { height: 12rpx; background: #f0f0f0; border-radius: 6rpx; overflow: hidden; margin-bottom: 4rpx; }
.sk-bar-fill { height: 100%; background: linear-gradient(90deg, #e4393c, #ff6b81); border-radius: 6rpx; }
.sk-pct { font-size: 20rpx; color: #999; }
.sk-btn {
  padding: 12rpx 0; text-align: center; border-radius: 8rpx;
  font-size: 26rpx; font-weight: 600;
  background: #ddd; color: #999;
}
.sk-btn.active { background: #e4393c; color: #fff; }

/* 空状态 & 加载 */
.empty { text-align: center; padding: 100rpx 0; }
.empty-icon { font-size: 80rpx; display: block; margin-bottom: 16rpx; }
.empty-text { font-size: 28rpx; color: #999; }
.loading { text-align: center; padding: 60rpx 0; }
.loading-text { font-size: 26rpx; color: #999; }
</style>
