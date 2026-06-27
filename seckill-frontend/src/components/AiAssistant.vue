<template>
  <div class="ai-assistant">
    <!-- 悬浮按钮 -->
    <div
      class="ai-float-btn"
      :class="{ active: isOpen, dragging: isDragging }"
      :style="btnStyle"
      @click="handleBtnClick"
      @mousedown="onDragStart"
      @touchstart.passive="onTouchStart"
    >
      <svg v-if="!isOpen" class="ai-float-icon" viewBox="0 0 24 24" fill="none">
        <path d="M12 2C6.48 2 2 5.92 2 10.5c0 2.6 1.46 4.93 3.77 6.47L4 22l4.5-2.25c1.14.35 2.37.55 3.63.55 5.63 0 10.13-3.58 10.13-8S17.75 2 12 2z" fill="currentColor" opacity="0.15"/>
        <path d="M12 3C6.75 3 2.5 6.58 2.5 11c0 2.4 1.3 4.55 3.35 5.98L4.5 21.5l4.25-2.12c1.05.3 2.17.47 3.35.47 5.25 0 9.5-3.25 9.5-7.25S17.25 3 12 3z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
        <circle cx="8.5" cy="10.5" r="1" fill="currentColor"/>
        <circle cx="12" cy="10.5" r="1" fill="currentColor"/>
        <circle cx="15.5" cy="10.5" r="1" fill="currentColor"/>
        <path d="M19 4l1.5 1.5L19 7M21 2l-1 3-3-1" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round" opacity="0.7"/>
      </svg>
      <svg v-else class="ai-float-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
        <path d="M18 6L6 18M6 6l12 12"/>
      </svg>
      <span v-if="!isOpen && unreadCount > 0" class="ai-badge">{{ unreadCount }}</span>
    </div>

    <!-- 聊天窗口 -->
    <Transition name="chat-window">
      <div v-if="isOpen" class="ai-chat-window" :class="{ dragging: isDragging }" :style="chatStyle">

        <!-- 会话列表侧边栏 -->
        <Transition name="sidebar">
          <div class="ai-sidebar" v-if="showSidebar">
            <div class="sidebar-header">
              <span class="sidebar-title">历史对话</span>
              <button class="sidebar-close-btn" @click="showSidebar = false">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                  <path d="M18 6L6 18M6 6l12 12"/>
                </svg>
              </button>
            </div>
            <button class="sidebar-new-btn" @click="handleNewConversation">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <path d="M12 5v14M5 12h14"/>
              </svg>
              <span>新对话</span>
            </button>
            <div class="sidebar-list">
              <template v-for="(group, groupName) in groupedConversations" :key="groupName">
                <div class="sidebar-group-title">{{ groupName }}</div>
                <div
                  v-for="conv in group"
                  :key="conv.id"
                  class="sidebar-item"
                  :class="{ active: currentConvId === conv.id }"
                  @click="switchConversation(conv.id)"
                >
                  <svg class="sidebar-item-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                  </svg>
                  <span class="sidebar-item-title">{{ conv.title }}</span>
                  <button class="sidebar-item-del" @click.stop="handleDeleteConversation(conv.id)" title="删除">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                      <path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
                    </svg>
                  </button>
                </div>
              </template>
              <div v-if="conversations.length === 0" class="sidebar-empty">
                <svg viewBox="0 0 24 24" fill="none" width="36" height="36" style="opacity:0.25">
                  <path d="M12 3C6.75 3 2.5 6.58 2.5 11c0 2.4 1.3 4.55 3.35 5.98L4.5 21.5l4.25-2.12c1.05.3 2.17.47 3.35.47 5.25 0 9.5-3.25 9.5-7.25S17.25 3 12 3z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/>
                  <circle cx="8.5" cy="10.5" r="0.8" fill="currentColor"/>
                  <circle cx="12" cy="10.5" r="0.8" fill="currentColor"/>
                  <circle cx="15.5" cy="10.5" r="0.8" fill="currentColor"/>
                </svg>
                <span>暂无对话记录</span>
              </div>
            </div>
          </div>
        </Transition>

        <!-- 头部 -->
        <div class="ai-header">
          <div class="ai-header-left">
            <button class="ai-header-btn" @click="toggleSidebar" title="历史对话">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20">
                <path d="M3 12h18M3 6h18M3 18h18"/>
              </svg>
            </button>
            <div class="ai-header-avatar">
              <svg viewBox="0 0 24 24" fill="none" width="20" height="20">
                <path d="M12 3C6.75 3 2.5 6.58 2.5 11c0 2.4 1.3 4.55 3.35 5.98L4.5 21.5l4.25-2.12c1.05.3 2.17.47 3.35.47 5.25 0 9.5-3.25 9.5-7.25S17.25 3 12 3z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                <circle cx="8.5" cy="10.5" r="1" fill="currentColor"/>
                <circle cx="12" cy="10.5" r="1" fill="currentColor"/>
                <circle cx="15.5" cy="10.5" r="1" fill="currentColor"/>
              </svg>
            </div>
            <div class="ai-header-info">
              <span class="ai-name">小秒助手</span>
              <span class="ai-status">在线 · AI 购物顾问</span>
            </div>
            <select class="ai-model-select" v-model="selectedModel" @change="saveModel" title="选择模型">
              <option v-for="m in models" :key="m.id" :value="m.id">{{ m.name }}</option>
            </select>
          </div>
          <div class="ai-header-actions">
            <button class="ai-header-btn" @click="handleNewConversation" title="新对话">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                <path d="M12 5v14M5 12h14"/>
              </svg>
            </button>
            <button class="ai-header-btn" @click="isOpen = false" title="关闭">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
                <path d="M18 6L6 18M6 6l12 12"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="ai-messages" ref="messagesRef" @scroll="onMessagesScroll">
          <!-- 欢迎页 -->
          <div v-if="messages.length === 0" class="ai-welcome">
            <div class="welcome-icon">
              <svg viewBox="0 0 24 24" fill="none" width="44" height="44">
                <path d="M12 2C6.48 2 2 5.92 2 10.5c0 2.6 1.46 4.93 3.77 6.47L4 22l4.5-2.25c1.14.35 2.37.55 3.63.55 5.63 0 10.13-3.58 10.13-8S17.75 2 12 2z" fill="currentColor" opacity="0.1"/>
                <path d="M12 3C6.75 3 2.5 6.58 2.5 11c0 2.4 1.3 4.55 3.35 5.98L4.5 21.5l4.25-2.12c1.05.3 2.17.47 3.35.47 5.25 0 9.5-3.25 9.5-7.25S17.25 3 12 3z" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/>
                <circle cx="8.5" cy="10.5" r="1" fill="currentColor"/>
                <circle cx="12" cy="10.5" r="1" fill="currentColor"/>
                <circle cx="15.5" cy="10.5" r="1" fill="currentColor"/>
                <path d="M19 4l1.5 1.5L19 7M21 2l-1 3-3-1" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round" opacity="0.5"/>
              </svg>
            </div>
            <h3 class="welcome-title">你好，我是小秒</h3>
            <p class="welcome-desc">秒杀商城的智能购物助手，有什么可以帮你的？</p>
            <div class="welcome-cards">
              <button class="welcome-card" @click="sendQuickQuestion('秒杀活动怎么参与？有什么规则？')">
                <span class="welcome-card-icon">🔥</span>
                <div class="welcome-card-body">
                  <span class="welcome-card-text">秒杀怎么玩？</span>
                  <span class="welcome-card-desc">了解秒杀规则和技巧</span>
                </div>
                <svg class="welcome-card-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
              </button>
              <button class="welcome-card" @click="sendQuickQuestion('有什么优惠券可以领？怎么使用？')">
                <span class="welcome-card-icon">🎫</span>
                <div class="welcome-card-body">
                  <span class="welcome-card-text">优惠券指南</span>
                  <span class="welcome-card-desc">领取和使用优惠券</span>
                </div>
                <svg class="welcome-card-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
              </button>
              <button class="welcome-card" @click="sendQuickQuestion('我的订单怎么查看？')">
                <span class="welcome-card-icon">📦</span>
                <div class="welcome-card-body">
                  <span class="welcome-card-text">查看订单</span>
                  <span class="welcome-card-desc">查询订单状态和物流</span>
                </div>
                <svg class="welcome-card-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
              </button>
              <button class="welcome-card" @click="sendQuickQuestion('退款政策是什么？怎么申请退款？')">
                <span class="welcome-card-icon">🔄</span>
                <div class="welcome-card-body">
                  <span class="welcome-card-text">退款政策</span>
                  <span class="welcome-card-desc">了解退换货流程</span>
                </div>
                <svg class="welcome-card-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 18l6-6-6-6"/></svg>
              </button>
            </div>
          </div>

          <!-- 消息列表 -->
          <div v-for="(msg, index) in messages" :key="index" class="ai-msg" :class="msg.role" :style="{ animationDelay: index * 30 + 'ms' }">
            <!-- 用户消息 -->
            <div v-if="msg.role === 'user'" class="msg-row user-row">
              <div class="msg-bubble user-bubble">
                <div class="msg-text">{{ msg.content }}</div>
              </div>
              <div class="msg-avatar user-avatar">
                <svg viewBox="0 0 24 24" fill="currentColor" width="18" height="18">
                  <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                </svg>
              </div>
            </div>
            <!-- AI 消息 -->
            <div v-else class="msg-row ai-row">
              <div class="msg-avatar ai-msg-avatar">
                <svg viewBox="0 0 24 24" fill="none" width="16" height="16">
                  <path d="M12 3C6.75 3 2.5 6.58 2.5 11c0 2.4 1.3 4.55 3.35 5.98L4.5 21.5l4.25-2.12c1.05.3 2.17.47 3.35.47 5.25 0 9.5-3.25 9.5-7.25S17.25 3 12 3z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                  <circle cx="8.5" cy="10.5" r="0.8" fill="currentColor"/>
                  <circle cx="12" cy="10.5" r="0.8" fill="currentColor"/>
                  <circle cx="15.5" cy="10.5" r="0.8" fill="currentColor"/>
                </svg>
              </div>
              <div class="msg-body">
                <div class="msg-content markdown-body" v-html="renderMarkdown(msg.content)"></div>
                <div class="msg-actions">
                  <button class="msg-action-btn" @click="copyMessage(msg.content)" title="复制">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                      <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/>
                    </svg>
                    <span>{{ copyBtnText }}</span>
                  </button>
                  <button v-if="index === messages.length - 1" class="msg-action-btn" @click="regenerate" title="重新生成">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                      <path d="M1 4v6h6"/><path d="M3.51 15a9 9 0 102.13-9.36L1 10"/>
                    </svg>
                    <span>重新生成</span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 加载中 -->
          <div v-if="loading" class="ai-msg assistant loading-msg">
            <div class="msg-row ai-row">
              <div class="msg-avatar ai-msg-avatar">
                <svg viewBox="0 0 24 24" fill="none" width="16" height="16">
                  <path d="M12 3C6.75 3 2.5 6.58 2.5 11c0 2.4 1.3 4.55 3.35 5.98L4.5 21.5l4.25-2.12c1.05.3 2.17.47 3.35.47 5.25 0 9.5-3.25 9.5-7.25S17.25 3 12 3z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/>
                  <circle cx="8.5" cy="10.5" r="0.8" fill="currentColor"/>
                  <circle cx="12" cy="10.5" r="0.8" fill="currentColor"/>
                  <circle cx="15.5" cy="10.5" r="0.8" fill="currentColor"/>
                </svg>
              </div>
              <div class="msg-body">
                <div class="typing-indicator">
                  <span class="typing-dot"></span>
                  <span class="typing-dot"></span>
                  <span class="typing-dot"></span>
                </div>
              </div>
            </div>
          </div>

          <!-- 回到底部按钮 -->
          <Transition name="fade">
            <button v-if="showScrollBtn" class="scroll-bottom-btn" @click="scrollToBottom(true)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <path d="M6 9l6 6 6-6"/>
              </svg>
            </button>
          </Transition>
        </div>

        <!-- 输入区域 -->
        <div class="ai-input-area">
          <div class="ai-input-wrapper">
            <textarea
              ref="inputRef"
              v-model="inputMessage"
              placeholder="输入你的问题..."
              class="ai-input"
              rows="1"
              @input="autoResize"
              @keydown="handleKeydown"
              :disabled="loading"
            ></textarea>
            <button
              class="ai-send-btn"
              @click="loading ? stopGenerate() : sendMessage()"
              :class="{ active: inputMessage.trim() || loading }"
            >
              <!-- 发送按钮 -->
              <svg v-if="!loading" viewBox="0 0 24 24" fill="currentColor" width="18" height="18">
                <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
              </svg>
              <!-- 停止按钮 -->
              <svg v-else viewBox="0 0 24 24" fill="currentColor" width="16" height="16">
                <rect x="6" y="6" width="12" height="12" rx="2"/>
              </svg>
            </button>
          </div>
          <div class="ai-input-hint">Enter 发送，Shift + Enter 换行</div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import { marked } from 'marked'
