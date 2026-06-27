<template>
  <div class="goods-page">
    <!-- 搜索栏 -->
    <div class="search-header">
      <div class="search-bar">
        <div class="search-input-wrap">
          <span class="search-icon">🔍</span>
          <input
            v-model="keyword"
            placeholder="搜索商品名称、分类..."
            class="search-input"
            @keyup.enter="handleSearch"
            @focus="showSearchPanel = true"
          />
          <span v-if="keyword" class="clear-btn" @click="clearSearch">×</span>
        </div>
        <button class="search-btn" @click="handleSearch">搜索</button>
      </div>

      <!-- 搜索面板（历史 + 热搜） -->
      <div v-if="showSearchPanel && !keyword" class="search-panel">
        <!-- 搜索历史 -->
        <div v-if="searchHistory.length > 0" class="panel-section">
          <div class="section-header">
            <span class="section-title">搜索历史</span>
            <span class="clear-history" @click="clearHistory">🗑️ 清空</span>
          </div>
          <div class="tag-list">
            <span
              v-for="(item, index) in searchHistory" :key="index"
              class="history-tag" @click="selectKeyword(item)"
            >{{ item }}</span>
          </div>
        </div>

        <!-- 热门搜索 -->
        <div class="panel-section">
          <div class="section-header">
            <span class="section-title">🔥 热门搜索</span>
          </div>
          <div class="tag-list">
            <span
              v-for="(item, index) in hotKeywords" :key="index"
              class="hot-tag" :class="{ 'hot-top': index < 3 }" @click="selectKeyword(item)"
            >{{ item }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选排序栏 -->
    <div class="filter-bar" v-if="!showSearchPanel || keyword">
      <div class="filter-left">
        <span class="filter-count">共 {{ filteredList.length }} 件商品</span>
      </div>
      <div class="filter-right">
        <span
          class="sort-item" :class="{ active: sortType === 'default' }"
          @click="sortType = 'default'"
        >综合</span>
        <span
          class="sort-item" :class="{ active: sortType === 'sales' }"
          @click="sortType = 'sales'"
        >销量</span>
        <span
          class="sort-item" :class="{ active: sortType === 'price' }"
          @click="togglePriceSort"
        >
          价格
          <span class="sort-arrow">
            <span :class="{ active: sortType === 'price' && sortOrder === 'asc' }">↑</span>
            <span :class="{ active: sortType === 'price' && sortOrder === 'desc' }">↓</span>
          </span>
        </span>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 商品列表 -->
    <div class="goods-container" v-else-if="filteredList.length > 0">
      <div class="goods-grid">
        <div v-for="item in filteredList" :key="item.id" class="goods-card" @click="router.push('/goods/' + item.id)">
          <div class="gc-img">
            <img :src="item.goodsImg" :alt="item.goodsName" loading="lazy" @error="handleImageError" />
            <div class="gc-tag" v-if="item.goodsStock < 10">库存紧张</div>
          </div>
          <div class="gc-body">
            <div class="gc-name">{{ item.goodsName }}</div>
            <div class="gc-tags"><span class="tag">自营</span></div>
            <div class="gc-bottom">
              <div class="gc-price">
                <span class="yen">¥</span>
                <span class="num">{{ item.goodsPrice }}</span>
              </div>
              <span class="gc-sales">已售{{ getSales(item) }}件</span>
            </div>
            <button class="gc-cart-btn" @click.stop="handleAddToCart(item)">
              <span class="cart-icon">🛒</span> 加入购物车
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">🔍</div>
      <p v-if="keyword" class="empty-text">未找到"{{ keyword }}"相关商品</p>
      <p v-else class="empty-text">暂无商品</p>
      <div class="empty-actions">
        <button v-if="keyword" class="empty-btn" @click="clearSearch">清除搜索</button>
        <button class="empty-btn outline" @click="router.push('/')">返回首页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getGoodsList } from '../api/goods'
