<template>
  <div class="seckill-page container">
    <!-- 秒杀头部 -->
    <div class="seckill-header">
      <div class="sh-left">
        <h1>🔥 限时秒杀</h1>
        <p>全场低至1折，限时抢购中</p>
      </div>
      <div class="sh-right">
        <span class="sh-label">距离结束</span>
        <div class="sh-timer">
          <span class="st-num">{{ hh }}</span>
          <span class="st-sep">:</span>
          <span class="st-num">{{ mm }}</span>
          <span class="st-sep">:</span>
          <span class="st-num">{{ ss }}</span>
        </div>
      </div>
    </div>

    <!-- 秒杀商品列表 -->
    <div class="seckill-list" v-if="seckillList.length > 0">
      <div v-for="item in seckillList" :key="item.id"
        class="sk-item fade-in" @click="handleSeckill(item)">
        <div class="sk-img">
          <img :src="item.goodsImg" :alt="item.goodsName" @error="handleImageError" />
          <div class="sk-status" :class="statusClass(item.status)">
            {{ statusText(item.status) }}
          </div>
        </div>
        <div class="sk-info">
          <div class="sk-name">{{ item.goodsName }}</div>
          <div class="sk-title">{{ item.goodsTitle }}</div>
          <div class="sk-price-row">
            <div class="sk-price">
              <span class="sk-yen">¥</span>
              <span class="sk-now">{{ item.seckillPrice }}</span>
            </div>
            <span class="sk-old">¥{{ item.goodsPrice }}</span>
            <span class="sk-discount">{{ getDiscount(item) }}折</span>
          </div>
          <div class="sk-progress">
            <div class="sk-bar">
              <div class="sk-bar-fill" :style="{ width: getProgress(item) + '%' }"></div>
            </div>
            <span class="sk-pct">已抢{{ getProgress(item) }}%</span>
          </div>
          <button class="sk-btn" :class="{ active: item.status === 1 }" :disabled="item.status !== 1">
            {{ item.status === 0 ? '未开始' : item.status === 1 ? '立即抢购' : '已结束' }}
          </button>
          <div class="sk-countdown" v-if="item.remainSeconds > 0">
            {{ item.status === 0 ? '距开始' : '距结束' }}: {{ formatTime(item.remainSeconds) }}
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty">
      <p>暂无秒杀活动</p>
    </div>

    <!-- 验证码弹窗 -->
    <div class="modal-mask" v-if="captchaVisible" @click.self="captchaVisible = false">
      <div class="modal">
        <div class="modal-title">安全验证</div>
        <p class="modal-tip">请完成验证后继续</p>
        <div class="captcha-box">
          <img v-if="captchaImg" :src="captchaImg" />
          <div v-else class="captcha-loading">加载中...</div>
          <span class="captcha-refresh" @click.stop="refreshCaptcha">换一张</span>
        </div>
        <input v-model="captchaInput" placeholder="请输入验证码（不区分大小写）" class="modal-input"
          @keyup.enter="submitCaptcha" />
        <div class="modal-actions">
          <button class="btn-cancel" @click="captchaVisible = false">取消</button>
          <button class="btn-confirm" :disabled="seckillLoading" @click="submitCaptcha">
            {{ seckillLoading ? '提交中...' : '确认抢购' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 结果弹窗 -->
    <div class="modal-mask" v-if="resultVisible" @click.self="resultVisible = false">
      <div class="modal result-modal">
        <div v-if="result.status === 'success'" class="result-box success">
          <div class="result-icon">🎉</div>
          <h3>恭喜，抢购成功！</h3>
          <p>订单号: {{ result.orderNo }}</p>
          <button class="btn-confirm" @click="$router.push('/orders')">查看订单</button>
        </div>
        <div v-else-if="result.status === 'fail'" class="result-box fail">
          <div class="result-icon">😢</div>
          <h3>{{ result.msg || '很遗憾，已售罄' }}</h3>
          <button class="btn-cancel" @click="resultVisible = false">知道了</button>
        </div>
        <div v-else class="result-box queuing">
          <div class="result-icon spinning">⏳</div>
          <h3>排队中...</h3>
          <p>请耐心等待</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getSeckillGoodsList } from '../api/goods'
import { doSeckill, getSeckillResult } from '../api/seckill'
import { getCaptcha } from '../api/captcha'
import { useUserStore } from '../store/user'
import { handleImageError } from '../utils/image'

const router = useRouter()
const userStore = useUserStore()
const seckillList = ref([])
const captchaVisible = ref(false)
const captchaImg = ref('')
const captchaId = ref('')
const captchaInput = ref('')
const seckillLoading = ref(false)
const pendingItem = ref(null)
const resultVisible = ref(false)
const result = ref({ status: '', orderNo: '', msg: '' })
let pollTimer = null

const countdown = ref(0)
const hh = computed(() => String(Math.floor(countdown.value / 3600)).padStart(2, '0'))
const mm = computed(() => String(Math.floor((countdown.value % 3600) / 60)).padStart(2, '0'))
const ss = computed(() => String(countdown.value % 60).padStart(2, '0'))
let cdTimer = null

onMounted(async () => {
  try {
    const r = await getSeckillGoodsList()
    seckillList.value = r.data || []
    // 使用实际秒杀活动的最小剩余时间
    const activeSeckills = seckillList.value.filter(i => i.status === 1 && i.remainSeconds > 0)
    if (activeSeckills.length > 0) {
      countdown.value = Math.min(...activeSeckills.map(i => i.remainSeconds))
    }
  } catch (e) { ElMessage.error('加载秒杀列表失败') }
  cdTimer = setInterval(() => { if (countdown.value > 0) countdown.value-- }, 1000)
})
onUnmounted(() => { if (cdTimer) clearInterval(cdTimer); if (pollTimer) clearInterval(pollTimer) })

const statusText = (s) => ['未开始', '抢购中', '已结束'][s]
const statusClass = (s) => ['upcoming', 'active', 'ended'][s]
const formatTime = (sec) => {
  if (!sec || sec <= 0) return '00:00:00'
  const h = Math.floor(sec / 3600), m = Math.floor((sec % 3600) / 60), s = sec % 60
  return `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`
}
const getDiscount = (item) => {
  if (!item.goodsPrice || !item.seckillPrice) return '?'
  // 计算折扣（X折 = 秒杀价/原价 * 10）
  return (Number(item.seckillPrice) / Number(item.goodsPrice) * 10).toFixed(1)
}
const getProgress = (item) => {
  if (item.stockCount == null || !item.goodsStock) return 0
  const sold = item.goodsStock - item.stockCount
  return Math.min(99, Math.round((sold / item.goodsStock) * 100))
}

const handleSeckill = async (item) => {
  if (item.status !== 1) return
  if (!userStore.token) { router.push('/login'); return }
  pendingItem.value = item
  captchaInput.value = ''
  captchaVisible.value = true
  await refreshCaptcha()
}
const refreshCaptcha = async () => {
  try {
    const r = await getCaptcha()
    captchaImg.value = r.data.captchaImg
    captchaId.value = r.data.captchaId
    captchaInput.value = ''
  } catch (e) {}
}
const submitCaptcha = async () => {
  if (!captchaInput.value.trim()) return
  seckillLoading.value = true
  try {
    await doSeckill(pendingItem.value.id, captchaId.value, captchaInput.value.trim())
    captchaVisible.value = false
    result.value = { status: 'queuing' }; resultVisible.value = true
    startPolling(pendingItem.value.id)
  } catch (e) {} finally { seckillLoading.value = false }
}
const startPolling = (goodsId) => {
  if (pollTimer) clearInterval(pollTimer); let n = 0
  pollTimer = setInterval(async () => {
    n++; if (n > 30) { clearInterval(pollTimer); result.value = { status: 'fail', msg: '查询超时' }; return }
    try {
      const r = await getSeckillResult(goodsId); const d = r.data
      if (d.status === 'success') { clearInterval(pollTimer); result.value = { status: 'success', orderNo: d.orderNo } }
      else if (d.status === 'fail') { clearInterval(pollTimer); result.value = { status: 'fail', msg: d.msg || '已售罄' } }
    } catch (e) { clearInterval(pollTimer); result.value = { status: 'fail', msg: '查询失败' } }
  }, 1000)
}
</script>

<style scoped>
.seckill-page {}

/* ===== 头部 ===== */
.seckill-header {
  display: flex; justify-content: space-between; align-items: center;
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white; padding: 28px 32px;
  border-radius: var(--radius); margin-bottom: 16px;
}
.sh-left h1 { font-size: 28px; font-weight: 800; margin-bottom: 4px; }
.sh-left p { font-size: 14px; opacity: 0.9; }
.sh-right { text-align: center; }
.sh-label { font-size: 13px; opacity: 0.9; display: block; margin-bottom: 6px; }
.sh-timer { display: flex; gap: 4px; align-items: center; }
.st-num {
  background: rgba(0,0,0,0.25); padding: 6px 10px; border-radius: 6px;
  font-size: 24px; font-weight: 800; font-family: 'Courier New', monospace;
  min-width: 40px; text-align: center;
}
.st-sep { font-size: 22px; font-weight: 700; }

/* ===== 秒杀列表 ===== */
.seckill-list {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px;
}
.sk-item {
  display: flex; gap: 16px;
  background: var(--white); border-radius: var(--radius);
  padding: 16px; box-shadow: var(--shadow);
  cursor: pointer; transition: all 0.3s;
}
.sk-item:hover { box-shadow: var(--shadow-hover); transform: translateY(-2px); }
.sk-img {
  width: 180px; height: 180px; flex-shrink: 0;
  border-radius: 6px; overflow: hidden; position: relative;
}
.sk-img img { width: 100%; height: 100%; object-fit: cover; }
.sk-status {
  position: absolute; top: 8px; left: 0;
  padding: 3px 12px; font-size: 11px; font-weight: 700;
  border-radius: 0 12px 12px 0; color: white;
}
.sk-status.active { background: var(--red); }
.sk-status.upcoming { background: var(--orange); }
.sk-status.ended { background: #999; }

.sk-info { flex: 1; display: flex; flex-direction: column; }
.sk-name { font-size: 16px; font-weight: 600; margin-bottom: 4px; }
.sk-title { font-size: 12px; color: var(--text-light); margin-bottom: 12px; }
.sk-price-row { display: flex; align-items: baseline; gap: 10px; margin-bottom: 12px; }
.sk-price { color: var(--red); }
.sk-yen { font-size: 14px; font-weight: 600; }
.sk-now { font-size: 28px; font-weight: 800; }
.sk-old { font-size: 13px; color: var(--text-light); text-decoration: line-through; }
.sk-discount {
  background: var(--red-light); color: var(--red);
  padding: 2px 6px; border-radius: 4px; font-size: 11px; font-weight: 600;
}
.sk-progress { margin-bottom: 12px; }
.sk-bar { height: 10px; background: #f0f0f0; border-radius: 5px; overflow: hidden; margin-bottom: 4px; }
.sk-bar-fill { height: 100%; background: linear-gradient(90deg, var(--red), var(--orange)); border-radius: 5px; }
.sk-pct { font-size: 11px; color: var(--text-light); }

.sk-btn {
  width: 100%; padding: 10px; border: none; border-radius: 6px;
  font-size: 15px; font-weight: 600; cursor: pointer;
  background: #ddd; color: #999; transition: all 0.2s;
}
.sk-btn.active {
  background: var(--red); color: white;
}
.sk-btn.active:hover { background: var(--red-dark); }
.sk-countdown { text-align: center; font-size: 12px; color: var(--text-light); margin-top: 6px; }

/* ===== 弹窗 ===== */
.modal-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); display: flex;
  align-items: center; justify-content: center; z-index: 1000;
}
.modal {
  background: white; border-radius: 12px; padding: 28px;
  width: 380px; text-align: center;
}
.modal-title { font-size: 18px; font-weight: 700; margin-bottom: 6px; }
.modal-tip { font-size: 13px; color: var(--text-sub); margin-bottom: 16px; }
.captcha-box {
  position: relative; cursor: pointer; border-radius: 6px;
  overflow: hidden; margin-bottom: 12px; border: 1px solid var(--border);
}
.captcha-box img { width: 100%; height: 80px; object-fit: cover; }
.captcha-loading { height: 80px; display: flex; align-items: center; justify-content: center; }
.captcha-refresh {
  position: absolute; bottom: 4px; right: 8px;
  font-size: 11px; color: white; background: rgba(0,0,0,0.5);
  padding: 2px 8px; border-radius: 10px;
}
.modal-input {
  width: 100%; padding: 10px 12px; border: 1px solid var(--border);
  border-radius: 6px; font-size: 14px; outline: none; margin-bottom: 16px;
}
.modal-input:focus { border-color: var(--red); }
.modal-actions { display: flex; gap: 10px; }
.btn-cancel {
  flex: 1; padding: 10px; border: 1px solid var(--border);
  border-radius: 6px; background: white; cursor: pointer; font-size: 14px;
}
.btn-confirm {
  flex: 1; padding: 10px; border: none;
  border-radius: 6px; background: var(--red); color: white;
  cursor: pointer; font-size: 14px; font-weight: 600;
}
.btn-confirm:hover { background: var(--red-dark); }
.btn-confirm:disabled { opacity: 0.6; cursor: not-allowed; }

.result-modal { padding: 32px; }
.result-box { text-align: center; }
.result-icon { font-size: 56px; margin-bottom: 12px; }
.result-box h3 { font-size: 20px; margin-bottom: 8px; }
.result-box p { font-size: 13px; color: var(--text-sub); margin-bottom: 20px; }
.spinning { display: inline-block; animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0); } to { transform: rotate(360deg); } }

.empty { text-align: center; padding: 80px 0; color: var(--text-light); }
</style>