import hljs from 'highlight.js'
import DOMPurify from 'dompurify'
import { sendAiMessage, getConversations, createConversation, deleteConversation, getAiHistory } from '../api/ai'
import { useUserStore } from '../store/user'

const router = useRouter()

// 配置 marked
marked.setOptions({
  highlight: (code, lang) => {
    if (lang && hljs.getLanguage(lang)) {
      try { return hljs.highlight(code, { language: lang }).value } catch (e) {}
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true,
  gfm: true
})

const userStore = useUserStore()
const isOpen = ref(false)
const loading = ref(false)
const inputMessage = ref('')
const messages = ref([])
const messagesRef = ref(null)
const inputRef = ref(null)
const unreadCount = ref(0)
const showScrollBtn = ref(false)
const copyBtnText = ref('复制')
let abortController = null

// --- 模型选择 ---
const models = ref([
  { id: 'qwen-turbo', name: '极速', desc: '响应最快' },
  { id: 'qwen-plus', name: '增强', desc: '平衡性能' },
  { id: 'qwen-max', name: '旗舰', desc: '最强能力' }
])
const selectedModel = ref(localStorage.getItem('ai_model') || 'qwen-turbo')
const saveModel = () => { localStorage.setItem('ai_model', selectedModel.value) }

// --- 会话管理 ---
const showSidebar = ref(false)
const conversations = ref([])
const currentConvId = ref(null)

// 按时间分组的会话列表
const groupedConversations = computed(() => {
  const groups = {}
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 86400000)
  const weekAgo = new Date(today.getTime() - 7 * 86400000)

  conversations.value.forEach(conv => {
    const d = new Date(conv.updateTime)
    let label
    if (d >= today) label = '今天'
    else if (d >= yesterday) label = '昨天'
    else if (d >= weekAgo) label = '最近 7 天'
    else label = '更早'
    if (!groups[label]) groups[label] = []
    groups[label].push(conv)
  })
  return groups
})

// --- 拖拽状态 ---
const side = ref('right')
const offsetY = ref(100)
const isDragging = ref(false)
let startX = 0
let startY = 0
let startOffsetY = 0
let moved = false

const btnSize = 56
const edgeGap = 24
const chatWidth = 420
const chatGap = 12

const draggingLeft = ref(null)
const draggingBottom = ref(null)

const btnStyle = computed(() => {
  if (isDragging.value && draggingLeft.value !== null) {
    return { left: draggingLeft.value + 'px', bottom: draggingBottom.value + 'px' }
  }
  // 未打开时露出一半，打开后完全显示
  const hideOffset = isOpen.value ? edgeGap : -btnSize / 2
  return {
    left: side.value === 'left' ? hideOffset + 'px' : (window.innerWidth - btnSize - (isOpen.value ? edgeGap : -btnSize / 2)) + 'px',
    bottom: offsetY.value + 'px',
  }
})

const chatStyle = computed(() => {
  if (isDragging.value && draggingLeft.value !== null) {
    return { left: draggingLeft.value + 'px', bottom: draggingBottom.value + btnSize + chatGap + 'px' }
  }
  return {
    left: side.value === 'left' ? edgeGap + 'px' : (window.innerWidth - edgeGap - chatWidth) + 'px',
    bottom: offsetY.value + btnSize + chatGap + 'px',
  }
})

const toggleChat = () => {
  // 未登录时跳转登录页
  if (!userStore.token) {
    router.push('/login')
    return
  }
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    unreadCount.value = 0
    if (conversations.value.length === 0) {
      loadConversations()
    }
    nextTick(() => {
      scrollToBottom()
      inputRef.value?.focus()
    })
  }
}

