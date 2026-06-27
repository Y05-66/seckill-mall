<!--
  普通商品详情页

  功能：展示商品大图、价格、库存、名称、描述，支持加入购物车和收藏。
  路径参数：?id=商品ID

  数据流：onMounted → getGoodsDetail(id) → detail
-->
<template>
  <view class="page" v-if="detail">
    <view class="hero-img">
      <image :src="getFullImageUrl(detail.goodsImg)" mode="aspectFill" class="img" @error="handleImageError" />
    </view>

    <view class="price-card">
      <view class="price-row">
        <view class="price">
          <text class="price-symbol">¥</text>
          <text class="price-value">{{ detail.goodsPrice }}</text>
        </view>
        <text class="stock">库存 {{ detail.goodsStock }} 件</text>
      </view>
    </view>

    <view class="info-card">
      <view class="info-title">{{ detail.goodsName }}</view>
      <view class="info-subtitle">{{ detail.goodsTitle }}</view>
    </view>

    <view v-if="detail.goodsDesc" class="desc-card">
      <view class="desc-title">商品详情</view>
      <view class="desc-content">{{ detail.goodsDesc }}</view>
    </view>

    <!-- 数量选择 -->
    <view class="quantity-card">
      <text class="qty-label">购买数量</text>
      <view class="qty-selector">
        <view class="qty-btn" :class="{ disabled: quantity <= 1 }" @tap="changeQuantity(-1)">-</view>
        <text class="qty-value">{{ quantity }}</text>
        <view class="qty-btn" :class="{ disabled: quantity >= 99 }" @tap="changeQuantity(1)">+</view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="action-bar safe-bottom">
      <view class="action-left">
        <view class="action-icon" @tap="goHome">
          <text class="icon">🏠</text>
          <text class="label">首页</text>
        </view>
        <view class="action-icon" @tap="goCart">
          <text class="icon">🛒</text>
          <text class="label">购物车</text>
        </view>
        <view class="action-icon" @tap="handleFavorite">
          <text class="icon">{{ isFav ? '❤️' : '🤍' }}</text>
          <text class="label">收藏</text>
        </view>
      </view>
      <view class="action-right">
        <view class="btn-cart" :class="{ 'btn-in-cart': isInCart }" @tap="handleAddToCart">
          {{ isInCart ? '✅ 已加入购物车' : '🛒 加入购物车' }}
        </view>
        <view class="btn-buy" @tap="handleBuyNow">🔥 立即购买</view>
      </view>
    </view>
  </view>

  <!-- 加载中 -->
  <view v-else class="loading">
    <text>加载中...</text>
  </view>
  <AiEntry />
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref, onMounted } from 'vue'
import { getGoodsDetail } from '../../api/goods'
import { addToCart, getCartList, deleteCartItem } from '../../api/cart'
import { addFavorite, removeFavorite, isFavorite } from '../../api/favorite'
import { handleImageError, getFullImageUrl } from '../../utils/image'
import { showToast, switchTab, navigateTo, redirectTo } from '../../utils/nav'
import { useAuth } from '../../composables/useAuth'

const { checkLogin } = useAuth()

const detail = ref(null)
const isFav = ref(false)
const isInCart = ref(false)
const cartItemId = ref(null)
const loading = ref(false)

onMounted(async () => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  const id = page.$page?.options?.id || page.options?.id
  if (id) {
    try {
      const res = await getGoodsDetail(id)
      detail.value = res.data
      // 检查是否已在购物车中
      const token = uni.getStorageSync('token')
      if (token) {
        try {
          const cartRes = await getCartList()
          const cartList = cartRes.data || []
          const cartItem = cartList.find(item => item.goodsId == id)
          isInCart.value = !!cartItem
          cartItemId.value = cartItem?.id || null
        } catch (e) {}
      }
    } catch (e) {
      showToast({ title: '加载失败', icon: 'none' })
    }
  }
})

// 数量选择
const quantity = ref(1)
const changeQuantity = (delta) => {
  const newQty = quantity.value + delta
  if (newQty < 1) return
  if (detail.value && detail.value.goodsStock && newQty > detail.value.goodsStock) {
    showToast({ title: '已超过库存数量', icon: 'none' })
    return
  }
  if (newQty > 99) {
    showToast({ title: '最多添加99件', icon: 'none' })
    return
  }
  quantity.value = newQty
}

// 加入/移除购物车
const handleAddToCart = async () => {
  if (!checkLogin()) return
  if (loading.value) return
  loading.value = true
  try {
    if (isInCart.value && cartItemId.value) {
      // 从购物车移除
      await deleteCartItem(cartItemId.value)
      isInCart.value = false
      cartItemId.value = null
      showToast({ title: '已从购物车移除', icon: 'success' })
    } else {
      // 加入购物车
      const res = await addToCart({ goodsId: detail.value.id, quantity: quantity.value })
      isInCart.value = true
      // 重新获取购物车列表以获取 cartItemId
      try {
        const cartRes = await getCartList()
        const cartItem = (cartRes.data || []).find(item => item.goodsId == detail.value.id)
        cartItemId.value = cartItem?.id || null
      } catch (e) {}
      showToast({ title: '已加入购物车', icon: 'success' })
    }
  } catch (e) {} finally {
    loading.value = false
  }
}