import { addToCart } from '../api/cart'
import { useUserStore } from '../store/user'
import { handleImageError } from '../utils/image'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const goodsList = ref([])
const loading = ref(true)
const keyword = ref(route.query.keyword || '')
const showSearchPanel = ref(false)
const sortType = ref('default')
const sortOrder = ref('asc')

// 搜索历史（从localStorage读取）
const searchHistory = ref(JSON.parse(localStorage.getItem('searchHistory') || '[]'))

// 热门搜索关键词
const hotKeywords = ref(['iPhone', 'MacBook', '耳机', 'iPad', '小米', '华为', '键盘', '鼠标'])

// 加载商品列表
onMounted(async () => {
  try {
    const r = await getGoodsList()
    goodsList.value = r.data || []
  } catch (e) {
    ElMessage.error('加载商品列表失败')
  } finally {
    loading.value = false
  }
})

// 监听路由query变化
watch(() => route.query.keyword, (newVal) => {
  if (newVal !== undefined) {
    keyword.value = newVal
    showSearchPanel.value = false
  }
})

// 过滤和排序后的列表
const filteredList = computed(() => {
  let list = goodsList.value

  // 关键词过滤
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    list = list.filter(g =>
      (g.goodsName && g.goodsName.toLowerCase().includes(kw)) ||
      (g.goodsTitle && g.goodsTitle.toLowerCase().includes(kw))
    )
  }

  // 排序
  if (sortType.value === 'sales') {
    list = [...list].sort((a, b) => getSales(b) - getSales(a))
  } else if (sortType.value === 'price') {
    list = [...list].sort((a, b) => {
      return sortOrder.value === 'asc'
        ? a.goodsPrice - b.goodsPrice
        : b.goodsPrice - a.goodsPrice
    })
  }

  return list
})

// 搜索
const handleSearch = () => {
  if (!keyword.value.trim()) return
  showSearchPanel.value = false
  // 保存搜索历史
  saveSearchHistory(keyword.value.trim())
}

// 选择关键词
const selectKeyword = (kw) => {
  keyword.value = kw
  showSearchPanel.value = false
  saveSearchHistory(kw)
}

// 清除搜索
const clearSearch = () => {
  keyword.value = ''
  showSearchPanel.value = true
}

// 价格排序切换
const togglePriceSort = () => {
  if (sortType.value === 'price') {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortType.value = 'price'
    sortOrder.value = 'asc'
  }
}

// 保存搜索历史
const saveSearchHistory = (kw) => {
  let history = JSON.parse(localStorage.getItem('searchHistory') || '[]')
  // 去重并放到最前面
  history = history.filter(item => item !== kw)
  history.unshift(kw)
  // 最多保存10条
  history = history.slice(0, 10)
  localStorage.setItem('searchHistory', JSON.stringify(history))
  searchHistory.value = history
}

// 清空搜索历史
const clearHistory = () => {
  localStorage.removeItem('searchHistory')
  searchHistory.value = []
}

// 获取销量（基于库存差值）
const getSales = (i) => {
  if (!i.goodsStock) return 0
  return i.stockCount !== undefined ? Math.max(0, i.goodsStock - i.stockCount) : 0
}

// 加入购物车
const handleAddToCart = async (item) => {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  try {
    await addToCart({ goodsId: item.id, quantity: 1 })
    ElMessage.success('已加入购物车')
  } catch (e) {}
}
</script>

<style scoped>
.goods-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

/* ===== 搜索栏 ===== */
.search-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--white);
  padding: 16px 0;
  margin-bottom: 0;
}

.search-bar {
  display: flex;
  gap: 12px;
  max-width: 800px;
  margin: 0 auto;
}

.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 24px;
  padding: 0 16px;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.search-input-wrap:focus-within {
  border-color: var(--red);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(228, 57, 60, 0.1);
}

.search-icon {
  font-size: 16px;
  margin-right: 8px;
  opacity: 0.6;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 12px 0;
  font-size: 14px;
  outline: none;
}

.search-input::placeholder {
  color: #999;
}

