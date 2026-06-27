<!--
  秒杀商品详情页

  功能：
  1. 商品大图 + 状态标签
  2. 浮起的价格卡片（秒杀价、原价、折扣、库存、倒计时）
  3. 底部操作栏（首页 + 抢购按钮）
  4. 验证码弹窗 → 秒杀请求 → 轮询结果

  路径参数：?id=秒杀商品ID
  秒杀流程：点击抢购 → 验证码 → doSeckill → 轮询 getSeckillResult → 成功/失败弹窗
-->
<template>
  <view class="page" v-if="detail">
    <!-- 商品图片 -->
    <view class="hero-img">
      <image :src="getFullImageUrl(detail.goodsImg)" mode="aspectFill" class="img" />
      <view class="img-overlay">
        <view class="status-tag" :class="'st-' + detail.status">
          {{ ['未开始', '进行中', '已结束'][detail.status] }}
        </view>
      </view>
    </view>

    <!-- 价格卡片 -->
    <view class="price-card">
      <view class="price-row">
        <view class="seckill-price">
          <text class="price-symbol">¥</text>
          <text class="price-value">{{ detail.seckillPrice }}</text>
        </view>
        <view class="price-info">
          <text class="original-price">原价 ¥{{ detail.goodsPrice }}</text>
          <view class="discount-tag">{{ discount }}折</view>
        </view>
      </view>
      <view class="stock-row">
        <text class="stock-text">剩余 {{ detail.stockCount }} 件</text>
        <view v-if="detail.status !== 2" class="countdown-inline">
          <Countdown
            :seconds="detail.remainSeconds"
            :type="detail.status === 0 ? 'start' : 'end'"
            @finished="onCountdownFinished"
          />
        </view>
      </view>
    </view>

    <!-- 商品信息 -->
    <view class="info-card">
      <view class="info-title">{{ detail.goodsName }}</view>
      <view class="info-subtitle">{{ detail.goodsTitle }}</view>
    </view>

    <!-- 底部操作栏 -->
    <view class="action-bar safe-bottom">
      <view class="action-left">
        <view class="action-icon" @tap="goHome">
          <text>🏠</text>
          <text class="icon-label">首页</text>
        </view>
      </view>
      <view class="action-right">
        <view
          class="buy-btn"
          :class="{ 'btn-disabled': detail.status !== 1, 'btn-glow': detail.status === 1 }"
          @tap="handleSeckill"
        >
          {{ detail.status === 0 ? '⏰ 即将开始' : detail.status === 2 ? '已结束' : '🔥 立即抢购' }}
        </view>
      </view>
    </view>

    <!-- 验证码弹窗 -->
    <CaptchaPopup
      :visible="captchaVisible"
      @close="captchaVisible = false"
      @submit="submitSeckill"
    />

    <!-- 结果弹窗 -->
    <view v-if="resultVisible" class="modal-overlay" @tap="resultVisible = false">
      <view class="result-popup" @tap.stop>
        <!-- 成功 -->
        <view v-if="resultStatus === 'success'" class="result-content success">
          <view class="result-icon">🎉</view>
          <view class="result-title">抢购成功！</view>
          <view class="result-order">订单号: {{ resultOrderNo }}</view>
          <view class="result-btns">
            <view class="result-btn primary" @tap="goOrderDetail">查看订单</view>
          </view>
        </view>
        <!-- 失败 -->
        <view v-else-if="resultStatus === 'fail'" class="result-content fail">
          <view class="result-icon">😔</view>
          <view class="result-title">{{ resultMsg || '抢购失败' }}</view>
          <view class="result-btns">
            <view class="result-btn ghost" @tap="resultVisible = false">知道了</view>
          </view>
        </view>
        <!-- 排队中 -->
        <view v-else class="result-content queuing">
          <view class="result-icon spinning">⏳</view>
          <view class="result-title">排队中...</view>
          <view class="result-hint">请耐心等待</view>
        </view>
      </view>
    </view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getSeckillGoodsDetail } from '../../api/goods'
import { doSeckill, getSeckillResult } from '../../api/seckill'
import { useUserStore } from '../../store/user'
import Countdown from '../../components/Countdown.vue'
import CaptchaPopup from '../../components/CaptchaPopup.vue'
import { getFullImageUrl } from '../../utils/image'

const userStore = useUserStore()
const detail = ref(null)
const captchaVisible = ref(false)
const resultVisible = ref(false)
const resultStatus = ref('')
import { showToast, navigateTo, switchTab } from '../../utils/nav'

const resultOrderNo = ref('')
const resultMsg = ref('')
let pollingTimer = null

const discount = computed(() => {
  if (!detail.value) return '--'
  return (Number(detail.value.seckillPrice) / Number(detail.value.goodsPrice) * 10).toFixed(1)
})

onMounted(async () => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  const id = page.$page?.options?.id || page.options?.id
  if (id) {
    try {
      const res = await getSeckillGoodsDetail(id)
      detail.value = res.data
    } catch (e) {
      showToast({ title: '加载失败', icon: 'none' })
    }
  }
})

onUnmounted(() => { if (pollingTimer) clearInterval(pollingTimer) })

const onCountdownFinished = () => {
  if (detail.value) {
    getSeckillGoodsDetail(detail.value.id).then(res => { detail.value = res.data })
  }
}

const goHome = () => switchTab('/pages/index/index')

const handleSeckill = () => {
  if (!userStore.isLoggedIn) {
    navigateTo('/pages/login/login')
    return
  }
  if (!detail.value || detail.value.status !== 1) return
  captchaVisible.value = true
}