const handleBtnClick = () => {
  if (!moved) toggleChat()
}

// --- 侧边栏 ---
const toggleSidebar = () => {
  showSidebar.value = !showSidebar.value
  if (showSidebar.value) loadConversations()
}

const loadConversations = async () => {
  try {
    const res = await getConversations()
    conversations.value = res.data || []
  } catch (e) {}
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

// --- 拖拽逻辑 ---
const onDragStart = (e) => {
  if (e.button !== 0) return
  isDragging.value = true; moved = false
  startX = e.clientX; startY = e.clientY; startOffsetY = offsetY.value
  const initLeft = side.value === 'left' ? edgeGap : window.innerWidth - edgeGap - btnSize
  draggingLeft.value = initLeft; draggingBottom.value = offsetY.value
  document.addEventListener('mousemove', onDragMove)
  document.addEventListener('mouseup', onDragEnd)
}

const onDragMove = (e) => {
  if (!isDragging.value) return
  const dx = e.clientX - startX; const dy = e.clientY - startY
  if (Math.abs(dx) > 3 || Math.abs(dy) > 3) moved = true
  draggingLeft.value = Math.max(edgeGap, Math.min(window.innerWidth - edgeGap - btnSize, e.clientX - btnSize / 2))
  const maxBottom = window.innerHeight - btnSize - 20
  draggingBottom.value = Math.max(20, Math.min(maxBottom, startOffsetY - dy))
}

const onDragEnd = () => {
  const finalLeft = draggingLeft.value; const mid = window.innerWidth / 2
  side.value = (finalLeft + btnSize / 2) < mid ? 'left' : 'right'
  offsetY.value = draggingBottom.value
  draggingLeft.value = null; draggingBottom.value = null; isDragging.value = false
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
}

// --- 触屏拖拽 ---
const onTouchStart = (e) => {
  const touch = e.touches[0]
  isDragging.value = true; moved = false
  startX = touch.clientX; startY = touch.clientY; startOffsetY = offsetY.value
  const initLeft = side.value === 'left' ? edgeGap : window.innerWidth - edgeGap - btnSize
  draggingLeft.value = initLeft; draggingBottom.value = offsetY.value
  document.addEventListener('touchmove', onTouchMove, { passive: false })
  document.addEventListener('touchend', onTouchEnd)
}

const onTouchMove = (e) => {
  if (!isDragging.value) return; e.preventDefault()
  const touch = e.touches[0]
  const dx = touch.clientX - startX; const dy = touch.clientY - startY
  if (Math.abs(dx) > 3 || Math.abs(dy) > 3) moved = true
  draggingLeft.value = Math.max(edgeGap, Math.min(window.innerWidth - edgeGap - btnSize, touch.clientX - btnSize / 2))
  const maxBottom = window.innerHeight - btnSize - 20
  draggingBottom.value = Math.max(20, Math.min(maxBottom, startOffsetY - dy))
}

const onTouchEnd = () => {
  const finalLeft = draggingLeft.value; const mid = window.innerWidth / 2
  side.value = (finalLeft + btnSize / 2) < mid ? 'left' : 'right'
  offsetY.value = draggingBottom.value
  draggingLeft.value = null; draggingBottom.value = null; isDragging.value = false
  document.removeEventListener('touchmove', onTouchMove)
  document.removeEventListener('touchend', onTouchEnd)
}

onBeforeUnmount(() => {
  document.removeEventListener('mousemove', onDragMove)
  document.removeEventListener('mouseup', onDragEnd)
  document.removeEventListener('touchmove', onTouchMove)
  document.removeEventListener('touchend', onTouchEnd)
})

// --- 消息逻辑 ---
const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return
  const userMsg = inputMessage.value.trim()
  inputMessage.value = ''
  resetTextarea()

  messages.value.push({ role: 'user', content: userMsg })
  nextTick(scrollToBottom)

  loading.value = true
  abortController = new AbortController()
  try {
    const res = await sendAiMessage(userMsg, currentConvId.value, abortController.signal, selectedModel.value)
    messages.value.push({ role: 'assistant', content: res.data.reply })
    if (!currentConvId.value && res.data.conversationId) {
      currentConvId.value = res.data.conversationId
    }
    loadConversations()
    if (!isOpen.value) unreadCount.value++
  } catch (e) {
    if (e.name === 'CanceledError' || e.name === 'AbortError') {
      // 用户主动取消，不添加错误消息
    } else {
      messages.value.push({ role: 'assistant', content: '抱歉，暂时无法回复，请稍后再试。' })
    }
  } finally {
    abortController = null
    loading.value = false
    nextTick(scrollToBottom)
  }
}

