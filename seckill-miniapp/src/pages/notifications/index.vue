<template>
  <view class="page">
    <view v-if="notifications.length > 0" class="noti-list">
      <view class="noti-item" v-for="item in notifications" :key="item.id"
        :class="{ unread: item.isRead === 0 }" @tap="handleRead(item)">
        <view class="noti-icon">
          {{ item.type === 'order' ? '📦' : item.type === 'promotion' ? '🎁' : '📢' }}
        </view>
        <view class="noti-content">
          <text class="noti-title">{{ item.title }}</text>
          <text class="noti-text">{{ item.content }}</text>
          <text class="noti-time">{{ item.createTime }}</text>
        </view>
        <view v-if="item.isRead === 0" class="noti-dot"></view>
      </view>
    </view>
    <view v-else class="empty">
      <text class="empty-icon">🔔</text>
      <text class="empty-text">暂无消息</text>
    </view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getNotifications, markAsRead } from '../../api/notification'

const notifications = ref([])

onShow(async () => {
  try { const r = await getNotifications(); notifications.value = r.data || [] } catch (e) {}
})

const handleRead = async (item) => {
  if (item.isRead === 0) {
    try { await markAsRead(item.id); item.isRead = 1 } catch (e) {}
  }
}
</script>

<style scoped>
.page { background: #f5f5f5; min-height: 100vh; padding: 20rpx; }
.noti-list { display: flex; flex-direction: column; gap: 12rpx; }
.noti-item {
  display: flex; align-items: flex-start; gap: 16rpx;
  background: #fff; padding: 24rpx; border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06); position: relative;
}
.noti-item.unread { background: #fff8f8; }
.noti-icon { font-size: 40rpx; }
.noti-content { flex: 1; }
.noti-title { font-size: 28rpx; font-weight: 600; display: block; margin-bottom: 6rpx; }
.noti-text { font-size: 24rpx; color: #666; display: block; margin-bottom: 8rpx; }
.noti-time { font-size: 22rpx; color: #999; }
.noti-dot { width: 16rpx; height: 16rpx; background: #e4393c; border-radius: 50%; }

.empty { text-align: center; padding: 120rpx 0; }
.empty-icon { font-size: 80rpx; display: block; margin-bottom: 16rpx; }
.empty-text { font-size: 28rpx; color: #999; }
</style>