const submitSeckill = async (captchaId, captchaCode) => {
  captchaVisible.value = false
  try {
    const res = await doSeckill(detail.value.id, captchaId, captchaCode)
    const data = res.data
    resultStatus.value = data.status
    resultVisible.value = true
    if (data.status === 'success') {
      resultOrderNo.value = data.orderNo
    } else if (data.status === 'queuing') {
      startPolling()
    }
  } catch (e) {}
}

const startPolling = () => {
  if (pollingTimer) clearInterval(pollingTimer)
  let retryCount = 0
  const MAX_RETRIES = 60
  pollingTimer = setInterval(async () => {
    retryCount++
    if (retryCount > MAX_RETRIES) {
      clearInterval(pollingTimer)
      resultStatus.value = 'fail'
      resultMsg.value = '查询超时，请查看订单列表'
      return
    }
    try {
      const res = await getSeckillResult(detail.value.id)
      const data = res.data
      resultStatus.value = data.status
      if (data.status !== 'queuing') {
        clearInterval(pollingTimer)
        if (data.status === 'success') resultOrderNo.value = data.orderNo
        else resultMsg.value = data.msg
      }
    } catch (e) { clearInterval(pollingTimer) }
  }, 1000)
}

const goOrderDetail = () => {
  resultVisible.value = false
  navigateTo('/pages/order/detail?orderNo=' + resultOrderNo.value)
}
</script>

<style scoped>
.page { padding-bottom: 140rpx; }

/* 商品图片 */
.hero-img { position: relative; }
.img { width: 100%; height: 600rpx; display: block; }
.img-overlay {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0.1), transparent 30%);
}
.status-tag {
  position: absolute; top: 24rpx; right: 24rpx;
  padding: 8rpx 24rpx; border-radius: 24rpx;
  font-size: 24rpx; font-weight: 600; color: #fff;
}
.st-0 { background: rgba(107, 114, 128, 0.8); }
.st-1 { background: rgba(239, 71, 111, 0.9); }
.st-2 { background: rgba(107, 114, 128, 0.5); }

/* 价格卡片 */
.price-card {
  background: var(--bg-card); margin: -40rpx 20rpx 0; position: relative;
  border-radius: var(--radius-lg); padding: 28rpx;
  box-shadow: var(--shadow-lg); z-index: 1;
}
.price-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.seckill-price { display: flex; align-items: baseline; }
.price-symbol { font-size: 28rpx; color: var(--danger); font-weight: 600; }
.price-value { font-size: 56rpx; color: var(--danger); font-weight: 700; }
.price-info { display: flex; align-items: center; gap: 12rpx; }
.original-price { font-size: 24rpx; color: var(--text-hint); text-decoration: line-through; }
.discount-tag {
  padding: 4rpx 14rpx; background: var(--danger-light); color: var(--danger);
  font-size: 22rpx; font-weight: 600; border-radius: 8rpx;
}
.stock-row { display: flex; justify-content: space-between; align-items: center; }
.stock-text { font-size: 24rpx; color: var(--text-secondary); }

/* 商品信息 */
.info-card {
  background: var(--bg-card); margin: 20rpx; border-radius: var(--radius);
  padding: 28rpx; box-shadow: var(--shadow);
}
.info-title { font-size: 34rpx; font-weight: 700; color: var(--text-primary); line-height: 1.4; }
.info-subtitle { font-size: 26rpx; color: var(--text-secondary); margin-top: 12rpx; line-height: 1.5; }

/* 底部操作栏 */
.action-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  display: flex; align-items: center;
  background: var(--bg-card); padding: 16rpx 24rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.06);
}
.action-left { display: flex; gap: 32rpx; padding-right: 24rpx; }
.action-icon { display: flex; flex-direction: column; align-items: center; gap: 4rpx; font-size: 36rpx; }
.icon-label { font-size: 20rpx; color: var(--text-hint); }
.action-right { flex: 1; }
.buy-btn {
  display: flex; align-items: center; justify-content: center;
  height: 88rpx; border-radius: 44rpx;
  font-size: 30rpx; font-weight: 700; color: #fff;
}
.btn-glow {
  background: linear-gradient(135deg, #ef476f, #d63d63);
  box-shadow: 0 8rpx 30rpx rgba(239, 71, 111, 0.4);
}
.btn-disabled { background: #e5e7eb; color: #9ca3af; }

/* 结果弹窗 */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); z-index: 999;
  display: flex; align-items: center; justify-content: center;
}
.result-popup {
  width: 560rpx; background: #fff; border-radius: var(--radius-lg);
  padding: 48rpx; text-align: center;
}
.result-content { display: flex; flex-direction: column; align-items: center; }
.result-icon { font-size: 96rpx; margin-bottom: 20rpx; }
.result-title { font-size: 34rpx; font-weight: 700; color: var(--text-primary); margin-bottom: 12rpx; }
.result-order { font-size: 24rpx; color: var(--text-secondary); margin-bottom: 32rpx; }
.result-hint { font-size: 24rpx; color: var(--text-hint); margin-top: 8rpx; }
.result-btns { width: 100%; margin-top: 24rpx; }
.result-btn {
  display: flex; align-items: center; justify-content: center;
  height: 84rpx; border-radius: 42rpx; font-size: 28rpx; font-weight: 600;
}
.result-btn.primary {
  background: linear-gradient(135deg, #4361ee, #3a56d4); color: #fff;
}
.result-btn.ghost { background: #f3f4f6; color: var(--text-secondary); }
.spinning { animation: spin 1s linear infinite; display: inline-block; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>
