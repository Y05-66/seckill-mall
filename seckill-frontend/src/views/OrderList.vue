<template>
  <div class="order-page container">
    <div class="page-bar">
      <span class="page-title">我的订单</span>
      <span class="page-count">共 {{ filteredList.length }} 笔</span>
    </div>

    <!-- 状态筛选标签 -->
    <div class="status-tabs">
      <span
        v-for="tab in tabs" :key="tab.value"
        class="tab-item" :class="{ active: currentTab === tab.value }"
        @click="currentTab = tab.value"
      >
        {{ tab.label }}
        <span v-if="tab.value === 0 && unpaidCount > 0" class="tab-badge">{{ unpaidCount }}</span>
      </span>
    </div>

    <div class="order-list" v-if="filteredList.length > 0">
      <div v-for="order in filteredList" :key="order.id" class="order-card fade-in">
        <div class="oc-header">
          <span class="oc-no">订单号: {{ order.orderNo }}</span>
          <span class="oc-status" :class="'s' + order.status">{{ order.statusDesc }}</span>
        </div>
        <div class="oc-body">
          <div class="oc-info">
            <div class="oc-name">{{ order.goodsName }}</div>
            <div class="oc-time">{{ order.createTime }}</div>
          </div>
          <div class="oc-price">
            <span class="oc-yen">¥</span>
            <span class="oc-num">{{ order.seckillPrice }}</span>
          </div>
          <div class="oc-actions" v-if="order.status === 0">
            <button class="btn-pay" @click="handlePay(order)">立即支付</button>
            <button class="btn-cancel" @click="handleCancel(order)">取消</button>
          </div>
          <div class="oc-actions" v-if="order.status === 1">
            <button class="btn-refund" @click="handleRefund(order)">申请退款</button>
          </div>
          <div class="oc-actions" v-if="order.status >= 2">
            <button class="btn-delete" @click="handleDelete(order)">删除</button>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty">
      <div class="empty-icon">📦</div>
      <p v-if="currentTab === -1">暂无订单</p>
      <p v-else>暂无{{ tabs.find(t => t.value === currentTab)?.label }}订单</p>
      <button class="btn-go" @click="$router.push('/seckill')">去秒杀</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, payOrder, cancelOrder, deleteOrder } from '../api/order'
import { applyRefund } from '../api/refund'

const route = useRoute()
const orderList = ref([])

/** 当前选中的标签值，-1=全部，0=待支付，1=已支付，2=已取消，3=已超时，4=已退款 */
const currentTab = ref(-1)

/** 标签配置 */
const tabs = [
  { label: '全部', value: -1 },
  { label: '待支付', value: 0 },
  { label: '已支付', value: 1 },
  { label: '已取消', value: 2 },
  { label: '已超时', value: 3 },
  { label: '已退款', value: 4 }
]

/** 未支付订单数量（用于标签角标显示） */
const unpaidCount = computed(() => {
  return orderList.value.filter(o => o.status === 0).length
})

/** 根据标签筛选订单列表 */
const filteredList = computed(() => {
  if (currentTab.value === -1) return orderList.value
  return orderList.value.filter(o => o.status === currentTab.value)
})

onMounted(() => {
  // 从URL参数读取初始状态筛选（从Profile页面快捷入口跳转时携带）
  const statusParam = route.query.status
  if (statusParam !== undefined) {
    currentTab.value = Number(statusParam)
  }
  loadOrders()
})

/** 加载订单列表 */
async function loadOrders() {
  try { const r = await getOrderList(); orderList.value = r.data || [] } catch (e) {}
}

/** 支付订单 */
const handlePay = async (order) => {
  try {
    await ElMessageBox.confirm('确认支付该订单？', '提示', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'info'
    })
  } catch { return }
  try {
    await payOrder(order.orderNo)
    ElMessage.success('支付成功')
    await loadOrders()
  } catch (e) {}
}

/** 取消订单（释放库存） */
const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确认取消该订单？取消后将释放库存。', '提示', {
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
      type: 'warning'
    })
  } catch { return }
  try {
    await cancelOrder(order.orderNo)
    ElMessage.success('订单已取消')
    await loadOrders()
  } catch (e) {}
}

