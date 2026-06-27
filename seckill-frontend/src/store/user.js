/**
 * 用户状态管理 Store（Pinia）
 *
 * 管理用户登录态、Token、用户信息等全局状态。
 * 数据持久化到 localStorage，页面刷新后自动恢复。
 *
 * @module store/user
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 从 localStorage 恢复状态
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const nickname = ref(localStorage.getItem('nickname') || '')
  const role = ref(Number(localStorage.getItem('role') || 0))

  /**
   * 设置登录信息（登录成功后调用）
   * @param {Object} data - { token, userId, username, nickname, role }
   */
  function setUser(data) {
    token.value = data.token
    userId.value = data.userId
    username.value = data.username
    nickname.value = data.nickname
    role.value = data.role ?? 0

    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', data.userId ?? '')
    localStorage.setItem('username', data.username ?? '')
    localStorage.setItem('nickname', data.nickname ?? '')
    localStorage.setItem('role', String(data.role ?? 0))
  }

  /**
   * 更新用户信息（合并而非覆盖）
   * @param {Object} info - 要更新的字段 { nickname, phone, email, avatar }
   */
  function updateUserInfo(info) {
    if (info.nickname !== undefined) {
      nickname.value = info.nickname
      localStorage.setItem('nickname', info.nickname)
    }
    if (info.username !== undefined) {
      username.value = info.username
      localStorage.setItem('username', info.username)
    }
  }

  /**
   * 退出登录（清除所有用户状态）
   */
  function logout() {
    token.value = ''
    userId.value = ''
    username.value = ''
    nickname.value = ''
    role.value = 0
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('nickname')
    localStorage.removeItem('role')
  }

  return { token, userId, username, nickname, role, setUser, updateUserInfo, logout }
})
