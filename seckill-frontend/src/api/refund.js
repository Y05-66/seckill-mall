/**
 * 退款模块 API
 *
 * 包含退款申请、退款记录查询、退款审批等接口。
 * @module api/refund
 */
import request from '../utils/request'

/**
 * 申请退款（需登录）
 * @param {Object} data - { orderNo: string, type: number, amount: number, reason: string }
 * @returns {Promise<{code:number}>}
 */
export function applyRefund(data) {
  return request.post('/refund/apply', data)
}

/**
 * 获取我的退款记录（需登录）
 * @returns {Promise<{code:number, data:Array<{id:number, orderNo:string, status:number, ...}>}>}
 */
export function getMyRefunds() {
  return request.get('/refund/my')
}

/**
 * 获取所有退款记录（管理员）
 * @returns {Promise<{code:number, data:Array}>}
 */
export function getAllRefunds() {
  return request.get('/refund/admin/list')
}

/**
 * 审批通过退款（管理员）
 * @param {number} id - 退款ID
 * @param {string} adminNote - 管理员备注
 * @returns {Promise<{code:number}>}
 */
export function approveRefund(id, adminNote) {
  return request.put('/refund/admin/' + id + '/approve', { adminNote })
}

/**
 * 拒绝退款（管理员）
 * @param {number} id - 退款ID
 * @param {string} adminNote - 拒绝原因
 * @returns {Promise<{code:number}>}
 */
export function rejectRefund(id, adminNote) {
  return request.put('/refund/admin/' + id + '/reject', { adminNote })
}
