<template>
  <div class="coupons-page container">
    <div class="page-header">
      <h2>优惠券中心</h2>
    </div>

    <!-- 可领取的优惠券 -->
    <div class="section">
      <h3 class="section-title">可领取优惠券</h3>
      <div class="coupon-grid" v-if="availableCoupons.length > 0">
        <div v-for="item in availableCoupons" :key="item.id" class="coupon-card">
          <div class="coupon-left">
            <div class="coupon-value">
              <span v-if="item.type === 1" class="coupon-yen">¥</span>
              <span class="coupon-num">{{ item.type === 1 ? item.value : (item.value * 10) }}</span>
              <span v-if="item.type === 2" class="coupon-unit">折</span>
            </div>
            <div class="coupon-condition">
              {{ item.minAmount > 0 ? `满${item.minAmount}可用` : '无门槛' }}
            </div>
          </div>
          <div class="coupon-right">
            <div class="coupon-name">{{ item.name }}</div>
            <div class="coupon-time">{{ item.endTime }} 截止</div>
            <button class="coupon-btn" @click="handleClaim(item)">立即领取</button>
          </div>
        </div>
      </div>
      <div v-else class="empty">暂无可领取的优惠券</div>
    </div>

    <!-- 我的优惠券 -->
    <div class="section">
      <h3 class="section-title">我的优惠券</h3>
      <div class="coupon-grid" v-if="myCoupons.length > 0">
        <div v-for="item in myCoupons" :key="item.id" class="coupon-card" :class="{ used: item.status === 1 }">
          <div class="coupon-left">
            <div class="coupon-value">
              <span v-if="item.type === 1" class="coupon-yen">¥</span>
              <span class="coupon-num">{{ item.type === 1 ? item.value : (item.value * 10) }}</span>
              <span v-if="item.type === 2" class="coupon-unit">折</span>
            </div>
            <div class="coupon-condition">
              {{ item.minAmount > 0 ? `满${item.minAmount}可用` : '无门槛' }}
            </div>
          </div>
          <div class="coupon-right">
            <div class="coupon-name">{{ item.couponName }}</div>
            <div class="coupon-status">
              {{ item.status === 0 ? '未使用' : item.status === 1 ? '已使用' : '已过期' }}
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty">暂无优惠券</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvailableCoupons, claimCoupon, getMyCoupons } from '../api/coupon'

const availableCoupons = ref([])
const myCoupons = ref([])

onMounted(async () => {
  try {
    const [a, m] = await Promise.all([getAvailableCoupons(), getMyCoupons()])
    availableCoupons.value = a.data || []
    myCoupons.value = m.data || []
  } catch (e) {}
})

const handleClaim = async (coupon) => {
  try {
    await claimCoupon(coupon.id)
    ElMessage.success('领取成功')
    const [a, m] = await Promise.all([getAvailableCoupons(), getMyCoupons()])
    availableCoupons.value = a.data || []
    myCoupons.value = m.data || []
  } catch (e) {}
}
</script>

<style scoped>
.coupons-page {}

.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 22px; font-weight: 700; }

.section { margin-bottom: 32px; }
.section-title { font-size: 18px; font-weight: 600; margin-bottom: 16px; }

.coupon-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.coupon-card {
  display: flex; background: var(--white); border-radius: var(--radius);
  overflow: hidden; box-shadow: var(--shadow);
  border-left: 4px solid var(--red);
}
.coupon-card.used { opacity: 0.6; border-left-color: #ccc; }

.coupon-left {
  width: 120px; padding: 16px; text-align: center;
  background: var(--red-light); display: flex; flex-direction: column;
  justify-content: center;
}
.coupon-card.used .coupon-left { background: #f5f5f5; }
.coupon-value { color: var(--red); }
.coupon-card.used .coupon-value { color: #999; }
.coupon-yen { font-size: 14px; font-weight: 600; }
.coupon-num { font-size: 28px; font-weight: 800; }
.coupon-unit { font-size: 14px; }
.coupon-condition { font-size: 12px; color: var(--text-light); margin-top: 4px; }

.coupon-right { flex: 1; padding: 16px; display: flex; flex-direction: column; justify-content: center; }
.coupon-name { font-size: 15px; font-weight: 600; margin-bottom: 6px; }
.coupon-time { font-size: 12px; color: var(--text-light); margin-bottom: 10px; }
.coupon-status { font-size: 13px; color: var(--text-sub); }
.coupon-btn {
  align-self: flex-start; padding: 6px 16px;
  background: var(--red); color: white; border: none;
  border-radius: 4px; font-size: 13px; cursor: pointer;
}
.coupon-btn:hover { background: var(--red-dark); }

.empty {
  text-align: center; padding: 40px; color: var(--text-light);
  background: var(--white); border-radius: var(--radius);
}
</style>
