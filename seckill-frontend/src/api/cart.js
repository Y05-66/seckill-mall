/**
 * 购物车模块 API
 *
 * 包含购物车的增删改查、选中状态管理等接口（需登录）。
 * @module api/cart
 */
import request from '../utils/request'

/**
 * 添加商品到购物车
 * @param {Object} data - { goodsId: number, quantity?: number }
 * @returns {Promise<{code:number}>}
 */
export function addToCart(data) {
  return request.post('/cart/add', data)
}

/**
 * 获取购物车列表
 * @returns {Promise<{code:number, data:Array<{id:number, goodsId:number, goodsName:string, goodsPrice:number, quantity:number, checked:number, ...}>}>}
 */
export function getCartList() {
  return request.get('/cart/list')
}

/**
 * 更新购物车商品数量
 * @param {Object} data - { cartId: number, quantity: number }
 * @returns {Promise<{code:number}>}
 */
export function updateCartQuantity(data) {
  return request.put('/cart/update', data)
}

/**
 * 删除购物车商品
 * @param {number} cartId - 购物车项ID
 * @returns {Promise<{code:number}>}
 */
export function deleteCartItem(cartId) {
  return request.delete('/cart/' + cartId)
}

/**
 * 清空购物车
 * @returns {Promise<{code:number}>}
 */
export function clearCart() {
  return request.delete('/cart/clear')
}

/**
 * 选中/取消选中购物车商品
 * @param {Object} data - { cartId: number, checked: number (0或1) }
 * @returns {Promise<{code:number}>}
 */
export function toggleCheck(data) {
  return request.put('/cart/check', data)
}

/**
 * 全选/取消全选
 * @param {Object} data - { checked: number (0或1) }
 * @returns {Promise<{code:number}>}
 */
export function toggleCheckAll(data) {
  return request.put('/cart/check-all', data)
}

/**
 * 获取购物车商品数量
 * @returns {Promise<{code:number, data:number}>}
 */
export function getCartCount() {
  return request.get('/cart/count')
}
