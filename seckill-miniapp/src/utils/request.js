/**
 * HTTP 请求封装
 * 基于 uni.request，适配小程序/H5 多端环境
 */

import { API_HOST } from '../config'

// API 基础地址
// #ifdef H5
const BASE_URL = '/api'
// #endif
// #ifdef MP-WEIXIN || MP-ALIPAY || APP-PLUS
const BASE_URL = API_HOST
// #endif

/**
 * 检查 JWT 是否过期
 * @param {string} token
 * @returns {boolean} true=已过期
 */
function isTokenExpired(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000 < Date.now()
  } catch (e) {
    return true
  }
}

/**
 * 清除登录态并跳转登录页
 */
function clearAuthAndRedirect() {
  try {
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
  } catch (e) {}
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  if (currentPage && currentPage.route !== 'pages/login/login') {
    try {
      uni.redirectTo({ url: '/pages/login/login' })
    } catch (e) {}
  }
}

/**
 * 封装 uni.request
 */
function request(options) {
  return new Promise((resolve, reject) => {
    // 安全获取 token
    let token = ''
    try {
      if (typeof uni !== 'undefined' && uni.getStorageSync) {
        token = uni.getStorageSync('token') || ''
      }
    } catch (e) {}

    // 检查 token 是否过期，过期则直接跳登录，不发请求
    if (token && isTokenExpired(token)) {
      clearAuthAndRedirect()
      reject({ code: 401, msg: '登录已过期' })
      return
    }

    const header = {
      'Content-Type': 'application/json',
      ...options.header
    }
    if (token) {
      header['Authorization'] = 'Bearer ' + token
    }

    // 安全调用 uni.request
    if (typeof uni === 'undefined' || !uni.request) {
      reject(new Error('uni is not available'))
      return
    }

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header,
      timeout: 15000,
      success: (res) => {
        const data = res.data
        // 服务器崩溃时响应体可能为空
        if (!data) {
          try { uni.showToast({ title: '服务器异常', icon: 'none' }) } catch (e) {}
          reject(new Error('服务器异常'))
          return
        }
        if (res.statusCode === 401 || res.statusCode === 403) {
          clearAuthAndRedirect()
          reject(data)
          return
        }
        if (data.code === 200) {
          resolve(data)
        } else if (data.code === 401 || data.code === 403 || data.code === 4014) {
          clearAuthAndRedirect()
          reject(data)
        } else {
          try { uni.showToast({ title: data.msg || '请求失败', icon: 'none' }) } catch (e) {}
          reject(data)
        }
      },
      fail: (err) => {
        // HTTP 4xx/5xx 可能走 fail 回调，尝试提取服务端错误信息
        const serverMsg = err?.data?.msg || err?.errMsg
        if (serverMsg && !serverMsg.includes('timeout')) {
          try { uni.showToast({ title: serverMsg, icon: 'none' }) } catch (e) {}
        } else {
          try { uni.showToast({ title: '网络错误', icon: 'none' }) } catch (e) {}
        }
        reject(err)
      }
    })
  })
}

export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })
export const put = (url, data) => request({ url, method: 'PUT', data })
export const del = (url, data) => request({ url, method: 'DELETE', data })

export default request
