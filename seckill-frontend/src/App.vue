<template>
  <div class="app">
    <!-- 顶部通知栏 -->
    <div class="topbar">
      <div class="container topbar-inner">
        <span>欢迎来到秒杀商城！每日精选爆款限时抢购</span>
        <div class="topbar-right">
          <template v-if="userStore.token">
            <span @click="router.push('/profile')">{{ userStore.nickname || userStore.username }}</span>
            <span class="sep">|</span>
            <span @click="router.push('/orders')">我的订单</span>
            <span class="sep">|</span>
            <span v-if="userStore.role === 1" @click="router.push('/admin')">管理后台</span>
            <span v-if="userStore.role === 1" class="sep">|</span>
            <span @click="handleLogout">退出</span>
          </template>
          <template v-else>
            <span @click="router.push('/login')">请登录</span>
            <span class="sep">|</span>
            <span @click="router.push('/register')">免费注册</span>
          </template>
        </div>
      </div>
    </div>

    <!-- 头部 -->
    <header class="header">
      <div class="container header-inner">
        <div class="logo" @click="router.push('/')">
          <span class="logo-icon">⚡</span>
          <span class="logo-text">秒杀商城</span>
        </div>
        <div class="search-wrap">
          <div class="search-box">
            <span class="search-icon">🔍</span>
            <input
              v-model="searchText"
              placeholder="搜索商品名称、分类..."
              class="search-input"
              @keyup.enter="handleSearch"
              @focus="showSearchSuggest = true"
              @blur="hideSearchSuggest"
            />
            <button class="search-btn" @click="handleSearch">搜索</button>
          </div>
          <!-- 搜索建议下拉 -->
          <div v-if="showSearchSuggest && searchText" class="search-suggest">
            <div
              v-for="item in searchSuggestions" :key="item"
              class="suggest-item" @mousedown="selectSuggestion(item)"
            >
              <span class="suggest-icon">🔍</span>
              <span>{{ item }}</span>
            </div>
          </div>
        </div>
        <div class="header-actions">
          <div class="action" @click="router.push(userStore.token ? '/cart' : '/login')">
            <span class="action-icon">🛒</span>
            <span>购物车</span>
          </div>
          <div class="action" @click="router.push(userStore.token ? '/orders' : '/login')">
            <span class="action-icon">📋</span>
            <span>订单</span>
          </div>
          <div class="action" @click="router.push(userStore.token ? '/profile' : '/login')">
            <span class="action-icon">👤</span>
            <span>我的</span>
          </div>
        </div>
      </div>
    </header>

    <!-- 导航 -->
    <nav class="nav">
      <div class="container nav-inner">
        <div class="nav-item" :class="{ active: route.path === '/' }" @click="router.push('/')">首页</div>
        <div class="nav-item" :class="{ active: route.path === '/goods' }" @click="router.push('/goods')">全部商品</div>
        <div class="nav-item hot" :class="{ active: route.path === '/seckill' }" @click="router.push('/seckill')">
          <span class="hot-tag">HOT</span>限时秒杀
        </div>
        <div class="nav-item" :class="{ active: route.path === '/compare' }" @click="router.push('/compare')">商品对比</div>
      </div>
    </nav>

    <!-- 内容 -->
    <main class="main"><router-view /></main>

    <!-- 底部 -->
    <footer class="footer">
      <div class="container footer-inner">
        <div class="footer-grid">
          <div class="footer-col">
            <h4>关于我们</h4>
            <p>秒杀商城 — 高并发限时抢购平台</p>
            <p>Spring Boot + Vue 3 + Redis + RabbitMQ</p>
          </div>
          <div class="footer-col">
            <h4>购物指南</h4>
            <p @click="router.push('/')">首页</p>
            <p @click="router.push('/goods')">全部商品</p>
            <p @click="router.push('/seckill')">限时秒杀</p>
          </div>
          <div class="footer-col">
            <h4>技术支持</h4>
            <p>Redis 高速缓存</p>
            <p>RabbitMQ 消息队列</p>
            <p>Redisson 分布式锁</p>
          </div>
          <div class="footer-col">
            <h4>联系方式</h4>
            <p>📧 support@seckill.com</p>
            <p>📞 400-888-8888</p>
          </div>
        </div>
        <div class="footer-bottom">
          <p>© 2026 秒杀商城 Seckill Mall. All rights reserved.</p>
        </div>
      </div>
    </footer>

    <!-- AI 购物助手 -->
    <AiAssistant />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from './store/user'
import AiAssistant from './components/AiAssistant.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const searchText = ref('')
const showSearchSuggest = ref(false)

// 热门搜索词（可用于建议）
const allKeywords = ['iPhone', 'MacBook', 'AirPods', 'iPad', '小米', '华为', '键盘', '鼠标', '耳机', '显示器', '笔记本', '手机']

// 搜索建议（根据输入过滤）
const searchSuggestions = computed(() => {
  if (!searchText.value.trim()) return []
  const kw = searchText.value.trim().toLowerCase()
  return allKeywords.filter(item => item.toLowerCase().includes(kw)).slice(0, 5)
})

// 隐藏搜索建议（延迟执行，允许点击建议项）
const hideSearchSuggest = () => {
  setTimeout(() => { showSearchSuggest.value = false }, 200)
}

// 选择建议项
const selectSuggestion = (item) => {
  searchText.value = item
  showSearchSuggest.value = false
  handleSearch()
}

