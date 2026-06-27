<!--
  AdminLayout.vue - 管理后台布局组件

  功能：提供管理页面的统一容器布局（内边距 + 最小高度 + 背景色）。
  使用插槽（slot）承载具体页面内容。

  用法：<AdminLayout>页面内容</AdminLayout>
-->
<template>
  <view class="admin-layout">
    <slot />
  </view>
</template>

<style scoped>
.admin-layout { padding: 20rpx; min-height: 100vh; background: #f5f5f5; }
</style>
