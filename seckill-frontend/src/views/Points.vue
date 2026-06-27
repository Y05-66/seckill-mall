<template>
  <div class="points-page container">
    <div class="points-card">
      <div class="points-header">
        <div class="points-value">
          <span class="points-num">{{ points.points || 0 }}</span>
          <span class="points-label">积分</span>
        </div>
      </div>
      <div class="points-stats">
        <div class="stat-item">
          <span class="stat-num">{{ points.totalEarned || 0 }}</span>
          <span class="stat-label">累计获得</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ points.totalUsed || 0 }}</span>
          <span class="stat-label">累计使用</span>
        </div>
      </div>
    </div>

    <div class="section">
      <h3 class="section-title">积分记录</h3>
      <div class="log-list" v-if="pointsLog.length > 0">
        <div v-for="item in pointsLog" :key="item.id" class="log-item">
          <div class="log-info">
            <div class="log-desc">{{ item.description }}</div>
            <div class="log-time">{{ item.createTime }}</div>
          </div>
          <div class="log-points" :class="{ earn: item.points > 0 }">
            {{ item.points > 0 ? '+' : '' }}{{ item.points }}
          </div>
        </div>
      </div>
      <div v-else class="empty">暂无积分记录</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyPoints, getPointsLog } from '../api/points'

const points = ref({})
const pointsLog = ref([])

onMounted(async () => {
  try {
    const [p, l] = await Promise.all([getMyPoints(), getPointsLog()])
    points.value = p.data || {}
    pointsLog.value = l.data || []
  } catch (e) {}
})
</script>

<style scoped>
.points-page {}

.points-card {
  background: linear-gradient(135deg, var(--red) 0%, #ff6b81 100%);
  border-radius: var(--radius); padding: 24px; color: white;
  margin-bottom: 24px;
}
.points-header { text-align: center; margin-bottom: 20px; }
.points-num { font-size: 48px; font-weight: 800; }
.points-label { font-size: 16px; margin-left: 8px; opacity: 0.9; }
.points-stats { display: flex; justify-content: center; gap: 40px; }
.stat-item { text-align: center; }
.stat-num { display: block; font-size: 20px; font-weight: 700; }
.stat-label { font-size: 13px; opacity: 0.8; }

.section { margin-top: 24px; }
.section-title { font-size: 18px; font-weight: 600; margin-bottom: 16px; }

.log-list { background: var(--white); border-radius: var(--radius); box-shadow: var(--shadow); }
.log-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 20px; border-bottom: 1px solid #f5f5f5;
}
.log-item:last-child { border-bottom: none; }
.log-desc { font-size: 14px; margin-bottom: 4px; }
.log-time { font-size: 12px; color: var(--text-light); }
.log-points { font-size: 16px; font-weight: 700; color: var(--text-light); }
.log-points.earn { color: var(--red); }

.empty { text-align: center; padding: 40px; color: var(--text-light); }
</style>
