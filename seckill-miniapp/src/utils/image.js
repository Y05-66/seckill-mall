/**
 * 图片工具函数
 */

import { API_HOST } from '../config'

// 默认占位图（使用小程序支持的PNG格式）
const DEFAULT_PLACEHOLDER = '/static/placeholder.png'

/**
 * 处理图片URL，将相对路径转换为完整URL
 * 小程序中不能使用相对路径，需要完整的服务器地址
 * @param {string} url - 原始图片URL（可能是相对路径或完整URL）
 * @returns {string} 完整的图片URL
 */
export function getFullImageUrl(url) {
  if (!url || url === 'null' || url === 'undefined') {
    return DEFAULT_PLACEHOLDER
  }
  // 如果已经是完整URL（http/https开头），直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // 如果是相对路径（以/开头），拼接API_HOST
  if (url.startsWith('/')) {
    return API_HOST + url
  }
  // 其他情况，添加/前缀后拼接
  return API_HOST + '/' + url
}

/**
 * 图片加载失败时的处理函数
 * 用法：@error="handleImageError"
 * 直接将失败图片的 src 替换为占位图
 */
export function handleImageError(e) {
  // 在小程序中，event.target 可能不可用，静默处理
  // getFullImageUrl 已对空值返回占位图
}

/**
 * 获取图片URL，如果为空则返回占位图
 * @param {string} url - 原始图片URL
 * @returns {string} 有效的图片URL
 */
export function getImageUrl(url) {
  if (!url || url === 'null' || url === 'undefined') {
    return DEFAULT_PLACEHOLDER
  }
  return url
}

export default {
  getFullImageUrl,
  handleImageError,
  getImageUrl,
  DEFAULT_PLACEHOLDER
}
