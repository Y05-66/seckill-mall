<template>
  <div class="home">
    <!-- Banner + 分类 -->
    <div class="container hero-section">
      <div class="category-panel">
        <div class="cat-item" v-for="cat in categories" :key="cat.name" @click="router.push('/goods')">
          <span class="cat-icon">{{ cat.icon }}</span>
          <span>{{ cat.name }}</span>
        </div>
      </div>
      <div class="banner-panel">
        <Transition name="banner-fade">
          <div class="banner-slide" :key="currentSlide" :style="{ background: banners[currentSlide].bg }">
            <div class="banner-content">
              <div class="banner-tag">{{ banners[currentSlide].tag }}</div>
              <h2>{{ banners[currentSlide].title }}</h2>
              <p>{{ banners[currentSlide].desc }}</p>
              <button class="banner-btn" @click="router.push(banners[currentSlide].link)">{{ banners[currentSlide].btn }}</button>
            </div>
            <div class="banner-deco">{{ banners[currentSlide].icon }}</div>
          </div>
        </Transition>
        <div class="banner-dots">
          <span v-for="(_, i) in banners" :key="i" class="dot" :class="{ active: i === currentSlide }" @click="currentSlide = i"></span>
        </div>
      </div>
    </div>

    <!-- 秒杀专区 -->
    <div class="section container">
      <div class="seckill-header">
        <div class="sh-left">
          <span class="sh-icon">🔥</span>
          <span class="sh-title">限时秒杀</span>
          <div class="sh-timer">
            <span class="cd-num">{{ hh }}</span><span class="cd-sep">:</span>
            <span class="cd-num">{{ mm }}</span><span class="cd-sep">:</span>
            <span class="cd-num">{{ ss }}</span>
          </div>
        </div>
        <span class="sh-more" @click="router.push('/seckill')">更多秒杀 →</span>
      </div>
      <div class="seckill-grid">
        <div v-for="item in seckillList.slice(0, 5)" :key="item.id" class="sk-card" @click="router.push('/seckill')">
          <div class="sk-img">
            <img :src="item.goodsImg" :alt="item.goodsName" loading="lazy" @error="handleImageError" />
            <div class="sk-discount">{{ getDiscount(item) }}折</div>
          </div>
          <div class="sk-info">
            <div class="sk-name">{{ item.goodsName }}</div>
            <div class="sk-price">
              <span class="sk-now">¥{{ item.seckillPrice }}</span>
              <span class="sk-old">¥{{ item.goodsPrice }}</span>
            </div>
            <div class="sk-bar"><div class="sk-fill" :style="{ width: getProgress(item) + '%' }"></div></div>
            <div class="sk-pct">已抢{{ getProgress(item) }}%</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 热门商品 -->
    <div class="section container">
      <div class="section-header">
        <span class="sh-title">热门商品</span>
        <span class="sh-more" @click="router.push('/goods')">查看全部 →</span>
      </div>
      <div class="goods-grid">
        <div v-for="item in goodsList" :key="item.id" class="goods-card" @click="router.push('/goods/' + item.id)">
          <div class="gc-img">
            <img :src="item.goodsImg" :alt="item.goodsName" loading="lazy" @error="handleImageError" />
            <div class="gc-tag" v-if="item.goodsStock < 10">库存紧张</div>
          </div>
          <div class="gc-body">
            <div class="gc-name">{{ item.goodsName }}</div>
            <div class="gc-tags"><span class="tag">自营</span><span class="tag" v-if="item.goodsPrice > 5000">高价</span></div>
            <div class="gc-bottom">
              <div class="gc-price"><span class="yen">¥</span><span class="num">{{ item.goodsPrice }}</span></div>
              <span class="gc-sales">已售{{ getSales(item) }}件</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 服务保障 -->
    <div class="container">
      <div class="services">
        <div class="svc"><span class="svc-icon">🔒</span><div><div class="svc-title">正品保障</div><div class="svc-desc">假一赔十</div></div></div>
        <div class="svc"><span class="svc-icon">🚚</span><div><div class="svc-title">极速发货</div><div class="svc-desc">24小时内发货</div></div></div>
        <div class="svc"><span class="svc-icon">🔄</span><div><div class="svc-title">七天退换</div><div class="svc-desc">无忧退货</div></div></div>
        <div class="svc"><span class="svc-icon">💬</span><div><div class="svc-title">在线客服</div><div class="svc-desc">7×24小时</div></div></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { getGoodsList, getSeckillGoodsList } from '../api/goods'
