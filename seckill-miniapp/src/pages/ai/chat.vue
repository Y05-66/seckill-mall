<!--
  AI助手独立页面（全屏）
  从各页面的 AiEntry 浮动按钮跳转进入。
  功能：发送消息、会话管理、模型选择、复制/重新生成消息。
-->
<template>
  <view class="ai-page" :style="{ height: windowHeight + 'px' }">
    <!-- 头部 -->
    <view class="ai-header">
      <view class="ai-header-left">
        <view class="ai-header-btn" @tap="goBack">
          <text class="header-btn-icon">←</text>
        </view>
        <view class="ai-header-avatar">
          <text class="header-avatar-icon">💬</text>
        </view>
        <view class="ai-header-info">
          <text class="ai-name">小秒助手</text>
          <text class="ai-status">在线 · AI 购物顾问</text>
        </view>
      </view>
      <view class="ai-header-right">
        <picker :range="modelNames" :value="modelIndex" @change="onModelChange">
          <view class="ai-model-tag">
            <text class="model-tag-text">{{ models[modelIndex]?.name || '极速' }}</text>
          </view>
        </picker>
        <view class="ai-header-btn" @tap="toggleSidebar">
          <text class="header-btn-icon">☰</text>
        </view>
      </view>
    </view>

    <!-- 会话侧边栏 -->
    <view class="ai-sidebar-mask" v-if="showSidebar" @tap="showSidebar = false" catchtouchmove>
      <view class="ai-sidebar" catchtap catchtouchmove>
        <view class="sidebar-header">
          <text class="sidebar-title">历史对话</text>
          <view class="sidebar-close-btn" @tap="showSidebar = false">
            <text class="sidebar-close-icon">✕</text>
          </view>
        </view>
        <view class="sidebar-new-btn" @tap="handleNewConversation">
          <text class="sidebar-new-icon">＋</text>
          <text class="sidebar-new-text">新对话</text>
        </view>
        <scroll-view class="sidebar-list" scroll-y>
          <view
            v-for="conv in conversations"
            :key="conv.id"
            class="sidebar-item"
            :class="{ active: currentConvId === conv.id }"
            @tap="switchConversation(conv.id)"
          >
            <text class="sidebar-item-icon">💬</text>
            <text class="sidebar-item-title">{{ conv.title }}</text>
            <view class="sidebar-item-del" @tap.stop="handleDeleteConversation(conv.id)">
              <text class="sidebar-del-icon">🗑</text>
            </view>
          </view>
          <view v-if="conversations.length === 0" class="sidebar-empty">
            <text class="sidebar-empty-icon">💬</text>
            <text class="sidebar-empty-text">暂无对话记录</text>
          </view>
        </scroll-view>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view
      class="ai-messages"
      scroll-y
      :scroll-into-view="scrollToId"
      scroll-with-animation
      enhanced
      :show-scrollbar="false"
    >
      <!-- 欢迎页 -->
      <view v-if="messages.length === 0" class="ai-welcome">
        <view class="welcome-icon-wrap">
          <text class="welcome-icon">💬</text>
        </view>
        <text class="welcome-title">你好，我是小秒</text>
        <text class="welcome-desc">秒杀商城的智能购物助手</text>
        <view class="welcome-cards">
          <view class="welcome-card" @tap="sendQuickQuestion('秒杀活动怎么参与？有什么规则？')">
            <text class="welcome-card-icon">🔥</text>
            <view class="welcome-card-body">
              <text class="welcome-card-text">秒杀怎么玩？</text>
              <text class="welcome-card-desc">了解秒杀规则和技巧</text>
            </view>
            <text class="welcome-card-arrow">›</text>
          </view>
          <view class="welcome-card" @tap="sendQuickQuestion('有什么优惠券可以领？怎么使用？')">
            <text class="welcome-card-icon">🎫</text>
            <view class="welcome-card-body">
              <text class="welcome-card-text">优惠券指南</text>
              <text class="welcome-card-desc">领取和使用优惠券</text>
            </view>
            <text class="welcome-card-arrow">›</text>
          </view>
          <view class="welcome-card" @tap="sendQuickQuestion('我的订单怎么查看？')">
            <text class="welcome-card-icon">📦</text>
            <view class="welcome-card-body">
              <text class="welcome-card-text">查看订单</text>
              <text class="welcome-card-desc">查询订单状态和物流</text>
            </view>
            <text class="welcome-card-arrow">›</text>
          </view>
          <view class="welcome-card" @tap="sendQuickQuestion('退款政策是什么？怎么申请退款？')">
            <text class="welcome-card-icon">🔄</text>
            <view class="welcome-card-body">
              <text class="welcome-card-text">退款政策</text>
              <text class="welcome-card-desc">了解退换货流程</text>
            </view>
            <text class="welcome-card-arrow">›</text>
          </view>
        </view>
      </view>

      <!-- 消息列表 -->
      <view
        v-for="(msg, index) in messages"
        :key="index"
        :id="'msg-' + index"
        class="ai-msg"
        :class="msg.role"
      >
        <!-- 用户消息 -->
        <view v-if="msg.role === 'user'" class="msg-row user-row">
          <view class="msg-bubble user-bubble">
            <view class="msg-text user-text">
              <text class="user-text-content">{{ msg.content }}</text>
            </view>
          </view>
          <view class="msg-avatar user-avatar">
            <text class="avatar-icon">👤</text>
          </view>
        </view>
        <!-- AI 消息 -->
        <view v-else class="msg-row ai-row">
          <view class="msg-avatar ai-msg-avatar">
            <text class="avatar-icon">💬</text>
          </view>
          <view class="msg-body">
            <view class="msg-text ai-text">
              <text class="ai-text-content">{{ msg.content }}</text>
            </view>
            <view class="msg-actions">
              <view class="msg-action-btn" @tap="copyMessage(msg.content)">
                <text class="action-icon">📋</text>
                <text class="action-text">{{ copyBtnText }}</text>
              </view>
              <view v-if="index === messages.length - 1" class="msg-action-btn" @tap="regenerate">
                <text class="action-icon">🔄</text>
                <text class="action-text">重新生成</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载中 -->
      <view v-if="loading" id="msg-loading" class="ai-msg assistant">
        <view class="msg-row ai-row">
          <view class="msg-avatar ai-msg-avatar">
            <text class="avatar-icon">💬</text>
          </view>
          <view class="typing-indicator">
            <view class="typing-dot"></view>
            <view class="typing-dot"></view>
            <view class="typing-dot"></view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区域 -->
    <view class="ai-input-area">
      <view class="ai-input-wrapper">
        <input
          v-model="inputMessage"
          placeholder="输入你的问题..."
          class="ai-input"
          confirm-type="send"
          @confirm="sendMessage"
          :disabled="loading"
        />
        <view
          class="ai-send-btn"
          :class="{ active: inputMessage.trim() || loading }"
          @tap="loading ? stopGenerate() : sendMessage()"
        >
          <text class="send-icon">{{ loading ? '■' : '➤' }}</text>
        </view>
      </view>
      <text class="ai-input-hint">Enter 发送</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { sendAiMessage, getConversations, deleteConversation, getAiHistory } from '../../api/ai'

