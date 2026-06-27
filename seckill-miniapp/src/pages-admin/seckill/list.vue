<!--
  管理后台 - 秒杀活动管理

  功能：
  1. 秒杀活动列表（名称、价格、库存、时间、状态）
  2. 手动开始活动（status 0→1）
  3. 手动结束活动（status 1→2）
  4. 删除活动（进行中不可删除）

  权限：需管理员（role=1），onMounted 中校验
  状态流转：未开始(0) → 进行中(1) → 已结束(2)，单向不可逆
-->
<template>
  <view class="container">
    <view class="title">秒杀活动管理</view>

    <view v-for="item in seckillList" :key="item.id" class="seckill-item">
      <view class="item-info">
        <text class="item-name">{{ item.goodsName }}</text>
        <text class="item-price">¥{{ item.seckillPrice }} | 库存: {{ item.stockCount }}</text>
        <text class="item-time">{{ item.startTime }} ~ {{ item.endTime }}</text>
        <view class="item-tag" :class="'tag-' + item.status">{{ ['未开始','进行中','已结束'][item.status] }}</view>
      </view>
      <view class="item-actions">
        <view v-if="item.status === 0" class="act-btn success" @tap="handleStart(item)">开始</view>
        <view v-if="item.status === 1" class="act-btn warning" @tap="handleEnd(item)">结束</view>
        <view v-if="item.status !== 1" class="act-btn danger" @tap="handleDelete(item)">删除</view>
      </view>
    </view>

    <view v-if="seckillList.length === 0" class="empty">暂无数据</view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref, onMounted } from 'vue'
import { getAdminSeckillList, updateSeckillStatus, deleteSeckillActivity } from '../../api/admin'
import { useUserStore } from '../../store/user'

import { showToast, navigateBack, showModal } from '../../utils/nav'

const userStore = useUserStore()
const seckillList = ref([])

const loadList = async () => {
  try { const res = await getAdminSeckillList(); seckillList.value = res.data || [] } catch (e) {}
}

onMounted(() => {
  if (!userStore.isAdmin) { showToast({ title: '无权限', icon: 'none' }); navigateBack(); return }
  loadList()
})

const handleStart = async (item) => {
  const res = await showModal({ title: '确认开始', content: '确认开始该秒杀活动？' })
  if (res.confirm) {
    try { await updateSeckillStatus(item.id, 1); showToast({ title: '已开始', icon: 'success' }); loadList() } catch (e) {}
  }
}

const handleEnd = async (item) => {
  const res = await showModal({ title: '确认结束', content: '确认结束该秒杀活动？' })
  if (res.confirm) {
    try { await updateSeckillStatus(item.id, 2); showToast({ title: '已结束', icon: 'success' }); loadList() } catch (e) {}
  }
}

const handleDelete = async (item) => {
  const res = await showModal({ title: '确认删除', content: '确认删除该秒杀活动？' })
  if (res.confirm) {
    try { await deleteSeckillActivity(item.id); showToast({ title: '已删除', icon: 'success' }); loadList() } catch (e) {}
  }
}
</script>

<style scoped>
.container { padding: 20rpx; }
.title { font-size: 32rpx; font-weight: bold; margin-bottom: 20rpx; }
.seckill-item { background: #fff; border-radius: 12rpx; padding: 20rpx; margin-bottom: 16rpx; }
.item-name { font-size: 28rpx; font-weight: bold; display: block; }
.item-price { font-size: 24rpx; color: #f56c6c; display: block; margin-top: 6rpx; }
.item-time { font-size: 22rpx; color: #999; display: block; margin-top: 4rpx; }
.item-tag { display: inline-block; font-size: 20rpx; padding: 4rpx 12rpx; border-radius: 6rpx; margin-top: 8rpx; }
.tag-0 { background: #e6f7ff; color: #409eff; }
.tag-1 { background: #fff7e6; color: #e6a23c; }
.tag-2 { background: #f5f5f5; color: #999; }
.item-actions { display: flex; gap: 16rpx; margin-top: 12rpx; }
.act-btn { font-size: 22rpx; padding: 8rpx 20rpx; border-radius: 6rpx; }
.act-btn.success { background: #e6fffb; color: #67c23a; }
.act-btn.warning { background: #fff7e6; color: #e6a23c; }
.act-btn.danger { background: #fff1f0; color: #f56c6c; }
.empty { text-align: center; color: #999; padding: 80rpx 0; }
</style>