const handleSearch = () => {
  if (searchText.value.trim()) {
    router.push({ path: '/goods', query: { keyword: searchText.value.trim() } })
  }
}
const handleLogout = () => { userStore.logout(); router.push('/login') }
</script>

<style>
:root {
  --red: #e4393c;
  --red-dark: #c1272d;
  --red-light: #fff0f0;
  --orange: #ff6b00;
  --gold: #f5a623;
  --green: #52c41a;
  --bg: #f5f5f5;
  --white: #ffffff;
  --text: #222222;
  --text-main: #333333;
  --text-sub: #666666;
  --text-666: #666666;
  --text-light: #999999;
  --text-999: #999999;
  --text-ccc: #cccccc;
  --text-hint: #bbbbbb;
  --border: #e8e8e8;
  --shadow: 0 1px 4px rgba(0,0,0,0.08);
  --shadow-md: 0 4px 12px rgba(0,0,0,0.1);
  --shadow-lg: 0 8px 24px rgba(0,0,0,0.12);
  --shadow-hover: 0 4px 16px rgba(0,0,0,0.12);
  --shadow-card: 0 2px 8px rgba(0,0,0,0.06);
  --radius: 8px;
  --radius-sm: 4px;
  --radius-md: 12px;
  --radius-lg: 16px;
}

* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  background: var(--bg);
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  color: var(--text);
  font-size: 14px;
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
}
img { display: block; max-width: 100%; }
a { text-decoration: none; color: inherit; }

.container { max-width: 1200px; margin: 0 auto; padding: 0 16px; }

/* ===== 顶部栏 ===== */
.topbar { background: #f2f2f2; border-bottom: 1px solid var(--border); font-size: 12px; color: var(--text-666); }
.topbar-inner { display: flex; justify-content: space-between; align-items: center; height: 32px; }
.topbar-right { display: flex; gap: 6px; align-items: center; }
.topbar-right span { cursor: pointer; }
.topbar-right span:hover { color: var(--red); }
.topbar-right .sep { color: #ccc; cursor: default; }

/* ===== 头部 ===== */
.header { background: var(--white); padding: 12px 0; border-bottom: 2px solid var(--red); }
.header-inner { display: flex; align-items: center; gap: 24px; }
.logo { display: flex; align-items: center; gap: 8px; cursor: pointer; flex-shrink: 0; }
.logo-icon { font-size: 32px; }
.logo-text { font-size: 24px; font-weight: 800; color: var(--red); }

/* ===== 搜索框 ===== */
.search-wrap {
  flex: 1;
  max-width: 800px;
  position: relative;
}

.search-box {
  display: flex;
  align-items: center;
  border: 2px solid var(--red);
  border-radius: 24px;
  overflow: hidden;
  background: #fff;
  transition: all 0.3s;
}

.search-box:focus-within {
  box-shadow: 0 0 0 3px rgba(228, 57, 60, 0.15);
}

.search-icon {
  font-size: 16px;
  margin-left: 16px;
  opacity: 0.6;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 10px 12px;
  font-size: 14px;
  background: transparent;
}

.search-input::placeholder {
  color: #999;
}

.search-btn {
  background: var(--red);
  color: white;
  border: none;
  padding: 10px 36px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}

.search-btn:hover {
  background: var(--red-dark);
}

/* 搜索建议下拉 */
.search-suggest {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  overflow: hidden;
  animation: fadeIn 0.2s ease;
}

.suggest-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 14px;
  color: #333;
}

.suggest-item:hover {
  background: #f5f5f5;
}

.suggest-icon {
  font-size: 14px;
  opacity: 0.5;
}

.header-actions { display: flex; gap: 20px; flex-shrink: 0; margin-left: auto; }
.action { display: flex; flex-direction: column; align-items: center; cursor: pointer; font-size: 12px; color: var(--text-666); transition: color 0.2s; }
.action:hover { color: var(--red); }
.action-icon { font-size: 22px; margin-bottom: 2px; }

/* ===== 导航 ===== */
.nav { background: var(--white); border-bottom: 1px solid var(--border); }
.nav-inner { display: flex; }
.nav-item { padding: 12px 28px; font-size: 15px; font-weight: 500; cursor: pointer; position: relative; transition: color 0.2s; }
.nav-item:hover { color: var(--red); }
.nav-item.active { color: var(--red); font-weight: 600; }
.nav-item.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 60%; height: 2px; background: var(--red); }
.nav-item.hot { color: var(--red); }
.hot-tag { background: var(--red); color: white; font-size: 10px; font-weight: 700; padding: 1px 4px; border-radius: 2px; margin-right: 4px; }

/* ===== 主内容 ===== */
.main { min-height: calc(100vh - 350px); padding: 12px 0; }

/* ===== 底部 ===== */
.footer { background: #2c2c2c; color: #aaa; padding: 40px 0 0; margin-top: 40px; }
.footer-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 40px; padding-bottom: 30px; }
.footer-col h4 { color: white; font-size: 15px; margin-bottom: 12px; }
.footer-col p { font-size: 13px; line-height: 2.2; cursor: pointer; }
.footer-col p:hover { color: white; }
.footer-bottom { text-align: center; padding: 16px 0; border-top: 1px solid #3c3c3c; font-size: 12px; }

/* ===== 动画 ===== */
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
.fade-in { animation: fadeIn 0.3s ease-out; }
</style>
