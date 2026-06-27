/**
 * uni-app 应用入口文件
 *
 * 负责创建 Vue 3 SSR 应用实例并注册全局插件：
 * - Pinia：状态管理（用户登录态、Token 等）
 * - 全局路由守卫：拦截需要登录的页面
 *
 * 注意：uni-app 框架会自动注入 router，无需手动创建。
 * Element Plus 不在此处注册（小程序端使用原生组件）。
 */
import { createSSRApp, createApp as createVueApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import AiEntry from './components/AiEntry.vue'

// 需要登录才能访问的页面
const AUTH_PAGES = [
  'pages/cart/index',
  'pages/favorites/index',
  'pages/coupons/index',
  'pages/notifications/index',
  'pages/order/list',
  'pages/order/detail',
  'pages/my/index',
  'pages/refund/index',
  'pages/address/index'
]

// 需要管理员权限的页面
const ADMIN_PAGES = [
  'pages-admin/dashboard/index',
  'pages-admin/goods/list',
  'pages-admin/seckill/list',
  'pages-admin/order/list',
  'pages-admin/user/list'
]

/**
 * 创建应用实例（uni-app 要求导出此函数）
 * @returns {{ app: import('vue').App }} Vue 应用实例
 */
export function createApp() {
  const app = createSSRApp(App)
  // 注册 Pinia 状态管理
  app.use(createPinia())

  // 添加全局路由拦截器
  uni.addInterceptor('navigateTo', {
    invoke(args) {
      return checkAuth(args.url)
    }
  })
  uni.addInterceptor('redirectTo', {
    invoke(args) {
      return checkAuth(args.url)
    }
  })
  uni.addInterceptor('switchTab', {
    invoke(args) {
      return checkAuth(args.url)
    }
  })

  // H5 端额外挂载一份到 body（防止页面切换时组件销毁重建）
  // 小程序端通过每个页面本地 import + <AiEntry /> 渲染
  // #ifdef H5
  setTimeout(() => {
    if (!document.getElementById('ai-assistant-mount')) {
      const el = document.createElement('div')
      el.id = 'ai-assistant-mount'
      document.body.appendChild(el)
      const aiApp = createVueApp(AiEntry)
      aiApp.mount(el)
    }
  }, 100)
  // #endif

  return { app }
}

/**
 * 检查页面访问权限
 * @param {string} url - 目标页面路径
 * @returns {boolean} 是否允许导航
 */
function checkAuth(url) {
  // 提取路径（去掉查询参数）
  const path = url.split('?')[0].replace(/^\//, '')

  let token = ''
  let role = 0
  try {
    token = uni.getStorageSync('token') || ''
    // role 存储在 userInfo 对象中，需要从 userInfo 中获取
    const userInfo = uni.getStorageSync('userInfo') || {}
    role = Number(userInfo.role || 0)
  } catch (e) {}

  // 检查是否需要登录
  const needAuth = AUTH_PAGES.some(p => path.startsWith(p))
  if (needAuth && !token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => {
      uni.navigateTo({ url: '/pages/login/login' })
    }, 1500)
    return false
  }

  // 检查是否需要管理员权限
  const needAdmin = ADMIN_PAGES.some(p => path.startsWith(p))
  if (needAdmin && role !== 1) {
    uni.showToast({ title: '无权限访问', icon: 'none' })
    return false
  }

  return true
}