const loading = ref(false)
const inputMessage = ref('')
const messages = ref([])
const scrollToId = ref('')
const copyBtnText = ref('复制')

// 窗口高度（小程序中 100vh 不可靠）
let windowHeight = 667
try { windowHeight = uni.getSystemInfoSync().windowHeight } catch (e) {}

// --- 模型选择 ---
const models = ref([
  { id: 'qwen-turbo', name: '极速', desc: '响应最快' },
  { id: 'qwen-plus', name: '增强', desc: '平衡性能' },
  { id: 'qwen-max', name: '旗舰', desc: '最强能力' }
])
const modelNames = computed(() => models.value.map(m => m.name))
const modelIndex = ref(0)
const selectedModel = computed(() => models.value[modelIndex.value]?.id || 'qwen-turbo')

try {
  const saved = uni.getStorageSync('ai_model')
  if (saved) {
    const idx = models.value.findIndex(m => m.id === saved)
    if (idx >= 0) modelIndex.value = idx
  }
} catch (e) {}

/** 切换AI模型并持久化到本地存储 */
const onModelChange = (e) => {
  modelIndex.value = e.detail.value
  try { uni.setStorageSync('ai_model', selectedModel.value) } catch (e) {}
}

// --- 会话管理 ---
const showSidebar = ref(false)
const conversations = ref([])
const currentConvId = ref(null)

