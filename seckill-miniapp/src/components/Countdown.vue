<!--
  Countdown.vue - 倒计时组件

  功能：显示 HH:MM:SS 格式的倒计时，每秒自动递减。
  支持两种模式：
  - type="start"：距开始（蓝色主题）
  - type="end"：距结束（红色主题）

  Props:
    - seconds {number} - 剩余秒数（必填）
    - type {string} - 倒计时类型："start" | "end"，默认 "end"

  Events:
    - finished - 倒计时归零时触发

  用法：<Countdown :seconds="60" type="end" @finished="onFinished" />
-->
<template>
  <view class="countdown" :class="type === 'start' ? 'cd-wait' : 'cd-active'">
    <text class="cd-label">{{ type === 'start' ? '距开始' : '距结束' }}</text>
    <view class="cd-blocks">
      <view v-if="hours > 0" class="cd-block">{{ pad(hours) }}</view>
      <view v-if="hours > 0" class="cd-sep">:</view>
      <view class="cd-block">{{ pad(minutes) }}</view>
      <view class="cd-sep">:</view>
      <view class="cd-block">{{ pad(seconds) }}</view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  seconds: { type: Number, required: true },
  type: { type: String, default: 'end' }
})

const emit = defineEmits(['finished'])

const remaining = ref(props.seconds || 0)
let timer = null

const hours = computed(() => Math.floor(remaining.value / 3600))
const minutes = computed(() => Math.floor((remaining.value % 3600) / 60))
const seconds = computed(() => remaining.value % 60)

const pad = (n) => String(n).padStart(2, '0')

const tick = () => {
  if (remaining.value > 0) {
    remaining.value--
  } else {
    clearInterval(timer)
    timer = null
    emit('finished')
  }
}

const startTimer = () => {
  if (timer) clearInterval(timer)
  if (remaining.value > 0) {
    timer = setInterval(tick, 1000)
  }
}

watch(() => props.seconds, (val) => {
  remaining.value = val || 0
  startTimer()
})

onMounted(startTimer)
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<style scoped>
.countdown { display: flex; align-items: center; gap: 8rpx; }
.cd-label { font-size: 22rpx; font-weight: 500; }
.cd-active .cd-label { color: var(--danger); }
.cd-wait .cd-label { color: var(--primary); }

.cd-blocks { display: flex; align-items: center; gap: 4rpx; }
.cd-block {
  min-width: 40rpx; height: 40rpx; padding: 0 6rpx;
  border-radius: 8rpx; font-size: 22rpx; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.cd-active .cd-block { background: var(--danger); color: #fff; }
.cd-wait .cd-block { background: var(--primary); color: #fff; }
.cd-sep { font-size: 20rpx; font-weight: 700; }
.cd-active .cd-sep { color: var(--danger); }
.cd-wait .cd-sep { color: var(--primary); }
</style>