const regenerate = async () => {
  if (loading.value) return
  // 找到最后一条用户消息
  const lastUserMsg = [...messages.value].reverse().find(m => m.role === 'user')
  if (!lastUserMsg) return
  // 移除最后一条 AI 回复
  if (messages.value.length > 0 && messages.value[messages.value.length - 1].role === 'assistant') {
    messages.value.pop()
  }
  loading.value = true
  try {
    const res = await sendAiMessage(lastUserMsg.content, currentConvId.value, null, selectedModel.value)
    messages.value.push({ role: 'assistant', content: res.data.reply })
    loadConversations()
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，暂时无法回复，请稍后再试。' })
  } finally {
    loading.value = false
    nextTick(scrollToBottom)
  }
}

const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

const copyMessage = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    copyBtnText.value = '已复制!'
    setTimeout(() => { copyBtnText.value = '复制' }, 2000)
  } catch (e) {
    // fallback
    const ta = document.createElement('textarea')
    ta.value = text; document.body.appendChild(ta)
    ta.select(); document.execCommand('copy')
    document.body.removeChild(ta)
    copyBtnText.value = '已复制!'
    setTimeout(() => { copyBtnText.value = '复制' }, 2000)
  }
}

const stopGenerate = () => {
  if (abortController) { abortController.abort(); abortController = null }
  loading.value = false
}

