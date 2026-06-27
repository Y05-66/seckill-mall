<template>
  <div class="admin-layout container">
    <div class="admin-sidebar">
      <div class="sidebar-title">管理后台</div>
      <div class="sidebar-menu">
        <div class="menu-item" :class="{ active: route.path === '/admin/dashboard' }" @click="$router.push('/admin/dashboard')">
          <span>📊</span><span>数据概览</span>
        </div>
        <div class="menu-item" :class="{ active: route.path === '/admin/goods' }" @click="$router.push('/admin/goods')">
          <span>📦</span><span>商品管理</span>
        </div>
        <div class="menu-item" :class="{ active: route.path === '/admin/seckill' }" @click="$router.push('/admin/seckill')">
          <span>🔥</span><span>秒杀管理</span>
        </div>
        <div class="menu-item" :class="{ active: route.path === '/admin/orders' }" @click="$router.push('/admin/orders')">
          <span>📋</span><span>订单管理</span>
        </div>
        <div class="menu-item" :class="{ active: route.path === '/admin/users' }" @click="$router.push('/admin/users')">
          <span>👥</span><span>用户管理</span>
        </div>
        <div class="menu-item" :class="{ active: route.path === '/admin/banner' }" @click="$router.push('/admin/banner')">
          <span>🖼️</span><span>轮播图管理</span>
        </div>
        <div class="menu-item" :class="{ active: route.path === '/admin/refund' }" @click="$router.push('/admin/refund')">
          <span>💰</span><span>退款管理</span>
        </div>
        <div class="menu-item" :class="{ active: route.path === '/admin/log' }" @click="$router.push('/admin/log')">
          <span>📝</span><span>操作日志</span>
        </div>
      </div>
    </div>
    <div class="admin-main">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>

<style scoped>
.admin-layout {
  display: flex; gap: 16px;
  margin-top: 0;
}
.admin-sidebar {
  width: 200px; flex-shrink: 0;
  background: var(--white); border-radius: var(--radius);
  box-shadow: var(--shadow); overflow: hidden;
  position: sticky; top: 120px; height: fit-content;
}
.sidebar-title {
  padding: 16px; font-size: 16px; font-weight: 700;
  border-bottom: 2px solid var(--red); color: var(--red);
}
.sidebar-menu { padding: 8px 0; }
.menu-item {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 16px; cursor: pointer; font-size: 14px;
  transition: all 0.2s;
}
.menu-item:hover { background: var(--red-light); color: var(--red); }
.menu-item.active {
  background: var(--red); color: white; font-weight: 600;
}
.admin-main { flex: 1; min-width: 0; }
</style>