import { handleImageError } from '../utils/image'

const router = useRouter()
const userStore = useUserStore()
const goodsList = ref([])
const seckillList = ref([])

const categories = [
  { icon: '📱', name: '手机数码' }, { icon: '💻', name: '电脑办公' },
  { icon: '🎧', name: '耳机音响' }, { icon: '🎮', name: '游戏设备' },
  { icon: '📷', name: '摄影摄像' }, { icon: '🏠', name: '智能家电' },
  { icon: '⌚', name: '智能穿戴' }, { icon: '📖', name: '电子阅读' },
]

const banners = [
  { tag: '限时特惠', title: '秒杀专场 低至1折', desc: '每日精选爆款商品，限时限量抢购', btn: '立即抢购 →', icon: '⚡', bg: 'linear-gradient(135deg, #e4393c 0%, #ff6b81 100%)', link: '/seckill' },
  { tag: '新品首发', title: '科技好物 品质保障', desc: '最新数码产品，正品行货，全国联保', btn: '立即选购 →', icon: '🆕', bg: 'linear-gradient(135deg, #5f27cd 0%, #a55eea 100%)', link: '/goods' },
  { tag: '新人福利', title: '注册享专属优惠', desc: '新用户注册即享新人礼包', btn: '免费注册 →', icon: '🎁', bg: 'linear-gradient(135deg, #f0932b 0%, #f6e58d 100%)', link: '/register' },
]

const currentSlide = ref(0)
let slideTimer = null
const countdown = ref(0)
const hh = computed(() => String(Math.floor(countdown.value / 3600)).padStart(2, '0'))
const mm = computed(() => String(Math.floor((countdown.value % 3600) / 60)).padStart(2, '0'))
const ss = computed(() => String(countdown.value % 60).padStart(2, '0'))
let cdTimer = null

onMounted(async () => {
  try {
    const [g, s] = await Promise.all([getGoodsList(), getSeckillGoodsList()])
    goodsList.value = (g.data || []).slice(0, 12)
    seckillList.value = s.data || []
    // 使用实际秒杀活动的最小剩余时间
    const activeSeckills = seckillList.value.filter(i => i.status === 1 && i.remainSeconds > 0)
    if (activeSeckills.length > 0) {
      countdown.value = Math.min(...activeSeckills.map(i => i.remainSeconds))
    }
  } catch (e) {}
  cdTimer = setInterval(() => { if (countdown.value > 0) countdown.value-- }, 1000)
  slideTimer = setInterval(() => { currentSlide.value = (currentSlide.value + 1) % banners.length }, 4000)
})
onUnmounted(() => { if (cdTimer) clearInterval(cdTimer); if (slideTimer) clearInterval(slideTimer) })

const getDiscount = (i) => i.goodsPrice && i.seckillPrice ? (Number(i.seckillPrice) / Number(i.goodsPrice) * 10).toFixed(1) : '?'
const getProgress = (i) => {
  // 使用实际库存数据计算进度
  if (!i.stockCount || !i.goodsStock) return 0
  const sold = i.goodsStock - i.stockCount
  return Math.min(99, Math.round((sold / i.goodsStock) * 100))
}
const getSales = (i) => i.goodsStock ? (i.goodsStock - (i.stockCount || 0)) : 0
</script>

<style scoped>
.home {}

