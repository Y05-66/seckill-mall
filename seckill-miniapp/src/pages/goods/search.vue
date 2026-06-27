<!--
  商品搜索页

  功能：搜索商品，显示搜索历史和热门搜索
-->
<template>
  <view class="page">
    <!-- 搜索栏 -->
    <view class="search-header">
      <view class="search-box">
        <text class="search-icon">🔍</text>
        <input
          v-model="keyword"
          placeholder="搜索商品名称、分类..."
          class="search-input"
          focus
          confirm-type="search"
          @confirm="handleSearch"
        />
        <text v-if="keyword" class="clear-btn" @tap="keyword = ''">×</text>
      </view>
      <text class="cancel-btn" @tap="goBack">取消</text>
    </view>

    <!-- 搜索历史 & 热门搜索 -->
    <view v-if="!keyword && !showResults" class="search-panel">
      <!-- 搜索历史 -->
      <view v-if="searchHistory.length > 0" class="panel-section">
        <view class="section-header">
          <text class="section-title">搜索历史</text>
          <text class="clear-history" @tap="clearHistory">🗑️ 清空</text>
        </view>
        <view class="tag-list">
          <text
            v-for="(item, index) in searchHistory" :key="index"
            class="history-tag" @tap="selectKeyword(item)"
          >{{ item }}</text>
        </view>
      </view>

      <!-- 热门搜索 -->
      <view class="panel-section">
        <view class="section-header">
          <text class="section-title">🔥 热门搜索</text>
        </view>
        <view class="tag-list">
          <text
            v-for="(item, index) in hotKeywords" :key="index"
            class="hot-tag" :class="{ 'hot-top': index < 3 }" @tap="selectKeyword(item)"
          >{{ item }}</text>
        </view>
      </view>
    </view>

    <!-- 搜索结果 -->
    <view v-if="showResults" class="result-section">
      <view class="result-header">
        <text class="result-count">找到 {{ filteredList.length }} 件商品</text>
      </view>

      <view v-if="filteredList.length > 0" class="goods-grid">
        <view class="goods-card" v-for="item in filteredList" :key="item.id" @tap="goDetail(item)">
          <image :src="getFullImageUrl(item.goodsImg)" mode="aspectFill" class="goods-img" @error="handleImageError" />
          <view class="goods-info">
            <text class="goods-name">{{ item.goodsName }}</text>
            <view class="goods-price-row">
              <text class="price-symbol">¥</text>
              <text class="price-value">{{ item.goodsPrice }}</text>
            </view>
          </view>
        </view>
      </view>

      <view v-else class="empty">
        <text class="empty-icon">🔍</text>
        <text class="empty-text">未找到"{{ keyword }}"相关商品</text>
        <text class="empty-hint">换个关键词试试</text>
      </view>
    </view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref, computed, onMounted, watch } from 'vue'
import { getGoodsList } from '../../api/goods'
import { navigateTo, navigateBack } from '../../utils/nav'
import { handleImageError, getFullImageUrl } from '../../utils/image'

const keyword = ref('')
const goodsList = ref([])
const showResults = ref(false)

// 搜索历史
const searchHistory = ref(uni.getStorageSync('searchHistory') || [])

// 热门搜索
const hotKeywords = ref(['iPhone', 'MacBook', '耳机', 'iPad', '小米', '华为', '键盘', '鼠标'])

// 过滤后的商品列表
const filteredList = computed(() => {
  if (!keyword.value.trim()) return []
  const kw = keyword.value.trim().toLowerCase()
  return goodsList.value.filter(g =>
    (g.goodsName && g.goodsName.toLowerCase().includes(kw)) ||
    (g.goodsTitle && g.goodsTitle.toLowerCase().includes(kw))
  )
})

// 加载商品列表
onMounted(async () => {
  try {
    const res = await getGoodsList()
    goodsList.value = res.data || []
  } catch (e) {}
})

// 监听关键词变化
watch(keyword, (newVal) => {
  if (newVal.trim()) {
    showResults.value = true
  } else {
    showResults.value = false
  }
})

// 搜索
const handleSearch = () => {
  if (!keyword.value.trim()) return
  saveSearchHistory(keyword.value.trim())
}

// 选择关键词
const selectKeyword = (kw) => {
  keyword.value = kw
  saveSearchHistory(kw)
}

// 保存搜索历史
const saveSearchHistory = (kw) => {
  let history = searchHistory.value.filter(item => item !== kw)
  history.unshift(kw)
  history = history.slice(0, 10)
  uni.setStorageSync('searchHistory', history)
  searchHistory.value = history
}

// 清空搜索历史
const clearHistory = () => {
  uni.removeStorageSync('searchHistory')
  searchHistory.value = []
}

// 返回
const goBack = () => navigateBack()

// 跳转商品详情
const goDetail = (item) => navigateTo('/pages/goods/detail?id=' + item.id)
</script>

<style scoped>
.page { background: #f5f5f5; min-height: 100vh; }

/* 搜索栏 */
.search-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 20rpx;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 40rpx;
  padding: 16rpx 20rpx;
}

.search-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
  opacity: 0.6;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  background: transparent;
}

.clear-btn {
  font-size: 32rpx;
  color: #999;
  padding: 0 8rpx;
}

.cancel-btn {
  font-size: 28rpx;
  color: #666;
  white-space: nowrap;
}

/* 搜索面板 */
.search-panel {
  padding: 20rpx;
}

.panel-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.clear-history {
  font-size: 24rpx;
  color: #999;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.history-tag, .hot-tag {
  padding: 12rpx 24rpx;
  background: #f5f5f5;
  border-radius: 32rpx;
  font-size: 24rpx;
  color: #666;
}

.hot-tag.hot-top {
  background: #fff5f5;
  color: #e4393c;
  font-weight: 500;
}

/* 搜索结果 */
.result-section {
  padding: 20rpx;
}

.result-header {
  margin-bottom: 16rpx;
}

.result-count {
  font-size: 24rpx;
  color: #999;
}

.goods-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.goods-card {
  width: calc(50% - 8rpx);
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}

.goods-img {
  width: 100%;
  height: 340rpx;
}

.goods-info {
  padding: 16rpx;
}

.goods-name {
  font-size: 26rpx;
  font-weight: 500;
  color: #222;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 8rpx;
}

.goods-price-row {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 22rpx;
  color: #e4393c;
  font-weight: 600;
}

.price-value {
  font-size: 34rpx;
  color: #e4393c;
  font-weight: 700;
}

/* 空状态 */
.empty {
  text-align: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 80rpx;
  display: block;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-bottom: 12rpx;
}

.empty-hint {
  font-size: 24rpx;
  color: #999;
}
</style>
