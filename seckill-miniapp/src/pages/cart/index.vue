<template>
  <view class="page">
    <view v-if="cartList.length > 0">
      <view class="cart-item" v-for="item in cartList" :key="item.id">
        <view class="item-check" @tap="handleToggleCheck(item)">
          <text :class="['check-icon', item.checked === 1 ? 'checked' : '']">
            {{ item.checked === 1 ? '✓' : '' }}
          </text>
        </view>
        <image :src="getFullImageUrl(item.goodsImg)" mode="aspectFill" class="item-img" @error="handleImageError" />
        <view class="item-info">
          <text class="item-name">{{ item.goodsName }}</text>
          <text class="item-price">¥{{ item.goodsPrice }}</text>
          <view class="item-qty">
            <text class="qty-btn" @tap="handleQtyChange(item, -1)">-</text>
            <text class="qty-num">{{ item.quantity }}</text>
            <text class="qty-btn" @tap="handleQtyChange(item, 1)">+</text>
          </view>
        </view>
        <text class="item-delete" @tap="handleDelete(item)">删除</text>
      </view>

      <!-- 底部结算 -->
      <view class="cart-footer">
        <view class="footer-left" @tap="handleCheckAll">
          <text :class="['check-icon', isAllChecked ? 'checked' : '']">
            {{ isAllChecked ? '✓' : '' }}
          </text>
          <text>全选</text>
        </view>
        <view class="footer-right">
          <text class="total-label">合计：</text>
          <text class="total-price">¥{{ totalPrice.toFixed(2) }}</text>
          <view class="checkout-btn" @tap="handleCheckout">去结算({{ checkedCount }})</view>
        </view>
      </view>
    </view>

    <view v-else class="empty">
      <text class="empty-icon">🛒</text>
      <text class="empty-text">购物车是空的</text>
      <view class="empty-btn" @tap="goGoods">去逛逛</view>
    </view>
    <AiEntry />
  </view>
</template>

<script setup>
import AiEntry from '@/components/AiEntry.vue'

import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCartList, updateCartQuantity, deleteCartItem, toggleCheck, toggleCheckAll } from '../../api/cart'
import { handleImageError, getFullImageUrl } from '../../utils/image'
import { switchTab, showToast, showModal } from '../../utils/nav'

const cartList = ref([])

const loadCart = async () => {
  try { const r = await getCartList(); cartList.value = r.data || [] } catch (e) {}
}

onShow(loadCart)

const isAllChecked = computed(() => cartList.value.length > 0 && cartList.value.every(i => i.checked === 1))
const checkedCount = computed(() => cartList.value.filter(i => i.checked === 1).reduce((s, i) => s + i.quantity, 0))
const totalPrice = computed(() => cartList.value.filter(i => i.checked === 1).reduce((s, i) => s + i.goodsPrice * i.quantity, 0))

const handleToggleCheck = async (item) => {
  const newChecked = item.checked === 1 ? 0 : 1
  const oldChecked = item.checked
  item.checked = newChecked // 乐观更新
  try { await toggleCheck({ cartId: item.id, checked: newChecked }) } catch (e) { item.checked = oldChecked }
}
const handleCheckAll = async () => {
  const newChecked = isAllChecked.value ? 0 : 1
  const oldStates = cartList.value.map(i => ({ id: i.id, checked: i.checked }))
  cartList.value.forEach(i => { i.checked = newChecked }) // 乐观更新
  try { await toggleCheckAll({ checked: newChecked }) } catch (e) {
    oldStates.forEach(old => { const item = cartList.value.find(i => i.id === old.id); if (item) item.checked = old.checked })
  }
}
const handleQtyChange = async (item, delta) => {
  const newQty = item.quantity + delta
  if (newQty < 1) return
  const oldQty = item.quantity
  item.quantity = newQty // 乐观更新
  try { await updateCartQuantity({ cartId: item.id, quantity: newQty }) } catch (e) { item.quantity = oldQty }
}
const handleDelete = async (item) => {
  try {
    const res = await showModal({ title: '提示', content: '确认删除该商品？' })
    if (!res.confirm) return
    await deleteCartItem(item.id)
    await loadCart()
    showToast({ title: '已删除', icon: 'success' })
  } catch (e) {}
}
const goGoods = () => switchTab('/pages/index/index')
const handleCheckout = () => showToast({ title: '结算功能开发中', icon: 'none' })
</script>

<style scoped>
.page { background: #f5f5f5; min-height: 100vh; padding: 20rpx; padding-bottom: 120rpx; }

.cart-item {
  display: flex; align-items: center; gap: 16rpx;
  background: #fff; border-radius: 16rpx; padding: 20rpx;
  margin-bottom: 16rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}
.item-check { padding: 8rpx; }
.check-icon {
  width: 40rpx; height: 40rpx; border: 2rpx solid #ddd;
  border-radius: 50%; display: flex; align-items: center;
  justify-content: center; font-size: 24rpx; color: #fff;
}
.check-icon.checked { background: #e4393c; border-color: #e4393c; }
.item-img { width: 160rpx; height: 160rpx; border-radius: 12rpx; }
.item-info { flex: 1; }
.item-name { font-size: 26rpx; display: block; margin-bottom: 8rpx; }
.item-price { font-size: 30rpx; color: #e4393c; font-weight: 700; display: block; margin-bottom: 8rpx; }
.item-qty { display: flex; align-items: center; gap: 0; border: 2rpx solid #eee; border-radius: 8rpx; width: fit-content; }
.qty-btn { width: 56rpx; height: 56rpx; display: flex; align-items: center; justify-content: center; font-size: 28rpx; }
.qty-num { width: 64rpx; text-align: center; font-size: 26rpx; border-left: 2rpx solid #eee; border-right: 2rpx solid #eee; }
.item-delete { color: #999; font-size: 24rpx; padding: 8rpx; }

.cart-footer {
  position: fixed; bottom: 0; left: 0; right: 0;
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; padding: 20rpx 24rpx;
  box-shadow: 0 -2rpx 12rpx rgba(0,0,0,0.06);
}
.footer-left { display: flex; align-items: center; gap: 12rpx; }
.footer-right { display: flex; align-items: center; gap: 12rpx; }
.total-label { font-size: 26rpx; color: #666; }
.total-price { font-size: 36rpx; color: #e4393c; font-weight: 800; }
.checkout-btn {
  padding: 16rpx 32rpx; background: #e4393c; color: #fff;
  border-radius: 12rpx; font-size: 28rpx; font-weight: 600;
}

.empty { text-align: center; padding: 120rpx 0; }
.empty-icon { font-size: 100rpx; display: block; margin-bottom: 16rpx; }
.empty-text { font-size: 28rpx; color: #999; display: block; margin-bottom: 24rpx; }
.empty-btn {
  display: inline-block; padding: 16rpx 48rpx; background: #e4393c;
  color: #fff; border-radius: 12rpx; font-size: 28rpx;
}
</style>
