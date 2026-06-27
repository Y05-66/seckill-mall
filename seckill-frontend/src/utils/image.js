/**
 * 图片工具函数
 */

// 默认占位图
const DEFAULT_PLACEHOLDER = '/placeholder.svg'

/**
 * 图片加载失败时的处理函数
 * 用法：@error="handleImageError"
 */
export function handleImageError(e) {
  // 避免无限循环：如果已经是占位图则不再替换
  if (e.target.src !== window.location.origin + DEFAULT_PLACEHOLDER && !e.target.dataset.fallback) {
    e.target.dataset.fallback = 'true'
    e.target.src = DEFAULT_PLACEHOLDER
  }
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
  handleImageError,
  getImageUrl,
  DEFAULT_PLACEHOLDER
}
