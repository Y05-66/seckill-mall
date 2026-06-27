<!--
  CaptchaPopup.vue - 验证码弹窗组件

  功能：显示算术验证码图片，用户输入计算结果后提交。
  验证码从 /captcha 接口获取（Base64 图片 + captchaId）。
  内置防重复请求机制（loading 标志）。

  Props:
    - visible {boolean} - 是否显示弹窗

  Events:
    - close - 关闭弹窗
    - submit(captchaId, captchaCode) - 用户提交验证码

  用法：<CaptchaPopup :visible="show" @close="show=false" @submit="onSubmit" />
-->
<template>
  <view v-if="visible" class="overlay" @tap="$emit('close')">
    <view class="popup" @tap.stop>
      <view class="popup-header">
        <text class="popup-title">安全验证</text>
        <text class="popup-close" @tap="$emit('close')">✕</text>
      </view>
      <view class="popup-body">
        <view class="captcha-img-wrap">
          <image v-if="captchaImg" :src="captchaImg" mode="aspectFit" class="captcha-img" />
          <view v-else class="captcha-loading">
            <view class="loading-spinner"></view>
          </view>
          <view class="refresh-tip" @tap.stop="refreshCaptcha">换一张</view>
        </view>
        <view class="input-wrap" :class="{ 'input-wrap-focus': focused }">
          <text class="input-icon">🔤</text>
          <input
            v-model="captchaInput"
            type="text"
            placeholder="请输入验证码（不区分大小写）"
            class="input"
            @focus="focused = true"
            @blur="focused = false"
            @confirm="submit"
          />
        </view>
      </view>
      <view class="popup-footer">
        <view class="btn btn-primary btn-block" @tap="submit">确认抢购</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { getCaptcha } from '../api/seckill'

const props = defineProps({ visible: Boolean })
const emit = defineEmits(['close', 'submit'])

const captchaId = ref('')
import { showToast } from '../utils/nav'

const captchaImg = ref('')
const captchaInput = ref('')
const loading = ref(false)
const focused = ref(false)

watch(() => props.visible, (val) => { if (val) refreshCaptcha() })

const refreshCaptcha = async () => {
  if (loading.value) return
  loading.value = true

  try {
    const res = await getCaptcha()
    captchaId.value = res.data.captchaId
    captchaImg.value = res.data.captchaImg
    captchaInput.value = ''
  } catch (e) {
    showToast({ title: '获取验证码失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const submit = () => {
  if (!captchaInput.value.trim()) {
    showToast({ title: '请输入验证码', icon: 'none' })
    return
  }
  emit('submit', captchaId.value, captchaInput.value.trim())
}
</script>

<style scoped>
.overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); z-index: 999;
  display: flex; align-items: center; justify-content: center;
}
.popup {
  width: 580rpx; background: #fff; border-radius: var(--radius-lg);
  overflow: hidden;
}

.popup-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 28rpx 32rpx; border-bottom: 1rpx solid #f3f4f6;
}
.popup-title { font-size: 30rpx; font-weight: 700; color: var(--text-primary); }
.popup-close { font-size: 32rpx; color: var(--text-hint); padding: 8rpx; }

.popup-body { padding: 32rpx; }
.captcha-img-wrap {
  text-align: center; margin-bottom: 24rpx;
  background: #f9fafb; border-radius: 12rpx; padding: 20rpx;
  position: relative;
}
.captcha-img { width: 400rpx; height: 140rpx; border-radius: 8rpx; }
.captcha-loading { height: 140rpx; display: flex; align-items: center; justify-content: center; }
.refresh-tip {
  font-size: 20rpx; color: var(--text-hint); margin-top: 12rpx;
}

.popup-footer { padding: 0 32rpx 32rpx; }
</style>
