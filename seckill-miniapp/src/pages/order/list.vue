<template>
  <view class="page">
    <!-- 未登录 -->
    <view v-if="!userStore.isLoggedIn" class="empty">
      <text class="empty-icon">🔐</text>
      <text class="empty-text">请先登录查看订单</text>
      <view class="empty-btn" @tap="goLogin">去登录</view>
    </view>

    <!-- 已登录 -->
    <template v-else>
      <!-- 状态筛选标签 -->
      <view class="tabs">
        <view
          v-for="tab in tabs" :key="tab.value"
          class="tab-item" :class="{ active: currentTab === tab.value }"
          @tap="switchTabHandler(tab.value)"
        >
          <text>{{ tab.label }}</text>
          <view v-if="tab.value === 0 && unpaidCount > 0" class="badge">{{ unpaidCount }}</view>
        </view>
      </view>

      <view v-if="filteredList.length > 0" class="list">
        <view class="order-card" v-for="order in filteredList" :key="order.orderNo">
          <view class="oc-header">
            <text class="oc-no">{{ order.orderNo }}</text>
            <text class="oc-status" :class="'s' + order.status">{{ order.statusDesc }}</text>
          </view>
          <view class="oc-body" @tap="goDetail(order)">
            <text class="oc-name">{{ order.goodsName }}</text>
            <view class="oc-bottom">
              <view class="oc-price">
                <text class="oc-yen">¥</text>
                <text class="oc-num">{{ order.seckillPrice }}</text>
              </view>
              <view class="oc-actions" v-if="order.status === 0">
                <view class="oc-btn cancel" @tap.stop="handleCancel(order)">取消</view>
                <view class="oc-btn pay" @tap.stop="handlePay(order)">支付</view>
              </view>
              <view class="oc-actions" v-else-if="order.status === 1">
                <view class="oc-btn refund" @tap.stop="handleRefund(order)">退款</view>
              </view>
              <view class="oc-actions" v-if="order.status >= 2">
                <view class="oc-btn delete" @tap.stop="handleDelete(order)">删除</view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-else-if="!loading" class="empty">
        <text class="empty-icon">📦</text>
        <text class="empty-text">{{ currentTab === -1 ? '暂无订单' : '暂无该状态订单' }}</text>
        <view class="empty-btn" @tap="goSeckill">去抢购</view>
      </view>

      <view v-if="loading" class="loading">
        <text class="loading-text">加载中...</text>
      </view>
    </template>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref, computed, onUnmounted } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { getOrderList, payOrder, cancelOrder, deleteOrder } from '../../api/order'
import { useUserStore } from '../../store/user'
import { navigateTo, switchTab as navSwitchTab, showModal, showToast } from '../../utils/nav'

const userStore = useUserStore()
const orderList = ref([])
const loading = ref(true)

/** 当前选中的标签值，-1=全部 */
const currentTab = ref(-1)

/** 标签配置 */
const tabs = [
  { label: '全部', value: -1 },
  { label: '待支付', value: 0 },
  { label: '已支付', value: 1 },
  { label: '已取消', value: 2 },
  { label: '已超时', value: 3 },
  { label: '已退款', value: 4 }
]

/** 未支付订单数量（用于标签角标显示） */
const unpaidCount = computed(() => {
  return orderList.value.filter(o => o.status === 0).length
})

/** 根据标签筛选订单列表 */
const filteredList = computed(() => {
  if (currentTab.value === -1) return orderList.value
  return orderList.value.filter(o => o.status === currentTab.value)
})

const goLogin = () => navigateTo('/pages/login/login')
const goSeckill = () => navSwitchTab('/pages/seckill/list')

/** 跳转订单详情 */
const goDetail = (order) => navigateTo('/pages/order/detail?orderNo=' + order.orderNo)

/** 切换状态标签 */
const switchTabHandler = (value) => {
  currentTab.value = value
}

/** 加载订单列表 */
const loadOrders = async () => {
  if (!userStore.isLoggedIn) { loading.value = false; return }
  try { const r = await getOrderList(); orderList.value = r.data || [] } catch (e) {}
  finally { loading.value = false }
}

// 监听从"我的"页面传来的 tab 切换事件
onShow(() => {
  loadOrders()
})

uni.$on('switchOrderTab', (tab) => {
  currentTab.value = tab
})

onUnmounted(() => {
  uni.$off('switchOrderTab')
})

// 下拉刷新
onPullDownRefresh(async () => {
  await loadOrders()
  uni.stopPullDownRefresh()
})

