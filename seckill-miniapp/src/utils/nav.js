/**
 * 安全导航工具 - 解决 uni 在 script setup 中可能未定义的问题
 */

export function switchTab(url) {
  try {
    if (typeof uni !== 'undefined' && uni.switchTab) {
      uni.switchTab({ url })
    }
  } catch (e) {
    console.warn('switchTab failed:', url, e)
  }
}

export function navigateTo(url) {
  try {
    if (typeof uni !== 'undefined' && uni.navigateTo) {
      uni.navigateTo({ url })
    }
  } catch (e) {
    console.warn('navigateTo failed:', url, e)
  }
}

export function navigateBack(delta = 1) {
  try {
    if (typeof uni !== 'undefined' && uni.navigateBack) {
      uni.navigateBack({ delta })
    }
  } catch (e) {
    console.warn('navigateBack failed:', e)
  }
}

export function redirectTo(url) {
  try {
    if (typeof uni !== 'undefined' && uni.redirectTo) {
      uni.redirectTo({ url })
    }
  } catch (e) {
    console.warn('redirectTo failed:', url, e)
  }
}

export function showToast(options) {
  try {
    if (typeof uni !== 'undefined' && uni.showToast) {
      uni.showToast(options)
    }
  } catch (e) {
    console.warn('showToast failed:', e)
  }
}

export function showModal(options) {
  try {
    if (typeof uni !== 'undefined' && uni.showModal) {
      return uni.showModal(options)
    }
  } catch (e) {
    console.warn('showModal failed:', e)
  }
  return Promise.resolve({ confirm: false })
}
