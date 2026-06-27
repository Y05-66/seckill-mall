<template>
  <div class="detail-page container" v-if="goods">
    <div class="detail-content">
      <!-- 商品图片 -->
      <div class="detail-img">
        <img :src="goods.goodsImg" :alt="goods.goodsName" @error="handleImageError" />
      </div>

      <!-- 商品信息 -->
      <div class="detail-info">
        <h1 class="detail-name">{{ goods.goodsName }}</h1>
        <p class="detail-title">{{ goods.goodsTitle }}</p>

        <div class="detail-price-box">
          <div class="detail-price">
            <span class="price-symbol">¥</span>
            <span class="price-value">{{ goods.goodsPrice }}</span>
          </div>
          <div class="detail-tags">
            <span class="tag">自营</span>
            <span class="tag">包邮</span>
            <span class="tag">正品保障</span>
          </div>
        </div>

        <div class="detail-meta">
          <div class="meta-item">
            <span class="meta-label">库存</span>
            <span class="meta-value">{{ goods.goodsStock }} 件</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">销量</span>
            <span class="meta-value">{{ getSales(goods) }} 件</span>
          </div>
        </div>

        <div class="detail-desc" v-if="goods.goodsDesc">
          <h3>商品描述</h3>
          <p>{{ goods.goodsDesc }}</p>
        </div>

        <div class="detail-actions">
          <div class="quantity-box">
            <button class="qty-btn" @click="quantity > 1 && quantity--">-</button>
            <span class="qty-value">{{ quantity }}</span>
            <button class="qty-btn" @click="goods && quantity < goods.goodsStock && quantity++">+</button>
          </div>
          <button class="btn-cart" :class="{ 'in-cart': isInCart }" @click="handleAddToCart">
            {{ isInCart ? '✓ 已加入购物车' : '加入购物车' }}
          </button>
          <button class="btn-buy" @click="handleBuyNow">立即购买</button>
          <button class="btn-fav" @click="handleToggleFavorite">
            {{ isFav ? '❤️ 已收藏' : '🤍 收藏' }}
          </button>
          <button class="btn-fav" @click="handleAddToCompare">📊 加入对比</button>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="loading container">
    <p>加载中...</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getGoodsDetail } from '../api/goods'
import { addToCart, getCartList, deleteCartItem } from '../api/cart'
import { addFavorite, removeFavorite, isFavorite } from '../api/favorite'
import { useUserStore } from '../store/user'
import { handleImageError } from '../utils/image'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const goods = ref(null)
const quantity = ref(1)
const isFav = ref(false)
const isInCart = ref(false)
const cartItemId = ref(null)

onMounted(async () => {
  try {
    const res = await getGoodsDetail(route.params.id)
    goods.value = res.data
    // 检查是否已收藏和购物车状态
    if (userStore.token) {
      const [favRes, cartRes] = await Promise.all([
        isFavorite(route.params.id).catch(() => ({ data: false })),
        getCartList().catch(() => ({ data: [] }))
      ])
      isFav.value = favRes.data
      const cartList = cartRes.data || []
      const cartItem = cartList.find(item => item.goodsId == route.params.id)
      isInCart.value = !!cartItem
      cartItemId.value = cartItem?.id || null
    }
  } catch (e) {}
})

// 获取销量（基于库存差值）
const getSales = (item) => {
  if (!item || !item.goodsStock) return 0
  return item.stockCount !== undefined ? Math.max(0, item.goodsStock - item.stockCount) : 0
}

const handleAddToCart = async () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  try {
    if (isInCart.value && cartItemId.value) {
      // 从购物车移除
      await deleteCartItem(cartItemId.value)
      isInCart.value = false
      cartItemId.value = null
      ElMessage.success('已从购物车移除')
    } else {
      // 加入购物车
      await addToCart({ goodsId: goods.value.id, quantity: quantity.value })
      isInCart.value = true
      // 重新获取购物车列表以获取 cartItemId
      try {
        const cartRes = await getCartList()
        const cartItem = (cartRes.data || []).find(item => item.goodsId == goods.value.id)
        cartItemId.value = cartItem?.id || null
      } catch (e) {}
      ElMessage.success('已加入购物车')
    }
  } catch (e) {}
}

const handleBuyNow = async () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  // 如果已在购物车中，直接跳转；否则先加入再跳转
  try {
    if (!isInCart.value) {
      await addToCart({ goodsId: goods.value.id, quantity: quantity.value })
      isInCart.value = true
    }
    router.push('/cart')
  } catch (e) {}
}