const toggleSidebar = () => {
  showSidebar.value = !showSidebar.value
  if (showSidebar.value) loadConversations()
}

const loadConversations = async () => {
  try {
    const res = await getConversations()
    conversations.value = res.data || []
  } catch (e) {
    // 加载失败时静默处理
  }
}

const switchConversation = async (convId) => {
  if (currentConvId.value === convId) { showSidebar.value = false; return }
  currentConvId.value = convId
  showSidebar.value = false
  messages.value = []
  try {
    const res = await getAiHistory(convId)
    if (res.data && res.data.length > 0) messages.value = res.data
    nextTick(scrollToBottom)
  } catch (e) {}
}

const handleNewConversation = () => {
  showSidebar.value = false
  messages.value = []
  currentConvId.value = null
}

const handleDeleteConversation = async (convId) => {
  try {
    await deleteConversation(convId)
    conversations.value = conversations.value.filter(c => c.id !== convId)
    if (currentConvId.value === convId) { currentConvId.value = null; messages.value = [] }
  } catch (e) {}
}

// --- 消息逻辑 ---
/** 请求取消标志，点击停止按钮时设为 true */
let cancelled = false

/** 发送消息：追加用户消息 → 调用AI API → 追加AI回复 */
const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return
  const userMsg = inputMessage.value.trim()
  inputMessage.value = ''
  cancelled = false

  messages.value.push({ role: 'user', content: userMsg })
  nextTick(scrollToBottom)

  loading.value = true
  try {
    const res = await sendAiMessage(userMsg, currentConvId.value, selectedModel.value)
    if (cancelled) return
    messages.value.push({ role: 'assistant', content: res.data.reply })
    if (!currentConvId.value && res.data.conversationId) currentConvId.value = res.data.conversationId
    loadConversations()
  } catch (e) {
    if (cancelled) return
    messages.value.push({ role: 'assistant', content: '抱歉，暂时无法回复，请稍后再试。' })
  } finally {
    loading.value = false
    nextTick(scrollToBottom)
  }
}

/** 重新生成最后一条AI回复 */
const regenerate = async () => {
  if (loading.value) return
  const lastUserMsg = [...messages.value].reverse().find(m => m.role === 'user')
  if (!lastUserMsg) return
  if (messages.value.length > 0 && messages.value[messages.value.length - 1].role === 'assistant') messages.value.pop()
  loading.value = true
  try {
    const res = await sendAiMessage(lastUserMsg.content, currentConvId.value, selectedModel.value)
    messages.value.push({ role: 'assistant', content: res.data.reply })
    loadConversations()
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，暂时无法回复，请稍后再试。' })
  } finally {
    loading.value = false
    nextTick(scrollToBottom)
  }
}

const sendQuickQuestion = (q) => { inputMessage.value = q; sendMessage() }

const copyMessage = async (text) => {
  try {
    uni.setClipboardData({
      data: text,
      success: () => {
        copyBtnText.value = '已复制!'
        setTimeout(() => { copyBtnText.value = '复制' }, 2000)
      }
    })
  } catch (e) {}
}

/** 停止生成：设置取消标志，清除加载状态 */
const stopGenerate = () => { cancelled = true; loading.value = false }

