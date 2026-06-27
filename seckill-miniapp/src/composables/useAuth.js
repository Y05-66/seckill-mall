/**
 * 登录状态检查组合函数
 *
 * 提供统一的登录状态检查逻辑，避免在每个页面重复实现。
 * 用法：
 *   import { useAuth } from '../../composables/useAuth'
 *   const { checkLogin } = useAuth()
 *   if (!checkLogin()) return
 */

import { showToast, navigateTo } from '../utils/nav'

export function useAuth() {
  /**
   * 检查用户是否已登录
   * @returns {boolean} true=已登录, false=未登录（已自动跳转登录页）
   */
  const checkLogin = () => {
    const token = uni.getStorageSync('token')
    if (!token) {
      navigateTo('/pages/login/login')
      return false
    }
    return true
  }

  /**
   * 获取当前登录用户ID
   * @returns {number|null} 用户ID，未登录返回null
   */
  const getUserId = () => {
    try {
      const userInfo = uni.getStorageSync('userInfo') || {}
      return userInfo.userId || null
    } catch (e) {
      return null
    }
  }

  /**
   * 获取当前用户角色
   * @returns {number} 0=普通用户, 1=管理员
   */
  const getUserRole = () => {
    try {
      const userInfo = uni.getStorageSync('userInfo') || {}
      return Number(userInfo.role || 0)
    } catch (e) {
      return 0
    }
  }

  /**
   * 检查是否为管理员
   * @returns {boolean}
   */
  const isAdmin = () => {
    return getUserRole() === 1
  }

  return {
    checkLogin,
    getUserId,
    getUserRole,
    isAdmin
  }
}