const handleToggleFavorite = async () => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  try {
    if (isFav.value) {
      await removeFavorite(goods.value.id)
      isFav.value = false
    } else {
      await addFavorite(goods.value.id)
      isFav.value = true
    }
  } catch (e) {}
}

const handleAddToCompare = () => {
  const saved = localStorage.getItem('compareList')
  let list = saved ? JSON.parse(saved) : []
  if (list.find(i => i.id === goods.value.id)) {
    ElMessage.warning('已在对比列表中')
    return
  }
  if (list.length >= 3) {
    ElMessage.warning('最多对比3个商品')
    return
  }
  list.push({
    id: goods.value.id,
    goodsName: goods.value.goodsName,
    goodsImg: goods.value.goodsImg,
    goodsPrice: goods.value.goodsPrice,
    goodsStock: goods.value.goodsStock,
    goodsDesc: goods.value.goodsDesc
  })
  localStorage.setItem('compareList', JSON.stringify(list))
  ElMessage.success('已加入对比，可前往对比页面查看')
}
</script>

<style scoped>
.detail-page { padding-top: 20px; }

.detail-content {
  display: flex; gap: 30px;
  background: var(--white); border-radius: var(--radius);
  box-shadow: var(--shadow); padding: 24px;
}

.detail-img {
  width: 400px; height: 400px; flex-shrink: 0;
  border-radius: 8px; overflow: hidden;
  border: 1px solid #f0f0f0;
}
.detail-img img { width: 100%; height: 100%; object-fit: cover; }

.detail-info { flex: 1; }
.detail-name {
  font-size: 22px; font-weight: 700; margin-bottom: 8px;
  line-height: 1.4;
}
.detail-title {
  font-size: 14px; color: var(--text-sub); margin-bottom: 16px;
  line-height: 1.5;
}

.detail-price-box {
  background: linear-gradient(135deg, #fff5f5 0%, #fff0f0 100%);
  padding: 16px; border-radius: 8px; margin-bottom: 16px;
}
.detail-price { margin-bottom: 8px; }
.price-symbol { font-size: 18px; color: var(--red); font-weight: 600; }
.price-value { font-size: 32px; font-weight: 800; color: var(--red); }
.detail-tags { display: flex; gap: 8px; }
.tag {
  padding: 2px 8px; border: 1px solid var(--red);
  color: var(--red); border-radius: 4px; font-size: 11px;
}

.detail-meta {
  display: flex; gap: 24px; margin-bottom: 16px;
  padding-bottom: 16px; border-bottom: 1px solid #f0f0f0;
}
.meta-item { display: flex; gap: 8px; }
.meta-label { font-size: 13px; color: var(--text-light); }
.meta-value { font-size: 13px; }

.detail-desc { margin-bottom: 20px; }
.detail-desc h3 { font-size: 15px; margin-bottom: 8px; }
.detail-desc p { font-size: 14px; color: var(--text-sub); line-height: 1.6; }

.detail-actions {
  display: flex; align-items: center; gap: 12px;
}
.quantity-box {
  display: flex; align-items: center;
  border: 1px solid var(--border); border-radius: 6px;
}
.qty-btn {
  width: 36px; height: 36px; border: none; background: #f8f8f8;
  cursor: pointer; font-size: 18px; display: flex;
  align-items: center; justify-content: center;
}
.qty-btn:hover { background: #eee; }
.qty-value {
  width: 50px; height: 36px; display: flex;
  align-items: center; justify-content: center;
  font-size: 14px; border-left: 1px solid var(--border);
  border-right: 1px solid var(--border);
}

.btn-cart {
  padding: 12px 32px; background: var(--orange); color: white;
  border: none; border-radius: 8px; font-size: 16px;
  font-weight: 600; cursor: pointer;
}

.btn-cart.in-cart {
  background: #999;
  cursor: default;
}

.btn-cart:hover { opacity: 0.9; }

.btn-buy {
  padding: 12px 32px; background: var(--red); color: white;
  border: none; border-radius: 8px; font-size: 16px;
  font-weight: 600; cursor: pointer;
}
.btn-buy:hover { background: var(--red-dark); }

.btn-fav {
  padding: 12px 24px; background: white; color: var(--text);
  border: 1px solid var(--border); border-radius: 8px; font-size: 14px;
  cursor: pointer;
}
.btn-fav:hover { border-color: var(--red); color: var(--red); }

.loading { text-align: center; padding: 80px 0; }
</style>
