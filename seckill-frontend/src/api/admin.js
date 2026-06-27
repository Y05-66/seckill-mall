/**
 * 管理端 API
 *
 * 包含商品管理、秒杀活动管理、订单管理、用户管理等管理端接口（需管理员权限）。
 * @module api/admin
 */
import request from '../utils/request'

// ==================== 商品管理 ====================

/**
 * 获取商品列表（管理端）
 * @returns {Promise<{code:number, data:Array}>}
 */
export function getAdminGoodsList() {
  return request.get('/admin/goods/list')
}

/**
 * 新增秒杀商品
 * @param {Object} data - 商品信息
 * @returns {Promise<{code:number}>}
 */
export function addGoods(data) {
  return request.post('/admin/goods', data)
}

/**
 * 编辑秒杀商品
 * @param {number} id - 商品ID
 * @param {Object} data - 商品信息
 * @returns {Promise<{code:number}>}
 */
export function updateGoods(id, data) {
  return request.put('/admin/goods/' + id, data)
}

/**
 * 删除秒杀商品
 * @param {number} id - 商品ID
 * @returns {Promise<{code:number}>}
 */
export function deleteGoods(id) {
  return request.delete('/admin/goods/' + id)
}

// ==================== 秒杀活动管理 ====================

/**
 * 获取秒杀活动列表
 * @returns {Promise<{code:number, data:Array}>}
 */
export function getAdminSeckillList() {
  return request.get('/admin/seckill/list')
}

/**
 * 创建秒杀活动
 * @param {Object} data - 活动信息
 * @returns {Promise<{code:number}>}
 */
export function createSeckillActivity(data) {
  return request.post('/admin/seckill', data)
}

/**
 * 编辑秒杀活动
 * @param {number} id - 活动ID
 * @param {Object} data - 活动信息
 * @returns {Promise<{code:number}>}
 */
export function updateSeckillActivity(id, data) {
  return request.put('/admin/seckill/' + id, data)
}

/**
 * 删除秒杀活动
 * @param {number} id - 活动ID
 * @returns {Promise<{code:number}>}
 */
export function deleteSeckillActivity(id) {
  return request.delete('/admin/seckill/' + id)
}

/**
 * 修改秒杀活动状态
 * @param {number} id - 活动ID
 * @param {number} status - 状态（0-未开始，1-进行中，2-已结束）
 * @returns {Promise<{code:number}>}
 */
export function updateSeckillStatus(id, status) {
  return request.put('/admin/seckill/' + id + '/status', { status })
}

// ==================== 订单管理 ====================

/**
 * 获取订单列表（管理端，支持筛选）
 * @param {Object} params - { status?, orderNo? }
 * @returns {Promise<{code:number, data:Array}>}
 */
export function getAdminOrderList(params) {
  return request.get('/admin/order/list', { params })
}

/**
 * 获取订单统计数据
 * @returns {Promise<{code:number, data:{totalOrders:number, paidCount:number, totalRevenue:number, ...}>>}
 */
export function getOrderStatistics() {
  return request.get('/admin/order/statistics')
}

// ==================== 用户管理 ====================

/**
 * 获取用户列表（管理端）
 * @returns {Promise<{code:number, data:Array}>}
 */
export function getAdminUserList() {
  return request.get('/admin/user/list')
}

/**
 * 修改用户角色
 * @param {number} id - 用户ID
 * @param {number} role - 角色（0-普通用户，1-管理员）
 * @returns {Promise<{code:number}>}
 */
export function updateUserRole(id, role) {
  return request.put('/admin/user/' + id + '/role', null, { params: { role } })
}

/**
 * 启用/禁用用户
 * @param {number} id - 用户ID
 * @param {number} status - 状态（0-启用，1-禁用）
 * @returns {Promise<{code:number}>}
 */
export function updateUserStatus(id, status) {
  return request.put('/admin/user/' + id + '/status', null, { params: { status } })
}

// ==================== 补库存 ====================

/**
 * 补充秒杀商品库存
 * @param {number} id - 秒杀商品ID
 * @param {number} count - 增加的库存数量
 * @returns {Promise<{code:number}>}
 */
export function addStock(id, count) {
  return request.put('/admin/goods/' + id + '/stock', null, { params: { count } })
}
