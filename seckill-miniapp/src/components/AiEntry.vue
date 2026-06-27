<!--
  AI助手入口按钮 - 浮动按钮，点击跳转到AI助手页面
-->
<template>
  <view
    class="ai-entry-btn"
    :class="{ dragging: isDragging }"
    :style="{ left: btnLeft + 'px', bottom: btnBottom + 'px' }"
    @tap="handleTap"
    @touchstart="onTouchStart"
    @touchmove.prevent="onTouchMove"
    @touchend="onTouchEnd"
  >
    <text class="ai-entry-icon">💬</text>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const isDragging = ref(false)
const btnLeft = ref(0)
const btnBottom = ref(160)
let startX = 0
let startY = 0
let startBottom = 0
let moved = false

/** 按钮尺寸和边距 */
const btnSize = 48
const edgeGap = 12

const getScreenW = () => {
  try { return uni.getSystemInfoSync().windowWidth } catch (e) { return 375 }
}
const getScreenH = () => {
  try { return uni.getSystemInfoSync().windowHeight } catch (e) { return 667 }
}

// 初始化位置（右侧，露出一半）
btnLeft.value = getScreenW() - btnSize / 2

/** 触摸开始 - 记录初始位置 */
const onTouchStart = (e) => {
  const touch = e.touches[0]
  isDragging.value = true
  moved = false
  startX = touch.clientX
  startY = touch.clientY
  startBottom = btnBottom.value
}

/** 触摸移动 - 更新按钮位置 */
const onTouchMove = (e) => {
  if (!isDragging.value) return
  const touch = e.touches[0]
  const dx = touch.clientX - startX
  const dy = touch.clientY - startY
  if (Math.abs(dx) > 3 || Math.abs(dy) > 3) moved = true

  const screenW = getScreenW()
  btnLeft.value = Math.max(-btnSize / 2, Math.min(screenW - btnSize / 2, touch.clientX - btnSize / 2))
  btnBottom.value = Math.max(20, Math.min(getScreenH() - btnSize - 20, startBottom - dy))
}

/** 触摸结束 - 吸附到最近的屏幕边缘 */
const onTouchEnd = () => {
  const screenW = getScreenW()
  const mid = screenW / 2
  // 吸附到最近的边缘
  if (btnLeft.value + btnSize / 2 < mid) {
    btnLeft.value = -btnSize / 2
  } else {
    btnLeft.value = screenW - btnSize / 2
  }
  isDragging.value = false
}

/** 点击按钮 - 未登录跳转登录页，已登录跳转AI助手页 */
const handleTap = () => {
  if (moved) return
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }
  uni.navigateTo({ url: '/pages/ai/chat' })
}
</script>

<style scoped>
.ai-entry-btn {
  position: fixed;
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #e4393c, #ff6b81);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(228, 57, 60, 0.3);
  z-index: 999;
  transition: left 0.2s ease;
}

.ai-entry-btn.dragging {
  transition: none !important;
  transform: scale(1.1);
}

.ai-entry-icon {
  font-size: 22px;
}
</style>