// 立即购买（加入购物车并跳转）
const handleBuyNow = async () => {
  if (!checkLogin()) return
  if (loading.value) return
  loading.value = true
  try {
    await addToCart({ goodsId: detail.value.id, quantity: quantity.value })
    navigateTo('/pages/cart/index')
  } catch (e) {} finally {
    loading.value = false
  }
}

// 收藏/取消收藏
const handleFavorite = async () => {
  if (!checkLogin()) return
  try {
    // 先查询当前收藏状态
    const favRes = await isFavorite(detail.value.id)
    if (favRes.data) {
      await removeFavorite(detail.value.id)
      isFav.value = false
      showToast({ title: '已取消收藏', icon: 'success' })
    } else {
      await addFavorite(detail.value.id)
      isFav.value = true
      showToast({ title: '已收藏', icon: 'success' })
    }
  } catch (e) {}
}

// 跳转首页（tabBar页面）
const goHome = () => switchTab('/pages/index/index')

// 跳转购物车（非tabBar页面）
const goCart = () => navigateTo('/pages/cart/index')
</script>

<style scoped>
.page { padding-bottom: 120rpx; }

.hero-img { }
.img { width: 100%; height: 600rpx; display: block; }

.price-card {
  background: #fff; margin: -40rpx 20rpx 0; position: relative;
  border-radius: var(--radius-lg); padding: 28rpx;
  box-shadow: var(--shadow-lg); z-index: 1;
}
.price-row { display: flex; justify-content: space-between; align-items: baseline; }
.price { display: flex; align-items: baseline; }
.price-symbol { font-size: 28rpx; color: var(--danger); font-weight: 600; }
.price-value { font-size: 52rpx; color: var(--danger); font-weight: 700; }
.stock { font-size: 24rpx; color: var(--text-hint); }

.info-card {
  background: #fff; margin: 20rpx; border-radius: var(--radius);
  padding: 28rpx; box-shadow: var(--shadow);
}
.info-title { font-size: 34rpx; font-weight: 700; color: var(--text-primary); line-height: 1.4; }
.info-subtitle { font-size: 26rpx; color: var(--text-secondary); margin-top: 12rpx; }

.desc-card {
  background: #fff; margin: 0 20rpx; border-radius: var(--radius);
  padding: 28rpx; box-shadow: var(--shadow);
}
.desc-title { font-size: 28rpx; font-weight: 600; color: var(--text-primary); margin-bottom: 16rpx; }
.desc-content { font-size: 26rpx; color: var(--text-secondary); line-height: 1.8; }

/* 数量选择器 */
.quantity-card {
  background: #fff; margin: 20rpx; border-radius: var(--radius);
  padding: 28rpx; box-shadow: var(--shadow);
  display: flex; justify-content: space-between; align-items: center;
}
.qty-label { font-size: 28rpx; color: var(--text-primary); font-weight: 500; }
.qty-selector { display: flex; align-items: center; gap: 0; }
.qty-btn {
  width: 64rpx; height: 64rpx; background: #f5f5f5;
  display: flex; align-items: center; justify-content: center;
  font-size: 32rpx; color: #333; font-weight: 600;
}
.qty-btn:first-child { border-radius: 12rpx 0 0 12rpx; }
.qty-btn:last-child { border-radius: 0 12rpx 12rpx 0; }
.qty-btn.disabled { color: #ccc; }
.qty-btn:active { background: #e8e8e8; }
.qty-value {
  width: 80rpx; height: 64rpx; background: #f5f5f5;
  display: flex; align-items: center; justify-content: center;
  font-size: 28rpx; font-weight: 600; border-left: 1rpx solid #e8e8e8;
  border-right: 1rpx solid #e8e8e8;
}

/* 底部操作栏 */
.action-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: #fff; display: flex; align-items: center;
  padding: 16rpx 20rpx; box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.06);
  z-index: 100;
}
.action-left {
  display: flex; gap: 32rpx; margin-right: 20rpx;
}
.action-icon {
  display: flex; flex-direction: column; align-items: center;
  font-size: 20rpx; color: var(--text-secondary);
}
.action-icon .icon { font-size: 40rpx; margin-bottom: 4rpx; }
.action-right {
  flex: 1; display: flex; gap: 0;
}
.btn-cart, .btn-buy {
  flex: 1; height: 80rpx; display: flex;
  align-items: center; justify-content: center;
  font-size: 28rpx; font-weight: 600; color: #fff;
}
.btn-cart {
  background: linear-gradient(135deg, #ff9800, #ff6f00);
  border-radius: 40rpx 0 0 40rpx;
}

.btn-cart.btn-in-cart {
  background: linear-gradient(135deg, #999, #777);
}
.btn-buy {
  background: linear-gradient(135deg, #ef476f, #d63d63);
  border-radius: 0 40rpx 40rpx 0;
}

.loading {
  display: flex; align-items: center; justify-content: center;
  min-height: 100vh; color: var(--text-hint);
}

/* iPhone 底部安全区域 */
.safe-bottom {
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
}
</style>