/** 返回上一页，无历史时跳转首页 */
const goBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({ url: '/pages/index/index' })
  }
}

/** 滚动到最新消息 */
const scrollToBottom = () => {
  nextTick(() => {
    if (loading.value) scrollToId.value = 'msg-loading'
    else if (messages.value.length > 0) scrollToId.value = 'msg-' + (messages.value.length - 1)
  })
}

onMounted(async () => {
  const token = uni.getStorageSync('token')
  if (token) {
    try {
      await loadConversations()
      if (conversations.value.length > 0) {
        const latest = conversations.value[0]
        currentConvId.value = latest.id
        const res = await getAiHistory(latest.id)
        if (res.data && res.data.length > 0) messages.value = res.data
      }
    } catch (e) {}
  }
})
</script>

<style scoped>
.ai-page {
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow: hidden;
}

/* ========== 头部 ========== */
.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 28rpx;
  background: linear-gradient(135deg, #e4393c, #ff6b81);
  flex-shrink: 0;
  padding-top: 80rpx;
}

.ai-header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
}

.ai-header-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.ai-header-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-avatar-icon {
  font-size: 32rpx;
}

.ai-header-info {
  display: flex;
  flex-direction: column;
}

.ai-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #fff;
}

.ai-status {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.8);
}

.ai-header-btn {
  padding: 12rpx;
}

.header-btn-icon {
  font-size: 36rpx;
  color: rgba(255, 255, 255, 0.9);
}

.ai-model-tag {
  background: rgba(255, 255, 255, 0.2);
  border: 2rpx solid rgba(255, 255, 255, 0.3);
  padding: 8rpx 20rpx;
  border-radius: 16rpx;
}

.model-tag-text {
  font-size: 22rpx;
  color: #fff;
}

/* ========== 侧边栏 ========== */
.ai-sidebar-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 100;
}

.ai-sidebar {
  position: absolute;
  top: 0;
  left: 0;
  width: 70%;
  height: 100%;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 28rpx;
  padding-top: 80rpx;
  border-bottom: 2rpx solid #e5e5e5;
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1a1a;
}

.sidebar-close-btn {
  padding: 8rpx;
}

.sidebar-close-icon {
  font-size: 28rpx;
  color: #999;
}

.sidebar-new-btn {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin: 20rpx 24rpx;
  padding: 20rpx 28rpx;
  border: 2rpx solid #e5e5e5;
  border-radius: 16rpx;
}

.sidebar-new-icon {
  font-size: 28rpx;
  color: #e4393c;
}

.sidebar-new-text {
  font-size: 26rpx;
  color: #e4393c;
  font-weight: 500;
}

.sidebar-list {
  flex: 1;
  padding: 0 16rpx 24rpx;
}

.sidebar-group-title {
  font-size: 22rpx;
  font-weight: 600;
  color: #999;
  padding: 20rpx 16rpx 12rpx;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 20rpx 16rpx;
  border-radius: 12rpx;
}

.sidebar-item.active {
  background: #fff5f5;
}

.sidebar-item-icon {
  font-size: 28rpx;
  opacity: 0.5;
}

.sidebar-item-title {
  flex: 1;
  font-size: 26rpx;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-item.active .sidebar-item-title {
  color: #e4393c;
}

.sidebar-item-del {
  padding: 8rpx;
}

.sidebar-del-icon {
  font-size: 24rpx;
  opacity: 0.4;
}

.sidebar-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  padding: 100rpx 32rpx;
}

.sidebar-empty-icon {
  font-size: 56rpx;
  opacity: 0.25;
}

.sidebar-empty-text {
  font-size: 24rpx;
  color: #999;
}

/* ========== 消息列表 ========== */
.ai-messages {
  flex: 1;
  background: #fff;
}

/* ========== 欢迎页 ========== */
.ai-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 40rpx 40rpx;
}