const handlePay = async (order) => {
  const res = await showModal({ title: '确认支付', content: '确认支付该订单？', confirmColor: '#e4393c' })
  if (!res.confirm) return
  try { await payOrder(order.orderNo); showToast({ title: '支付成功', icon: 'success' }); await loadOrders() } catch (e) {}
}

const handleCancel = async (order) => {
  const res = await showModal({ title: '确认取消', content: '取消后库存将回滚', confirmColor: '#e4393c' })
  if (!res.confirm) return
  try { await cancelOrder(order.orderNo); showToast({ title: '已取消', icon: 'success' }); await loadOrders() } catch (e) {}
}

const handleRefund = (order) => {
  navigateTo('/pages/refund/index?orderNo=' + order.orderNo + '&amount=' + order.seckillPrice)
}

const handleDelete = async (order) => {
  const res = await showModal({ title: '删除订单', content: '确认删除该订单？删除后无法恢复。', confirmColor: '#e4393c' })
  if (!res.confirm) return
  try {
    await deleteOrder(order.orderNo)
    showToast({ title: '订单已删除', icon: 'success' })
    await loadOrders()
  } catch (e) {}
}
</script>

<style scoped>
.page { background: #f5f5f5; min-height: 100vh; }

/* 标签栏 */
.tabs {
  display: flex; background: #fff; padding: 0 10rpx;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
  position: sticky; top: 0; z-index: 10;
}
.tab-item {
  flex: 1; display: flex; align-items: center; justify-content: center;
  height: 88rpx; font-size: 26rpx; color: #666;
  position: relative;
}
.tab-item.active {
  color: #e4393c; font-weight: 600;
}
.tab-item.active::after {
  content: ''; position: absolute; bottom: 0; left: 30%; right: 30%;
  height: 4rpx; background: #e4393c; border-radius: 2rpx;
}
.badge {
  position: absolute; top: 12rpx; right: 20%;
  min-width: 32rpx; height: 32rpx; line-height: 32rpx;
  background: #e4393c; color: #fff; font-size: 20rpx;
  text-align: center; border-radius: 16rpx; padding: 0 8rpx;
}

.list { padding: 20rpx; display: flex; flex-direction: column; gap: 16rpx; }
.order-card {
  background: #fff; border-radius: 16rpx; overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}
.oc-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16rpx 24rpx; background: #fafafa; border-bottom: 1rpx solid #f0f0f0;
}
.oc-no { font-size: 22rpx; color: #999; }
.oc-status { font-size: 22rpx; font-weight: 600; padding: 4rpx 14rpx; border-radius: 16rpx; }
.oc-status.s0 { background: #fff7e6; color: #fa8c16; }
.oc-status.s1 { background: #f6ffed; color: #52c41a; }
.oc-status.s2 { background: #f0f0f0; color: #999; }
.oc-status.s3 { background: #fff0f0; color: #e4393c; }
.oc-status.s4 { background: #fff0f0; color: #e4393c; }

.oc-body { padding: 20rpx 24rpx; }
.oc-name { font-size: 28rpx; font-weight: 500; color: #222; display: block; margin-bottom: 12rpx; }
.oc-bottom { display: flex; justify-content: space-between; align-items: center; }
.oc-price { display: flex; align-items: baseline; }
.oc-yen { font-size: 24rpx; color: #e4393c; font-weight: 600; }
.oc-num { font-size: 36rpx; color: #e4393c; font-weight: 800; }
.oc-actions { display: flex; gap: 12rpx; }
.oc-btn {
  padding: 10rpx 28rpx; border-radius: 8rpx; font-size: 24rpx; font-weight: 600;
}
.oc-btn.cancel { background: #f5f5f5; color: #666; }
.oc-btn.pay { background: #e4393c; color: #fff; }
.oc-btn.refund { background: #fff7e6; color: #fa8c16; }
.oc-btn.delete { background: #f5f5f5; color: #999; }

.empty { text-align: center; padding: 120rpx 0; }
.empty-icon { font-size: 80rpx; display: block; margin-bottom: 16rpx; }
.empty-text { font-size: 28rpx; color: #999; display: block; margin-bottom: 24rpx; }
.empty-btn {
  display: inline-block; padding: 16rpx 48rpx; background: #e4393c;
  color: #fff; font-size: 28rpx; font-weight: 600; border-radius: 12rpx;
}

.loading { text-align: center; padding: 60rpx 0; }
.loading-text { font-size: 26rpx; color: #999; }
</style>
