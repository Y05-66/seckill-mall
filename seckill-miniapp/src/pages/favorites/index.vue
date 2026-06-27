<template>
  <view class="page">
    <view v-if="favorites.length > 0" class="goods-grid">
      <view class="goods-card" v-for="item in favorites" :key="item.id" @tap="goDetail(item)">
        <image :src="getFullImageUrl(item.goodsImg)" mode="aspectFill" class="goods-img" @error="handleImageError" />
        <view class="goods-info">
          <text class="goods-name">{{ item.goodsName }}</text>
          <text class="goods-price">¥{{ item.goodsPrice }}</text>
        </view>
        <text class="remove-btn" @tap.stop="handleRemove(item)">取消收藏</text>
      </view>
    </view>
    <view v-else class="empty">
      <text class="empty-icon">❤️</text>
      <text class="empty-text">暂无收藏</text>
      <view class="empty-btn" @tap="goGoods">去逛逛</view>
    </view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getFavoriteList, removeFavorite } from '../../api/favorite'
import { navigateTo, switchTab, showToast, showModal } from '../../utils/nav'
import { handleImageError, getFullImageUrl } from '../../utils/image'

const favorites = ref([])

onShow(async () => {
  try { const r = await getFavoriteList(); favorites.value = r.data || [] } catch (e) {}
})

const goDetail = (item) => navigateTo('/pages/goods/detail?id=' + item.id)
const goGoods = () => switchTab('/pages/index/index')
const handleRemove = async (item) => {
  try {
    const res = await showModal({ title: '提示', content: '确认取消收藏？' })
    if (!res.confirm) return
    await removeFavorite(item.id)
    favorites.value = favorites.value.filter(i => i.id !== item.id)
    showToast({ title: '已取消收藏', icon: 'success' })
  } catch (e) {}
}
</script>

<style scoped>
.page { background: #f5f5f5; min-height: 100vh; padding: 20rpx; }
.goods-grid { display: flex; flex-wrap: wrap; gap: 16rpx; }
.goods-card {
  width: calc(50% - 8rpx); background: #fff; border-radius: 16rpx;
  overflow: hidden; position: relative;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}
.goods-img { width: 100%; height: 340rpx; }
.goods-info { padding: 16rpx; }
.goods-name { font-size: 26rpx; display: block; margin-bottom: 8rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.goods-price { font-size: 34rpx; color: #e4393c; font-weight: 700; }
.remove-btn {
  position: absolute; top: 12rpx; right: 12rpx;
  background: rgba(0,0,0,0.6); color: #fff; padding: 6rpx 16rpx;
  border-radius: 8rpx; font-size: 22rpx;
}

.empty { text-align: center; padding: 120rpx 0; }
.empty-icon { font-size: 80rpx; display: block; margin-bottom: 16rpx; }
.empty-text { font-size: 28rpx; color: #999; display: block; margin-bottom: 24rpx; }
.empty-btn {
  display: inline-block; padding: 16rpx 48rpx; background: #e4393c;
  color: #fff; font-size: 28rpx; font-weight: 600; border-radius: 12rpx;
}
</style>
