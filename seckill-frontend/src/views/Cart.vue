<template>
  <div class="cart-page container">
    <div class="page-header">
      <h2>购物车</h2>
      <span class="cart-count">共 {{ cartList.length }} 件商品</span>
    </div>

    <div v-if="cartList.length > 0" class="cart-content">
      <!-- 购物车列表 -->
      <div class="cart-list">
        <div class="cart-item" v-for="item in cartList" :key="item.id">
          <input type="checkbox" :checked="item.checked === 1" @change="handleToggleCheck(item)" class="cart-checkbox" />
          <img :src="item.goodsImg" :alt="item.goodsName" class="cart-img" @error="handleImageError" />
          <div class="cart-info">
            <div class="cart-name">{{ item.goodsName }}</div>
            <div class="cart-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ item.goodsPrice }}</span>
            </div>
          </div>
          <div class="cart-quantity">
            <button class="qty-btn" @click="handleQuantityChange(item, item.quantity - 1)">-</button>
            <span class="qty-value">{{ item.quantity }}</span>
            <button class="qty-btn" @click="handleQuantityChange(item, item.quantity + 1)">+</button>
          </div>
          <div class="cart-subtotal">
            <span class="subtotal-symbol">¥</span>
            <span class="subtotal-value">{{ (item.goodsPrice * item.quantity).toFixed(2) }}</span>
          </div>
          <button class="cart-delete" @click="handleDelete(item)">删除</button>
        </div>
      </div>

      <!-- 底部结算栏 -->
      <div class="cart-footer">
        <div class="footer-left">
          <input type="checkbox" :checked="isAllChecked" @change="handleCheckAll" class="cart-checkbox" />
          <span>全选</span>
          <button class="clear-btn" @click="handleClear">清空购物车</button>
        </div>
        <div class="footer-right">
          <div class="total-info">
            <span>已选 {{ checkedCount }} 件商品</span>
            <span class="total-label">合计：</span>
            <span class="total-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ totalPrice.toFixed(2) }}</span>
            </span>
          </div>
          <button class="checkout-btn" :disabled="checkedCount === 0" @click="handleCheckout">
            去结算 ({{ checkedCount }})
          </button>
        </div>
      </div>
    </div>

    <div v-else class="empty-cart">
      <div class="empty-icon">🛒</div>
      <p>购物车是空的</p>
      <button class="go-shopping" @click="$router.push('/goods')">去逛逛</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getCartList, updateCartQuantity, deleteCartItem, clearCart, toggleCheck, toggleCheckAll } from '../api/cart'
import { handleImageError } from '../utils/image'

const router = useRouter()
const cartList = ref([])

onMounted(loadCart)

async function loadCart() {
  try {
    const res = await getCartList()
    cartList.value = res.data || []
  } catch (e) {}
}

const isAllChecked = computed(() => {
  return cartList.value.length > 0 && cartList.value.every(item => item.checked === 1)
})

const checkedCount = computed(() => {
  return cartList.value.filter(item => item.checked === 1).reduce((sum, item) => sum + item.quantity, 0)
})

const totalPrice = computed(() => {
  return cartList.value
    .filter(item => item.checked === 1)
    .reduce((sum, item) => sum + item.goodsPrice * item.quantity, 0)
})

const handleToggleCheck = async (item) => {
  const newChecked = item.checked === 1 ? 0 : 1
  const oldChecked = item.checked
  // 乐观更新：先更新UI
  item.checked = newChecked
  try {
    await toggleCheck({ cartId: item.id, checked: newChecked })
  } catch (e) {
    // 失败时回滚
    item.checked = oldChecked
  }
}

const handleCheckAll = async () => {
  const newChecked = isAllChecked.value ? 0 : 1
  const oldStates = cartList.value.map(item => ({ id: item.id, checked: item.checked }))
  // 乐观更新：先更新所有项
  cartList.value.forEach(item => { item.checked = newChecked })
  try {
    await toggleCheckAll({ checked: newChecked })
  } catch (e) {
    // 失败时回滚
    oldStates.forEach(old => {
      const item = cartList.value.find(i => i.id === old.id)
      if (item) item.checked = old.checked
    })
  }
}