/** 申请退款 */
const handleRefund = async (order) => {
  try {
    await ElMessageBox.confirm(`确认对订单 ${order.orderNo} 申请退款？`, '申请退款', {
      confirmButtonText: '确认退款',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch { return }
  try {
    await applyRefund({
      orderNo: order.orderNo,
      type: 1,
      amount: order.seckillPrice,
      reason: '用户主动申请退款'
    })
    ElMessage.success('退款申请已提交')
    await loadOrders()
  } catch (e) {}
}

/** 删除订单 */
const handleDelete = async (order) => {
  try {
    await ElMessageBox.confirm(`确认删除订单 ${order.orderNo}？删除后无法恢复。`, '删除订单', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch { return }
  try {
    await deleteOrder(order.orderNo)
    ElMessage.success('订单已删除')
    await loadOrders()
  } catch (e) {}
}
</script>

<style scoped>
.order-page {}

.page-bar {
  display: flex; align-items: center; gap: 8px;
  background: var(--white); padding: 14px 20px;
  border-radius: var(--radius); box-shadow: var(--shadow);
  margin-bottom: 12px;
}
.page-title { font-size: 18px; font-weight: 700; }
.page-count { font-size: 13px; color: var(--text-light); }

/* 状态筛选标签 */
.status-tabs {
  display: flex; gap: 8px; margin-bottom: 16px;
  background: var(--white); padding: 12px 16px;
  border-radius: var(--radius); box-shadow: var(--shadow);
  overflow-x: auto;
}
.tab-item {
  padding: 8px 16px; border-radius: 20px;
  font-size: 13px; color: var(--text-sub);
  cursor: pointer; transition: all 0.2s;
  white-space: nowrap; position: relative;
}
.tab-item:hover { background: #f5f5f5; }
.tab-item.active {
  background: var(--red); color: white;
  font-weight: 600;
}
.tab-badge {
  position: absolute; top: -6px; right: -6px;
  min-width: 18px; height: 18px; line-height: 18px;
  background: var(--orange); color: white;
  font-size: 11px; text-align: center;
  border-radius: 9px; padding: 0 4px;
}

.order-list { display: flex; flex-direction: column; gap: 12px; }
.order-card {
  background: var(--white); border-radius: var(--radius);
  box-shadow: var(--shadow); overflow: hidden;
  transition: box-shadow 0.2s;
}
.order-card:hover { box-shadow: var(--shadow-hover); }
.oc-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 20px; background: #fafafa; border-bottom: 1px solid #f0f0f0;
}
.oc-no { font-size: 13px; color: var(--text-sub); }
.oc-status { font-size: 12px; font-weight: 600; padding: 3px 10px; border-radius: 12px; }
.oc-status.s0 { background: #fff7e6; color: #fa8c16; }
.oc-status.s1 { background: #f6ffed; color: #52c41a; }
.oc-status.s2 { background: #f0f0f0; color: #999; }
.oc-status.s3 { background: #fff1f0; color: var(--red); }
.oc-status.s4 { background: #e6f7ff; color: #1890ff; }

.oc-body { display: flex; align-items: center; padding: 16px 20px; gap: 24px; }
.oc-info { flex: 1; }
.oc-name { font-size: 15px; font-weight: 500; margin-bottom: 4px; }
.oc-time { font-size: 12px; color: var(--text-light); }
.oc-price { color: var(--red); }
.oc-yen { font-size: 13px; font-weight: 600; }
.oc-num { font-size: 22px; font-weight: 800; }
.oc-actions { display: flex; gap: 8px; }
.btn-pay {
  padding: 8px 20px; border: none; border-radius: 4px;
  background: var(--red); color: white; font-size: 13px;
  font-weight: 600; cursor: pointer;
}
.btn-pay:hover { background: var(--red-dark); }
.btn-cancel {
  padding: 8px 20px; border: 1px solid var(--border); border-radius: 4px;
  background: white; font-size: 13px; cursor: pointer;
}
.btn-refund {
  padding: 8px 20px; border: 1px solid var(--orange); border-radius: 4px;
  background: white; color: var(--orange); font-size: 13px;
  font-weight: 600; cursor: pointer;
}
.btn-refund:hover { background: #fff7e6; }
.btn-delete {
  padding: 8px 20px; border: 1px solid #999; border-radius: 4px;
  background: white; color: #999; font-size: 13px;
  font-weight: 600; cursor: pointer;
}
.btn-delete:hover { background: #f5f5f5; color: #666; border-color: #666; }

.empty { text-align: center; padding: 80px 0; color: var(--text-light); }
.empty-icon { font-size: 64px; margin-bottom: 16px; opacity: 0.6; }
.btn-go {
  margin-top: 16px; padding: 10px 28px; border: none; border-radius: 6px;
  background: var(--red); color: white; font-size: 14px;
  font-weight: 600; cursor: pointer;
}
</style>