// --- Markdown 渲染 ---
const renderMarkdown = (text) => {
  if (!text) return ''
  try {
    return DOMPurify.sanitize(marked(text))
  } catch (e) {
    return text.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>')
  }
}

// --- 输入框自动增长 ---
const autoResize = () => {
  const el = inputRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 150) + 'px'
}

const resetTextarea = () => {
  if (inputRef.value) {
    inputRef.value.style.height = 'auto'
  }
}

const handleKeydown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

// --- 滚动 ---
const scrollToBottom = (smooth = false) => {
  if (messagesRef.value) {
    messagesRef.value.scrollTo({
      top: messagesRef.value.scrollHeight,
      behavior: smooth ? 'smooth' : 'auto'
    })
  }
}

const onMessagesScroll = () => {
  if (!messagesRef.value) return
  const { scrollTop, scrollHeight, clientHeight } = messagesRef.value
  showScrollBtn.value = scrollHeight - scrollTop - clientHeight > 100
}

// --- 生命周期 ---
onMounted(async () => {
  if (userStore.token) {
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
/* ========== 变量 ========== */
.ai-assistant {
  --ai-primary: #e4393c;
  --ai-primary-light: #ff6b81;
  --ai-bg: #ffffff;
  --ai-msg-bg: #f7f7f8;
  --ai-text: #1a1a1a;
  --ai-text-secondary: #666;
  --ai-text-muted: #999;
  --ai-border: #e5e5e5;
  --ai-hover: #f0f0f0;
  --ai-radius: 12px;
  --ai-shadow: 0 10px 40px rgba(0, 0, 0, 0.12);
}

/* ========== 悬浮按钮 ========== */
.ai-float-btn {
  position: fixed;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, var(--ai-primary), var(--ai-primary-light));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(228, 57, 60, 0.35);
  transition: transform 0.3s, box-shadow 0.3s, left 0.25s ease, bottom 0.25s ease;
  z-index: 9999;
  user-select: none;
  -webkit-user-select: none;
  border: none;
  outline: none;
}

.ai-float-btn:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 24px rgba(228, 57, 60, 0.45);
}

/* 呼吸光晕动画 */
.ai-float-btn:not(.active)::before {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--ai-primary), var(--ai-primary-light));
  opacity: 0;
  animation: btnPulse 2.5s ease-in-out infinite;
  z-index: -1;
}

