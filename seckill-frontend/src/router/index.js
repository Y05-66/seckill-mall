import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  { path: '/', component: () => import('../views/Home.vue') },
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/register', component: () => import('../views/Register.vue') },
  { path: '/reset-password', component: () => import('../views/ResetPassword.vue') },
  { path: '/goods', component: () => import('../views/GoodsList.vue') },
  { path: '/goods/:id', component: () => import('../views/GoodsDetail.vue') },
  { path: '/seckill', component: () => import('../views/SeckillList.vue') },
  { path: '/cart', component: () => import('../views/Cart.vue'), meta: { requiresAuth: true } },
  { path: '/address', component: () => import('../views/Address.vue'), meta: { requiresAuth: true } },
  { path: '/favorites', component: () => import('../views/Favorites.vue'), meta: { requiresAuth: true } },
  { path: '/notifications', component: () => import('../views/Notifications.vue'), meta: { requiresAuth: true } },
  { path: '/coupons', component: () => import('../views/Coupons.vue'), meta: { requiresAuth: true } },
  { path: '/points', component: () => import('../views/Points.vue'), meta: { requiresAuth: true } },
  { path: '/compare', component: () => import('../views/Compare.vue') },
  { path: '/orders', component: () => import('../views/OrderList.vue'), meta: { requiresAuth: true } },
  { path: '/profile', component: () => import('../views/Profile.vue'), meta: { requiresAuth: true } },
  { path: '/settings', component: () => import('../views/Settings.vue'), meta: { requiresAuth: true } },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', component: () => import('../views/admin/AdminDashboard.vue') },
      { path: 'goods', component: () => import('../views/admin/AdminGoods.vue') },
      { path: 'seckill', component: () => import('../views/admin/AdminSeckill.vue') },
      { path: 'orders', component: () => import('../views/admin/AdminOrder.vue') },
      { path: 'users', component: () => import('../views/admin/AdminUser.vue') },
      { path: 'banner', component: () => import('../views/admin/AdminBanner.vue') },
      { path: 'refund', component: () => import('../views/admin/AdminRefund.vue') },
      { path: 'log', component: () => import('../views/admin/AdminLog.vue') }
    ]
  },
  // 404 catch-all 路由（必须放在最后）
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('../views/NotFound.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() { return { top: 0 } }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const publicPages = ['/login', '/register', '/goods', '/seckill', '/compare', '/']
  const userStore = useUserStore()

  const isPublic = publicPages.some(p => to.path === p || to.path.startsWith(p + '/'))

  // 已登录用户访问登录/注册页，重定向到首页
  if (userStore.token && (to.path === '/login' || to.path === '/register')) {
    next('/')
  } else if (!isPublic && !userStore.token) {
    // 保存用户目标页面，登录后跳转
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.matched.some(record => record.meta.requiresAdmin) && userStore.role !== 1) {
    next('/goods')
  } else {
    next()
  }
})

export default router