/* ===== Hero ===== */
.hero-section { display: flex; gap: 12px; margin-top: 8px; align-items: stretch; }
.category-panel { width: 200px; flex-shrink: 0; background: var(--white); border-radius: var(--radius); box-shadow: var(--shadow); padding: 2px 0; }
.cat-item { display: flex; align-items: center; gap: 10px; padding: 7px 16px; cursor: pointer; transition: all 0.2s; font-size: 13px; }
.cat-item:hover { background: var(--red-light); color: var(--red); }
.cat-icon { font-size: 18px; }

.banner-panel { flex: 1; position: relative; border-radius: var(--radius); overflow: hidden; display: flex; flex-direction: column; }

/* 轮播切换动画 - 交叉淡入淡出 */
.banner-slide {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
}
.banner-fade-enter-active { transition: opacity 0.5s ease; }
.banner-fade-leave-active { transition: opacity 0.5s ease; position: absolute; top: 0; left: 0; right: 0; bottom: 0; }
.banner-fade-enter-from { opacity: 0; }
.banner-fade-leave-to { opacity: 0; }

.banner-slide { padding: 24px 32px; color: white; display: flex; align-items: center; justify-content: space-between; flex: 1; }
.banner-content { position: relative; z-index: 1; }
.banner-tag { display: inline-block; background: rgba(255,255,255,0.25); padding: 4px 14px; border-radius: 14px; font-size: 13px; font-weight: 500; margin-bottom: 12px; letter-spacing: 0.5px; }
.banner-content h2 { font-size: 30px; font-weight: 800; margin-bottom: 8px; letter-spacing: 1px; text-shadow: 0 2px 8px rgba(0,0,0,0.1); }
.banner-content p { font-size: 15px; opacity: 0.9; margin-bottom: 16px; line-height: 1.5; }
.banner-btn { background: white; color: var(--red); border: none; padding: 10px 24px; border-radius: 20px; font-size: 14px; font-weight: 600; cursor: pointer; box-shadow: 0 2px 10px rgba(0,0,0,0.1); transition: transform 0.2s, box-shadow 0.2s; }
.banner-btn:hover { transform: translateY(-1px); box-shadow: 0 4px 14px rgba(0,0,0,0.15); }
.banner-deco { font-size: 80px; opacity: 0.2; transition: transform 0.4s; }
.banner-slide:hover .banner-deco { transform: scale(1.1) rotate(5deg); }
.banner-dots { position: absolute; bottom: 10px; left: 50%; transform: translateX(-50%); display: flex; gap: 8px; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: rgba(255,255,255,0.4); cursor: pointer; transition: all 0.3s; }
.dot:hover { background: rgba(255,255,255,0.7); }
.dot.active { background: white; width: 24px; border-radius: 4px; }

/* ===== Section ===== */
.section { margin-top: 16px; }
.section-header { display: flex; justify-content: space-between; align-items: center; padding: 14px 0; border-bottom: 2px solid var(--red); margin-bottom: 14px; }

/* ===== 秒杀 ===== */
.seckill-header { display: flex; justify-content: space-between; align-items: center; background: var(--red); color: white; padding: 12px 16px; border-radius: var(--radius) var(--radius) 0 0; }
.sh-left { display: flex; align-items: center; gap: 8px; }
.sh-icon { font-size: 20px; }
.sh-title { font-size: 18px; font-weight: 700; }
.sh-timer { display: flex; align-items: center; gap: 3px; margin-left: 10px; }
.cd-num { background: rgba(0,0,0,0.25); padding: 2px 6px; border-radius: 3px; font-size: 15px; font-weight: 700; font-family: 'Courier New', monospace; min-width: 22px; text-align: center; }
.cd-sep { font-weight: 700; font-size: 14px; }
.sh-more { font-size: 12px; color: rgba(255,255,255,0.8); cursor: pointer; }