@keyframes btnPulse {
  0%, 100% { opacity: 0; transform: scale(0.95); }
  50% { opacity: 0.25; transform: scale(1.15); }
}

.ai-float-btn.active {
  background: #555;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
}

.ai-float-btn.dragging {
  cursor: grabbing;
  transform: scale(1.08);
  transition: none !important;
}

.ai-float-icon {
  width: 28px;
  height: 28px;
  color: white;
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.15));
}

.ai-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #ff4757;
  color: white;
  font-size: 11px;
  font-weight: 600;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  border: 2px solid white;
}

/* ========== 聊天窗口 ========== */
.ai-chat-window {
  position: fixed;
  width: 420px;
  height: 600px;
  background: var(--ai-bg);
  border-radius: 16px;
  box-shadow: var(--ai-shadow);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 9998;
  transition: left 0.25s ease, bottom 0.25s ease;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.ai-chat-window.dragging {
  transition: none !important;
  animation: none;
}

/* 窗口动画 */
.chat-window-enter-active { animation: chatOpen 0.25s ease; }
.chat-window-leave-active { animation: chatOpen 0.2s ease reverse; }
@keyframes chatOpen {
  from { opacity: 0; transform: scale(0.95) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

/* ========== 头部 ========== */
.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: linear-gradient(135deg, var(--ai-primary), var(--ai-primary-light));
  color: white;
  flex-shrink: 0;
}

.ai-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.ai-model-select {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  outline: none;
  flex-shrink: 0;
}

.ai-model-select option {
  color: #333;
  background: white;
}

.ai-header-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.ai-header-info {
  display: flex;
  flex-direction: column;
}

.ai-name {
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.ai-status {
  font-size: 11px;
  opacity: 0.85;
  margin-top: 1px;
}

.ai-header-actions {
  display: flex;
  gap: 4px;
}

.ai-header-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 6px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.85;
  transition: opacity 0.2s, background 0.2s;
}

.ai-header-btn:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.15);
}

/* ========== 侧边栏 ========== */
.ai-sidebar {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #fff;
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.sidebar-enter-active { animation: slideIn 0.25s ease; }
.sidebar-leave-active { animation: slideIn 0.2s ease reverse; }
@keyframes slideIn {
  from { transform: translateX(-100%); }
  to { transform: translateX(0); }
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--ai-border);
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--ai-text);
}

.sidebar-close-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  color: var(--ai-text-muted);
  border-radius: 6px;
  display: flex;
  align-items: center;
  transition: color 0.2s, background 0.2s;
}

.sidebar-close-btn:hover {
  color: var(--ai-text);
  background: var(--ai-hover);
}