.welcome-icon-wrap {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #fff5f5, #ffe0e0);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
}

.welcome-icon {
  font-size: 56rpx;
}

.welcome-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8rpx;
}

.welcome-desc {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 40rpx;
}

.welcome-cards {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  width: 100%;
}

.welcome-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx 28rpx;
  background: #fff;
  border: 2rpx solid #e5e5e5;
  border-radius: 20rpx;
}

.welcome-card-icon {
  font-size: 48rpx;
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff5f5;
  border-radius: 16rpx;
  flex-shrink: 0;
}

.welcome-card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.welcome-card-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1a1a;
}

.welcome-card-desc {
  font-size: 22rpx;
  color: #999;
}

.welcome-card-arrow {
  font-size: 36rpx;
  color: #999;
  flex-shrink: 0;
}

/* ========== 消息 ========== */
.ai-msg {
  margin-bottom: 8rpx;
}

.msg-row {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 28rpx;
}

.user-row {
  justify-content: flex-end;
}

.ai-row {
  justify-content: flex-start;
}

.msg-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-avatar {
  background: #e8e8e8;
}

.ai-msg-avatar {
  background: linear-gradient(135deg, #fff5f5, #ffe8e8);
}

.avatar-icon {
  font-size: 28rpx;
}

.msg-bubble {
  max-width: 78%;
}

.user-bubble {
  background: #e4393c;
  padding: 20rpx 28rpx;
  border-radius: 32rpx 32rpx 8rpx 32rpx;
}

.user-text {
  font-size: 28rpx;
  color: #fff;
  line-height: 1.5;
}

.user-text-content {
  font-size: 28rpx;
  color: #fff;
  line-height: 1.5;
}

.msg-body {
  flex: 1;
  min-width: 0;
}

.ai-text {
  font-size: 28rpx;
  color: #1a1a1a;
  line-height: 1.6;
  padding: 20rpx 28rpx;
  background: #f7f7f8;
  border-radius: 32rpx 32rpx 32rpx 8rpx;
  word-break: break-all;
}

.ai-text-content {
  font-size: 28rpx;
  color: #1a1a1a;
  line-height: 1.6;
}

/* ========== 消息操作 ========== */
.msg-actions {
  display: flex;
  gap: 8rpx;
  margin-top: 12rpx;
}

.msg-action-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
  background: #f0f0f0;
}

.action-icon {
  font-size: 22rpx;
}

.action-text {
  font-size: 20rpx;
  color: #999;
}

/* ========== 打字动画 ========== */
.typing-indicator {
  display: flex;
  gap: 8rpx;
  padding: 20rpx 28rpx;
  background: #f7f7f8;
  border-radius: 32rpx 32rpx 32rpx 8rpx;
}

.typing-dot {
  width: 12rpx;
  height: 12rpx;
  background: #bbb;
  border-radius: 50%;
  animation: typingBounce 1.4s infinite ease-in-out;
}

.typing-dot:nth-child(1) { animation-delay: -0.32s; }
.typing-dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes typingBounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* ========== 输入区域 ========== */
.ai-input-area {
  padding: 20rpx 28rpx;
  padding-bottom: 40rpx;
  background: #fff;
  border-top: 2rpx solid #e5e5e5;
  flex-shrink: 0;
}

.ai-input-wrapper {
  display: flex;
  align-items: center;
  gap: 12rpx;
  background: #f7f7f8;
  border: 2rpx solid #e5e5e5;
  border-radius: 24rpx;
  padding: 8rpx 8rpx 8rpx 24rpx;
}

.ai-input {
  flex: 1;
  font-size: 28rpx;
  color: #1a1a1a;
}

.ai-send-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  background: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ai-send-btn.active {
  background: #e4393c;
}

.send-icon {
  font-size: 28rpx;
  color: #fff;
}

.ai-input-hint {
  font-size: 20rpx;
  color: #999;
  text-align: center;
  margin-top: 8rpx;
}
</style>
