<template>
  <view class="page">
    <!-- 搜索栏 -->
    <view class="search-header">
      <view class="search-box" @tap="goSearch">
        <text class="search-icon">🔍</text>
        <text class="search-placeholder">搜索商品名称、分类...</text>
      </view>
    </view>

    <!-- Banner 轮播 -->
    <swiper
      class="banner-swiper"
      indicator-dots
      indicator-active-color="#ffffff"
      indicator-color="rgba(255,255,255,0.4)"
      autoplay
      interval="4000"
      duration="300"
      circular
      easing-function="easeInOutCubic"
    >
      <swiper-item v-for="(b, i) in banners" :key="i">
        <view class="banner" :style="{ background: b.bg }" @tap="goPage(b.link)">
          <view class="banner-content">
            <view class="banner-tag">{{ b.tag }}</view>
            <view class="banner-title">{{ b.title }}</view>
            <view class="banner-sub">{{ b.desc }}</view>
            <view class="banner-btn">{{ b.btn }}</view>
          </view>
          <view class="banner-icon">{{ b.icon }}</view>
        </view>
      </swiper-item>
    </swiper>

    <!-- 快捷入口 -->
    <view class="quick-row">
      <view class="quick-item" @tap="goSeckill">
        <view class="qi-icon" style="background:#fff0f0">⚡</view>
        <text class="qi-text">秒杀</text>
      </view>
      <view class="quick-item" @tap="goOrders">
        <view class="qi-icon" style="background:#f0f5ff">📋</view>
        <text class="qi-text">订单</text>
      </view>
      <view class="quick-item" @tap="goMy">
        <view class="qi-icon" style="background:#f0fff4">👤</view>
        <text class="qi-text">我的</text>
      </view>
    </view>

    <!-- 商品列表 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">全部商品</text>
        <text class="section-count">共{{ filteredList.length }}件</text>
      </view>
      <view class="goods-grid" v-if="filteredList.length > 0">
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
      <view v-else-if="!loading" class="empty">
        <text class="empty-icon">🔍</text>
        <text class="empty-text">暂无商品</text>
      </view>
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

import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh } from '@dcloudio/uni-app'
import { getGoodsList } from '../../api/goods'
import { switchTab, navigateTo } from '../../utils/nav'
import { handleImageError, getFullImageUrl } from '../../utils/image'

const goodsList = ref([])
const loading = ref(true)
const keyword = ref('')

const banners = [
  { tag: '限时特惠', title: '秒杀专场 低至1折', desc: '每日精选爆款商品，限时限量抢购', btn: '立即抢购 →', icon: '⚡', bg: 'linear-gradient(135deg, #e4393c 0%, #ff6b81 100%)', link: 'seckill' },
  { tag: '新品首发', title: '科技好物 品质保障', desc: '最新数码产品，正品行货，全国联保', btn: '立即选购 →', icon: '🆕', bg: 'linear-gradient(135deg, #5f27cd 0%, #a55eea 100%)', link: 'goods' },
  { tag: '新人福利', title: '注册享专属优惠', desc: '新用户注册即享新人礼包', btn: '免费注册 →', icon: '🎁', bg: 'linear-gradient(135deg, #f0932b 0%, #f6e58d 100%)', link: 'my' },
]

const goPage = (link) => {
  if (link === 'seckill') goSeckill()
  else if (link === 'goods') goSearch()
  else if (link === 'my') goMy()
}

// 过滤后的商品列表
const filteredList = computed(() => {
  if (!keyword.value.trim()) return goodsList.value
  const kw = keyword.value.trim().toLowerCase()
  return goodsList.value.filter(g =>
    (g.goodsName && g.goodsName.toLowerCase().includes(kw)) ||
    (g.goodsTitle && g.goodsTitle.toLowerCase().includes(kw))
  )
})

// 加载商品列表
const loadGoods = async () => {
  try {
    const res = await getGoodsList()
    goodsList.value = res.data || []
  } catch (e) {} finally {
    loading.value = false
  }
}

onMounted(loadGoods)

// 下拉刷新
onPullDownRefresh(async () => {
  await loadGoods()
  uni.stopPullDownRefresh()
})