.sidebar-new-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 12px 16px;
  padding: 10px 16px;
  background: none;
  border: 1px solid var(--ai-border);
  border-radius: 10px;
  cursor: pointer;
  color: var(--ai-text);
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s;
}

.sidebar-new-btn:hover {
  border-color: var(--ai-primary);
  color: var(--ai-primary);
  background: #fff5f5;
}

.sidebar-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px 12px;
}

.sidebar-group-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--ai-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 12px 12px 6px;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
  color: var(--ai-text-secondary);
}

.sidebar-item:hover {
  background: var(--ai-hover);
}

.sidebar-item.active {
  background: #fff5f5;
  color: var(--ai-primary);
}

.sidebar-item-icon {
  flex-shrink: 0;
  opacity: 0.5;
}

.sidebar-item-title {
  flex: 1;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-item-del {
  flex-shrink: 0;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  color: var(--ai-text-muted);
  border-radius: 4px;
  display: flex;
  align-items: center;
  opacity: 0;
  transition: opacity 0.15s, color 0.15s;
}

.sidebar-item:hover .sidebar-item-del {
  opacity: 1;
}

.sidebar-item-del:hover {
  color: var(--ai-primary);
}

.sidebar-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 60px 16px;
  color: var(--ai-text-muted);
  font-size: 13px;
}

/* ========== 消息列表 ========== */
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 0;
  background: var(--ai-bg);
  position: relative;
}

/* ========== 欢迎页 ========== */
.ai-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 20px 24px;
  text-align: center;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fff5f5, #ffe0e0);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ai-primary);
  margin-bottom: 18px;
  box-shadow: 0 4px 20px rgba(228, 57, 60, 0.1);
}

.welcome-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--ai-text);
  margin: 0 0 8px;
  letter-spacing: 0.3px;
}

.welcome-desc {
  font-size: 14px;
  color: var(--ai-text-secondary);
  margin: 0 0 24px;
}

.welcome-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  max-width: 360px;
}

.welcome-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: var(--ai-bg);
  border: 1px solid var(--ai-border);
  border-radius: 14px;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s;
}

.welcome-card:hover {
  border-color: var(--ai-primary);
  background: #fff8f8;
  box-shadow: 0 4px 14px rgba(228, 57, 60, 0.1);
  transform: translateX(3px);
}

.welcome-card-icon {
  font-size: 30px;
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff5f5;
  border-radius: 12px;
}

.welcome-card-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.welcome-card-text {
  font-size: 16px;
  font-weight: 600;
  color: var(--ai-text);
  line-height: 1.4;
}

.welcome-card-desc {
  font-size: 13px;
  color: var(--ai-text-muted);
}

.welcome-card-arrow {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  color: var(--ai-text-muted);
  transition: color 0.2s, transform 0.2s;
}

.welcome-card:hover .welcome-card-arrow {
  color: var(--ai-primary);
  transform: translateX(3px);
}

/* ========== 消息 ========== */
.ai-msg {
  animation: msgFadeIn 0.2s ease both;
}

@keyframes msgFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.msg-row {
  display: flex;
  gap: 10px;
  padding: 16px 20px;
  max-width: 100%;
}

.user-row {
  justify-content: flex-end;
}

.ai-row {
  justify-content: flex-start;
}

.msg-avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar {
  background: #e8e8e8;
  color: #666;
}

.ai-msg-avatar {
  background: linear-gradient(135deg, #fff5f5, #ffe8e8);
  color: var(--ai-primary);
}

.msg-bubble {
  max-width: 80%;
}

.user-bubble {
  background: var(--ai-primary);
  color: white;
  padding: 10px 16px;
  border-radius: 16px 16px 4px 16px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.msg-body {
  flex: 1;
  min-width: 0;
}

.msg-content {
  font-size: 14px;
  line-height: 1.7;
  color: var(--ai-text);
  word-break: break-word;
}

/* ========== Markdown 样式 ========== */
.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4) {
  margin: 16px 0 8px;
  font-weight: 600;
  line-height: 1.4;
}

.markdown-body :deep(h1) { font-size: 18px; }
.markdown-body :deep(h2) { font-size: 16px; }
.markdown-body :deep(h3) { font-size: 15px; }
.markdown-body :deep(h4) { font-size: 14px; }

.markdown-body :deep(p) {
  margin: 0 0 10px;
}

.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.markdown-body :deep(li) {
  margin: 4px 0;
}

.markdown-body :deep(strong) {
  font-weight: 600;
}

.markdown-body :deep(code) {
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  font-family: 'SF Mono', 'Monaco', 'Menlo', 'Consolas', monospace;
}

.markdown-body :deep(pre) {
  background: #1e1e1e;
  border-radius: 10px;
  margin: 10px 0;
  overflow: hidden;
}

.markdown-body :deep(pre code) {
  display: block;
  padding: 14px 16px;
  background: none;
  color: #d4d4d4;
  font-size: 12.5px;
  line-height: 1.6;
  overflow-x: auto;
}

.markdown-body :deep(blockquote) {
  margin: 10px 0;
  padding: 8px 16px;
  border-left: 3px solid var(--ai-primary);
  background: #fafafa;
  border-radius: 0 8px 8px 0;
  color: var(--ai-text-secondary);
}

.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 10px 0;
  font-size: 13px;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid var(--ai-border);
  padding: 8px 12px;
  text-align: left;
}

