<!--
  App.vue - 应用根组件

  职责：
  1. 定义全局 CSS 变量和通用样式类
  2. 监听应用生命周期（onLaunch、onShow）
  3. 提供全局样式：按钮、卡片、标签、输入框、空状态等

  注意：此组件不包含页面内容，页面内容由 pages.json 中的路由决定。
-->
<script setup>
import { onLaunch, onShow } from '@dcloudio/uni-app'

/**
 * 应用启动时触发（仅一次）
 * 可在此处初始化全局数据、检查登录态等
 */
onLaunch(() => { console.log('App Launch') })

/**
 * 应用从后台切换到前台时触发
 * 可在此处刷新数据或检查 Token 有效性
 */
onShow(() => { console.log('App Show') })
</script>

<style>
/* 全局样式 */
page {
  background-color: #f8f9fb;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', sans-serif;
  font-size: 28rpx;
  color: #1a1a2e;
  line-height: 1.6;
}

/* 主题变量 */
:root {
  --primary: #4361ee;
  --primary-light: #eef1ff;
  --danger: #ef476f;
  --danger-light: #fde8ed;
  --success: #06d6a0;
  --success-light: #e5faf4;
  --warning: #ffd166;
  --warning-light: #fff8e6;
  --text-primary: #1a1a2e;
  --text-secondary: #6b7280;
  --text-hint: #9ca3af;
  --bg-page: #f8f9fb;
  --bg-card: #ffffff;
  --border: #e5e7eb;
  --shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 8rpx 40rpx rgba(0, 0, 0, 0.1);
  --radius: 20rpx;
  --radius-lg: 28rpx;
}

/* 通用卡片 */
.card {
  background: var(--bg-card);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  overflow: hidden;
}

/* 通用按钮 */
.btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 88rpx;
  border-radius: 16rpx;
  font-size: 30rpx;
  font-weight: 600;
  letter-spacing: 2rpx;
}

.btn-primary {
  background: linear-gradient(135deg, #4361ee, #3a56d4);
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(67, 97, 238, 0.35);
}

.btn-primary:active {
  opacity: 0.9;
  transform: scale(0.98);
}

.btn-danger {
  background: linear-gradient(135deg, #ef476f, #d63d63);
  color: #fff;
  box-shadow: 0 8rpx 24rpx rgba(239, 71, 111, 0.35);
}

.btn-ghost {
  background: transparent;
  color: var(--primary);
  border: 2rpx solid var(--primary);
}

.btn-block {
  width: 100%;
}

.btn-sm {
  height: 64rpx;
  font-size: 26rpx;
  border-radius: 12rpx;
}

.btn-disabled {
  opacity: 0.5;
  pointer-events: none;
}

/* 通用标签 */
.tag {
  display: inline-flex;
  align-items: center;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.tag-primary { background: var(--primary-light); color: var(--primary); }
.tag-danger { background: var(--danger-light); color: var(--danger); }
.tag-success { background: var(--success-light); color: var(--success); }
.tag-warning { background: var(--warning-light); color: #b8860b; }
.tag-gray { background: #f3f4f6; color: var(--text-hint); }

/* 通用输入框 */
.input-wrap {
  display: flex;
  align-items: center;
  height: 96rpx;
  padding: 0 28rpx;
  background: #f3f4f6;
  border-radius: 16rpx;
  transition: all 0.2s;
}

.input-wrap-focus {
  background: #fff;
  box-shadow: 0 0 0 3rpx rgba(67, 97, 238, 0.2);
}

.input-wrap input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
}

/* 通用空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 60rpx;
}

.empty-icon { font-size: 100rpx; margin-bottom: 24rpx; }
.empty-text { font-size: 28rpx; color: var(--text-hint); }
.empty-action { margin-top: 32rpx; }

/* 通用 section */
.section {
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 20rpx;
  padding: 0 4rpx;
}

/* 通用分割线 */
.divider {
  height: 1rpx;
  background: var(--border);
  margin: 20rpx 0;
}

/* 通用徽标 */
.badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background: var(--danger);
  color: #fff;
  font-size: 20rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 通用 loading */
.loading-spinner {
  width: 48rpx;
  height: 48rpx;
  border: 4rpx solid var(--border);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 通用过渡 */
.fade-in {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(16rpx); }
  to { opacity: 1; transform: translateY(0); }
}

/* 安全区域 */
.safe-bottom {
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