.seckill-grid { display: flex; background: #fff; border-radius: 0 0 var(--radius) var(--radius); box-shadow: var(--shadow); overflow: hidden; }
.sk-card { flex: 1; padding: 14px; cursor: pointer; border-right: 1px solid #f5f5f5; transition: background 0.2s; }
.sk-card:last-child { border-right: none; }
.sk-card:hover { background: var(--red-light); }
.sk-img { position: relative; width: 100%; aspect-ratio: 1; border-radius: 6px; overflow: hidden; margin-bottom: 8px; }
.sk-img img { width: 100%; height: 100%; object-fit: cover; }
.sk-discount { position: absolute; top: 6px; left: 0; background: var(--red); color: white; padding: 1px 8px; border-radius: 0 8px 8px 0; font-size: 11px; font-weight: 700; }
.sk-name { font-size: 12px; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.sk-price { display: flex; align-items: baseline; gap: 6px; margin-bottom: 6px; }
.sk-now { font-size: 18px; font-weight: 800; color: var(--red); }
.sk-old { font-size: 11px; color: var(--text-999); text-decoration: line-through; }
.sk-bar { height: 8px; background: #ffe0e0; border-radius: 4px; overflow: hidden; margin-bottom: 2px; }
.sk-fill { height: 100%; background: var(--red); border-radius: 4px; }
.sk-pct { font-size: 10px; color: var(--text-999); }

/* ===== 商品网格 ===== */
.goods-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 10px; }
.goods-card { background: #fff; border-radius: var(--radius); overflow: hidden; cursor: pointer; box-shadow: var(--shadow); transition: all 0.3s; }
.goods-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-md); }
.gc-img { position: relative; width: 100%; aspect-ratio: 1; overflow: hidden; }
.gc-img img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s; }
.goods-card:hover .gc-img img { transform: scale(1.06); }
.gc-tag { position: absolute; top: 6px; left: 0; background: var(--orange); color: white; padding: 1px 8px; border-radius: 0 8px 8px 0; font-size: 10px; font-weight: 600; }
.gc-body { padding: 10px; }
.gc-name { font-size: 13px; font-weight: 500; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.gc-tags { display: flex; gap: 4px; margin-bottom: 6px; }
.tag { font-size: 10px; padding: 1px 4px; border: 1px solid var(--red); color: var(--red); border-radius: 2px; }
.gc-bottom { display: flex; justify-content: space-between; align-items: flex-end; }
.gc-price { color: var(--red); }
.yen { font-size: 11px; font-weight: 600; }
.num { font-size: 17px; font-weight: 800; }
.gc-sales { font-size: 10px; color: var(--text-999); }

/* ===== 服务 ===== */
.services { display: flex; justify-content: space-around; background: #fff; border-radius: var(--radius); padding: 20px; margin-top: 20px; box-shadow: var(--shadow); }
.svc { display: flex; align-items: center; gap: 10px; }
.svc-icon { font-size: 28px; }
.svc-title { font-size: 14px; font-weight: 600; }
.svc-desc { font-size: 11px; color: var(--text-999); }

/* ===== 响应式布局 ===== */
@media (max-width: 1024px) {
  .hero-section { flex-direction: column; }
  .category-panel { width: 100%; display: flex; flex-wrap: wrap; }
  .goods-grid { grid-template-columns: repeat(4, 1fr); }
  .seckill-grid { flex-wrap: wrap; }
  .sk-card { flex: 0 0 calc(33.33% - 1px); }
}

@media (max-width: 768px) {
  .goods-grid { grid-template-columns: repeat(3, 1fr); gap: 8px; }
  .sk-card { flex: 0 0 calc(50% - 1px); }
  .services { flex-wrap: wrap; gap: 12px; }
  .svc { width: calc(50% - 6px); }
}

@media (max-width: 576px) {
  .goods-grid { grid-template-columns: repeat(2, 1fr); gap: 8px; }
  .sk-card { flex: 0 0 100%; }
  .banner-content h2 { font-size: 22px; }
  .banner-content p { font-size: 13px; }
  .banner-deco { font-size: 50px; }
}
</style>