.clear-btn {
  font-size: 18px;
  color: #999;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 50%;
  transition: all 0.2s;
}

.clear-btn:hover {
  background: #e0e0e0;
  color: #666;
}

.search-btn {
  padding: 12px 24px;
  background: var(--red);
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.search-btn:hover {
  background: var(--red-dark);
  transform: translateY(-1px);
}

/* ===== 搜索面板 ===== */
.search-panel {
  max-width: 800px;
  margin: 12px auto 0;
  background: var(--white);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 20px;
  animation: slideDown 0.2s ease;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.panel-section {
  margin-bottom: 20px;
}

.panel-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.clear-history {
  font-size: 12px;
  color: #999;
  cursor: pointer;
  transition: color 0.2s;
}

.clear-history:hover {
  color: var(--red);
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-tag, .hot-tag {
  padding: 6px 14px;
  background: #f5f5f5;
  border-radius: 16px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.history-tag:hover, .hot-tag:hover {
  background: #e8e8e8;
  color: #333;
}

.hot-tag.hot-top {
  background: #fff5f5;
  color: var(--red);
  font-weight: 500;
}

.hot-tag.hot-top:hover {
  background: #ffe0e0;
}

/* ===== 筛选排序栏 ===== */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--white);
  padding: 12px 20px;
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  margin-bottom: 16px;
}

.filter-count {
  font-size: 13px;
  color: var(--text-999);
}

.filter-right {
  display: flex;
  gap: 20px;
}

.sort-item {
  font-size: 13px;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
}

.sort-item:hover, .sort-item.active {
  color: var(--red);
  font-weight: 500;
}

.sort-arrow {
  display: flex;
  flex-direction: column;
  font-size: 10px;
  line-height: 1;
}

.sort-arrow span {
  opacity: 0.3;
}

.sort-arrow span.active {
  opacity: 1;
  color: var(--red);
}

/* ===== 商品容器 ===== */
.goods-container {
  padding: 0;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

.goods-card {
  background: #fff;
  border-radius: var(--radius);
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--shadow);
  transition: all 0.3s;
}

.goods-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.gc-img {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
}

.gc-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.goods-card:hover .gc-img img {
  transform: scale(1.06);
}

.gc-tag {
  position: absolute;
  top: 8px;
  left: 0;
  background: var(--orange);
  color: white;
  padding: 2px 10px;
  border-radius: 0 10px 10px 0;
  font-size: 11px;
  font-weight: 600;
}

.gc-body {
  padding: 12px;
}

.gc-name {
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #333;
}

.gc-tags {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
}

.tag {
  font-size: 10px;
  padding: 2px 6px;
  border: 1px solid var(--red);
  color: var(--red);
  border-radius: 3px;
}

.gc-bottom {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 10px;
}

.gc-price {
  color: var(--red);
}

.yen {
  font-size: 12px;
  font-weight: 600;
}

.num {
  font-size: 18px;
  font-weight: 800;
}

.gc-sales {
  font-size: 11px;
  color: var(--text-999);
}

.gc-cart-btn {
  width: 100%;
  padding: 8px 0;
  background: var(--red);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.gc-cart-btn:hover {
  background: var(--red-dark);
}

.cart-icon {
  font-size: 14px;
}

/* ===== 加载状态 ===== */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: var(--text-999);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid var(--red);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* ===== 空状态 ===== */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: var(--white);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.6;
}

.empty-text {
  font-size: 16px;
  color: #666;
  margin-bottom: 24px;
}

.empty-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.empty-btn {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  background: var(--red);
  color: white;
  border: none;
}

.empty-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.empty-btn.outline {
  background: white;
  color: #666;
  border: 1px solid #ddd;
}

.empty-btn.outline:hover {
  border-color: var(--red);
  color: var(--red);
}

/* ===== 响应式 ===== */
@media (max-width: 992px) {
  .goods-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .goods-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }
}

@media (max-width: 576px) {
  .goods-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
}
</style>
