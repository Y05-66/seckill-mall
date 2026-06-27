<template>
  <div class="dashboard">
    <div class="stat-row">
      <div class="stat-card" v-for="s in statsCards" :key="s.label">
        <div class="sc-icon">{{ s.icon }}</div>
        <div class="sc-info">
          <div class="sc-value">{{ s.value }}</div>
          <div class="sc-label">{{ s.label }}</div>
        </div>
      </div>
    </div>

    <div class="detail-row">
      <div class="detail-card">
        <h3>订单状态分布</h3>
        <div ref="pieChart" class="chart-container"></div>
      </div>
      <div class="detail-card">
        <h3>营收概览</h3>
        <div class="revenue">
          <span class="rv-yen">¥</span>
          <span class="rv-num">{{ stats.totalRevenue || '0.00' }}</span>
        </div>
        <p class="rv-label">累计营收</p>
        <div class="rv-row">
          <div class="rv-item">
            <span class="rv-sub-label">今日订单</span>
            <span class="rv-sub-val">{{ stats.todayOrders || 0 }}</span>
          </div>
          <div class="rv-item">
            <span class="rv-sub-label">订单总数</span>
            <span class="rv-sub-val">{{ stats.totalOrders || 0 }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getOrderStatistics } from '../../api/admin'

echarts.use([PieChart, TooltipComponent, LegendComponent, CanvasRenderer])

const stats = ref({})
const pieChart = ref(null)
let chartInstance = null
let resizeHandler = null

onMounted(async () => {
  try {
    const r = await getOrderStatistics()
    stats.value = r.data || {}
    await nextTick()
    initChart()
  } catch (e) {}
})

const statsCards = computed(() => [
  { label: '订单总数', value: stats.value.totalOrders || 0, icon: '📦' },
  { label: '今日订单', value: stats.value.todayOrders || 0, icon: '📊' },
  { label: '已支付', value: stats.value.paidCount || 0, icon: '✅' },
  { label: '总营收', value: `¥${stats.value.totalRevenue || '0.00'}`, icon: '💰' },
])

const initChart = () => {
  if (!pieChart.value) return
  const chart = echarts.init(pieChart.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { fontSize: 12 } },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      label: { show: true, fontSize: 12 },
      data: [
        { value: stats.value.unpaidCount || 0, name: '待支付', itemStyle: { color: '#fa8c16' } },
        { value: stats.value.paidCount || 0, name: '已支付', itemStyle: { color: '#52c41a' } },
        { value: stats.value.cancelledCount || 0, name: '已取消', itemStyle: { color: '#999' } },
        { value: stats.value.timeOutCount || 0, name: '已超时', itemStyle: { color: '#e4393c' } },
      ]
    }]
  })
  resizeHandler = () => chart.resize()
  window.addEventListener('resize', resizeHandler)
  chartInstance = chart
}

onUnmounted(() => {
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
    resizeHandler = null
  }
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.dashboard {}

.stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 16px; }
.stat-card {
  display: flex; align-items: center; gap: 14px;
  background: var(--white); padding: 18px 16px;
  border-radius: var(--radius); box-shadow: var(--shadow);
}
.sc-icon { font-size: 32px; }
.sc-value { font-size: 22px; font-weight: 800; }
.sc-label { font-size: 13px; color: var(--text-sub); margin-top: 2px; }

.detail-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.detail-card {
  background: var(--white); border-radius: var(--radius);
  padding: 20px; box-shadow: var(--shadow);
}
.detail-card h3 { font-size: 16px; margin-bottom: 16px; }

.chart-container { width: 100%; height: 280px; }

.revenue { text-align: center; margin: 20px 0 4px; }
.rv-yen { font-size: 20px; color: var(--red); font-weight: 700; }
.rv-num { font-size: 36px; font-weight: 800; color: var(--red); }
.rv-label { text-align: center; font-size: 13px; color: var(--text-light); margin-bottom: 20px; }
.rv-row { display: flex; justify-content: center; gap: 32px; }
.rv-item { text-align: center; }
.rv-sub-label { display: block; font-size: 13px; color: var(--text-sub); margin-bottom: 4px; }
.rv-sub-val { font-size: 20px; font-weight: 700; }
</style>
