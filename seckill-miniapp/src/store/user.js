/**
 * 用户状态管理 Store（Pinia）
 *
 * 管理用户登录态、Token、用户信息等全局状态。
 * 数据持久化到 uni.storage，页面刷新后自动恢复。
 *
 * @module store/user
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 安全存储操作
function safeGetStorage(key) {
  try {
    if (typeof uni !== 'undefined' && uni.getStorageSync) {
      return uni.getStorageSync(key)
    }
  } catch (e) {}
  return null
}

function safeSetStorage(key, value) {
  try {
    if (typeof uni !== 'undefined' && uni.setStorageSync) {
      uni.setStorageSync(key, value)
    }
  } catch (e) {}
}

function safeRemoveStorage(key) {
  try {
    if (typeof uni !== 'undefined' && uni.removeStorageSync) {
      uni.removeStorageSync(key)
    }
  } catch (e) {}
}

export const useUserStore = defineStore('user', () => {
  // 从本地存储恢复状态（应用启动时自动恢复上次登录态）
  const token = ref(safeGetStorage('token') || '')
  const userInfo = ref(safeGetStorage('userInfo') || {})

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const nickname = computed(() => userInfo.value.nickname || userInfo.value.username || '未登录')
  const role = computed(() => userInfo.value.role ?? 0)
  const isAdmin = computed(() => role.value === 1)

  /**
   * 设置登录信息
   * @param {Object} data - 登录响应数据 { token, userId, username, nickname, role }
   */
  function setUser(data) {
    token.value = data.token
    userInfo.value = {
      userId: data.userId,
      username: data.username,
      nickname: data.nickname,
      role: data.role
    }
    safeSetStorage('token', data.token)
    safeSetStorage('userInfo', userInfo.value)
  }

  /**
   * 更新用户信息（如从 /user/info 获取的完整信息）
   */
  function updateUserInfo(info) {
    userInfo.value = { ...userInfo.value, ...info }
    safeSetStorage('userInfo', userInfo.value)
  }

  /**
   * 退出登录
   */
  function logout() {
    token.value = ''
    userInfo.value = {}
    safeRemoveStorage('token')
    safeRemoveStorage('userInfo')
  }

  return {
    token, userInfo, isLoggedIn, nickname, role, isAdmin,
    setUser, updateUserInfo, logout
  }
})