// 跳转搜索页
const goSearch = () => navigateTo('/pages/goods/search')

const goSeckill = () => switchTab('/pages/seckill/list')
const goOrders = () => switchTab('/pages/order/list')
const goMy = () => switchTab('/pages/my/index')
const goDetail = (item) => navigateTo('/pages/goods/detail?id=' + item.id)
</script>

<style scoped>
.page { background: #f5f5f5; min-height: 100vh; padding: 20rpx; }

/* 搜索栏 */
.search-header {
  margin-bottom: 20rpx;
}
.search-box {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 40rpx;
  padding: 18rpx 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}
.search-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
  opacity: 0.6;
}
.search-placeholder {
  font-size: 26rpx;
  color: #999;
}

/* Banner 轮播 */
.banner-swiper {
  height: 340rpx;
  border-radius: 24rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
  background: #e4393c;
}
swiper-item {
  height: 340rpx;
}
.banner {
  position: relative; border-radius: 24rpx; padding: 36rpx 32rpx;
  height: 340rpx; box-sizing: border-box;
  display: flex; align-items: center; justify-content: space-between;
}
.banner-content { position: relative; z-index: 1; flex: 1; }
.banner-tag {
  display: inline-block; padding: 8rpx 24rpx; background: rgba(255,255,255,0.25);
  border-radius: 24rpx; font-size: 26rpx; color: #fff; margin-bottom: 16rpx;
  font-weight: 500; letter-spacing: 1rpx;
}
.banner-title {
  font-size: 52rpx; font-weight: 800; color: #fff; margin-bottom: 10rpx;
  letter-spacing: 2rpx; text-shadow: 0 2rpx 8rpx rgba(0,0,0,0.1);
}
.banner-sub { font-size: 28rpx; color: rgba(255,255,255,0.9); margin-bottom: 24rpx; line-height: 1.5; }
.banner-btn {
  display: inline-block; padding: 14rpx 36rpx; background: rgba(255,255,255,0.95);
  color: #e4393c; font-size: 28rpx; font-weight: 700; border-radius: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.1);
}
.banner-icon {
  position: absolute; right: 24rpx; top: 50%; transform: translateY(-50%);
  font-size: 100rpx; opacity: 0.2; z-index: 1;
}

/* 快捷入口 */
.quick-row {
  display: flex; justify-content: space-around; background: #fff;
  border-radius: 16rpx; padding: 28rpx 0; margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}
.quick-item { display: flex; flex-direction: column; align-items: center; gap: 10rpx; }
.qi-icon {
  width: 80rpx; height: 80rpx; border-radius: 20rpx;
  display: flex; align-items: center; justify-content: center; font-size: 36rpx;
}
.qi-text { font-size: 24rpx; color: #666; font-weight: 500; }

/* Section */
.section { margin-bottom: 24rpx; }
.section-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16rpx; padding: 0 4rpx;
}
.section-title { font-size: 30rpx; font-weight: 700; color: #222; }
.section-count { font-size: 24rpx; color: #999; }

/* 商品网格 */
.goods-grid { display: flex; flex-wrap: wrap; gap: 16rpx; }
.goods-card {
  width: calc(50% - 8rpx); background: #fff; border-radius: 16rpx;
  overflow: hidden; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}
.goods-img { width: 100%; height: 340rpx; }
.goods-info { padding: 16rpx; }
.goods-name {
  font-size: 26rpx; font-weight: 500; color: #222;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  margin-bottom: 8rpx;
}
.goods-price-row { display: flex; align-items: baseline; }
.price-symbol { font-size: 22rpx; color: #e4393c; font-weight: 600; }
.price-value { font-size: 34rpx; color: #e4393c; font-weight: 700; }

/* 空状态 & 加载 */
.empty { text-align: center; padding: 100rpx 0; }
.empty-icon { font-size: 80rpx; display: block; margin-bottom: 16rpx; }
.empty-text { font-size: 28rpx; color: #999; }
.loading { text-align: center; padding: 60rpx 0; }
.loading-text { font-size: 26rpx; color: #999; }
</style>