.markdown-body :deep(th) {
  background: #f5f5f5;
  font-weight: 600;
}

.markdown-body :deep(tr:nth-child(even)) {
  background: #fafafa;
}

.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid var(--ai-border);
  margin: 16px 0;
}

.markdown-body :deep(a) {
  color: var(--ai-primary);
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

/* ========== 消息操作按钮 ========== */
.msg-actions {
  display: flex;
  gap: 4px;
  margin-top: 6px;
  opacity: 0;
  transition: opacity 0.2s;
}

.ai-msg:hover .msg-actions {
  opacity: 1;
}

.msg-action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 11px;
  color: var(--ai-text-muted);
  transition: all 0.15s;
}

.msg-action-btn:hover {
  background: var(--ai-hover);
  color: var(--ai-text);
}

/* ========== 打字动画 ========== */
.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 12px 16px;
}

.typing-dot {
  width: 7px;
  height: 7px;
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

/* ========== 回到底部按钮 ========== */
.scroll-bottom-btn {
  position: sticky;
  bottom: 12px;
  left: 50%;
  transform: translateX(-50%);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: white;
  border: 1px solid var(--ai-border);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ai-text-secondary);
  transition: all 0.2s;
  z-index: 5;
}

.scroll-bottom-btn:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  color: var(--ai-text);
}

.fade-enter-active,
.fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }

/* ========== 输入区域 ========== */
.ai-input-area {
  padding: 12px 16px 10px;
  background: var(--ai-bg);
  border-top: 1px solid var(--ai-border);
  flex-shrink: 0;
}

.ai-input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  background: var(--ai-msg-bg);
  border: 1px solid var(--ai-border);
  border-radius: 14px;
  padding: 6px 6px 6px 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.ai-input-wrapper:focus-within {
  border-color: var(--ai-primary);
  box-shadow: 0 0 0 2px rgba(228, 57, 60, 0.1);
}

.ai-input {
  flex: 1;
  border: none;
  background: none;
  outline: none;
  font-size: 14px;
  line-height: 1.5;
  color: var(--ai-text);
  resize: none;
  max-height: 150px;
  padding: 6px 0;
  font-family: inherit;
}

.ai-input::placeholder {
  color: var(--ai-text-muted);
}

.ai-input:disabled {
  opacity: 0.6;
}

.ai-send-btn {
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  border: none;
  background: #e0e0e0;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.ai-send-btn.active {
  background: var(--ai-primary);
}

.ai-send-btn.active:hover {
  background: var(--ai-primary-light);
}

.ai-input-hint {
  font-size: 11px;
  color: var(--ai-text-muted);
  text-align: center;
  margin-top: 6px;
}

/* ========== 滚动条 ========== */
.ai-messages::-webkit-scrollbar,
.sidebar-list::-webkit-scrollbar {
  width: 5px;
}

.ai-messages::-webkit-scrollbar-thumb,
.sidebar-list::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

.ai-messages::-webkit-scrollbar-thumb:hover,
.sidebar-list::-webkit-scrollbar-thumb:hover {
  background: #ccc;
}

/* ========== highlight.js 主题覆盖 ========== */
.markdown-body :deep(.hljs-keyword) { color: #569cd6; }
.markdown-body :deep(.hljs-string) { color: #ce9178; }
.markdown-body :deep(.hljs-comment) { color: #6a9955; }
.markdown-body :deep(.hljs-function) { color: #dcdcaa; }
.markdown-body :deep(.hljs-number) { color: #b5cea8; }
.markdown-body :deep(.hljs-title) { color: #dcdcaa; }
.markdown-body :deep(.hljs-built_in) { color: #4ec9b0; }
.markdown-body :deep(.hljs-attr) { color: #9cdcfe; }
.markdown-body :deep(.hljs-variable) { color: #9cdcfe; }
.markdown-body :deep(.hljs-type) { color: #4ec9b0; }
.markdown-body :deep(.hljs-meta) { color: #c586c0; }
</style>