const handleQuantityChange = async (item, newQuantity) => {
  if (newQuantity < 1) return
  const oldQuantity = item.quantity
  // 乐观更新：先更新UI
  item.quantity = newQuantity
  try {
    await updateCartQuantity({ cartId: item.id, quantity: newQuantity })
  } catch (e) {
    // 失败时回滚
    item.quantity = oldQuantity
  }
}

const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm('确认删除该商品？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch { return }
  try {
    await deleteCartItem(item.id)
    await loadCart()
    ElMessage.success('已删除')
  } catch (e) {}
}

const handleClear = async () => {
  try {
    await ElMessageBox.confirm('确认清空购物车？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch { return }
  try {
    await clearCart()
    await loadCart()
    ElMessage.success('购物车已清空')
  } catch (e) {}
}

const handleCheckout = () => {
  // TODO: 跳转到结算页面
  ElMessage.info('结算功能开发中')
}
</script>

<style scoped>
.cart-page {}

.page-header {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 20px;
}
.page-header h2 { font-size: 22px; font-weight: 700; }
.cart-count { font-size: 14px; color: var(--text-light); }

.cart-content {
  background: var(--white); border-radius: var(--radius);
  box-shadow: var(--shadow); overflow: hidden;
}

.cart-list { padding: 0 20px; }
.cart-item {
  display: flex; align-items: center; gap: 16px;
  padding: 16px 0; border-bottom: 1px solid #f5f5f5;
}
.cart-item:last-child { border-bottom: none; }

.cart-checkbox {
  width: 18px; height: 18px; cursor: pointer;
  accent-color: var(--red);
}

.cart-img {
  width: 80px; height: 80px; object-fit: cover;
  border-radius: 8px; border: 1px solid #f0f0f0;
}

.cart-info { flex: 1; }
.cart-name {
  font-size: 14px; font-weight: 500; margin-bottom: 8px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.cart-price { color: var(--red); }
.price-symbol { font-size: 12px; }
.price-value { font-size: 16px; font-weight: 700; }

.cart-quantity {
  display: flex; align-items: center; gap: 0;
  border: 1px solid var(--border); border-radius: 6px;
}
.qty-btn {
  width: 32px; height: 32px; border: none; background: #f8f8f8;
  cursor: pointer; font-size: 16px; display: flex;
  align-items: center; justify-content: center;
}
.qty-btn:hover { background: #eee; }
.qty-value {
  width: 40px; height: 32px; display: flex;
  align-items: center; justify-content: center;
  font-size: 14px; border-left: 1px solid var(--border);
  border-right: 1px solid var(--border);
}

.cart-subtotal { min-width: 100px; text-align: right; }
.subtotal-symbol { font-size: 12px; color: var(--red); }
.subtotal-value { font-size: 16px; font-weight: 700; color: var(--red); }

.cart-delete {
  padding: 6px 12px; border: 1px solid var(--border);
  border-radius: 4px; background: white; cursor: pointer;
  font-size: 12px; color: var(--text-sub);
}
.cart-delete:hover { color: var(--red); border-color: var(--red); }

.cart-footer {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 20px; background: #fafafa;
  border-top: 1px solid var(--border);
}
.footer-left { display: flex; align-items: center; gap: 12px; }
.clear-btn {
  padding: 4px 8px; border: none; background: none;
  cursor: pointer; font-size: 12px; color: var(--text-light);
}
.clear-btn:hover { color: var(--red); }

.footer-right { display: flex; align-items: center; gap: 20px; }
.total-info { display: flex; align-items: baseline; gap: 8px; font-size: 14px; }
.total-label { color: var(--text-sub); }
.total-price { color: var(--red); }
.total-price .price-symbol { font-size: 14px; }
.total-price .price-value { font-size: 24px; font-weight: 800; }

.checkout-btn {
  padding: 12px 32px; background: var(--red); color: white;
  border: none; border-radius: 8px; font-size: 16px;
  font-weight: 600; cursor: pointer;
}
.checkout-btn:hover { background: var(--red-dark); }
.checkout-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.empty-cart {
  text-align: center; padding: 80px 0;
  background: var(--white); border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.empty-icon { font-size: 64px; margin-bottom: 16px; }
.empty-cart p { font-size: 16px; color: var(--text-sub); margin-bottom: 20px; }
.go-shopping {
  padding: 10px 24px; background: var(--red); color: white;
  border: none; border-radius: 8px; font-size: 14px;
  font-weight: 600; cursor: pointer;
}
</style>
