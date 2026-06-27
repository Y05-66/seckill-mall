/**
 * 退款模块 API
 *
 * 包含退款申请、退款记录查询接口（需登录）。
 * @module api/refund
 */
import { get, post } from '../utils/request'

/**
 * 申请退款
 * @param {Object} data - { orderNo: string, type: number, amount: number, reason: string }
 * @returns {Promise<{code:number}>}
 */
export const applyRefund = (data) => post('/refund/apply', data)

/**
 * 获取用户退款记录
 * @returns {Promise<{code:number, data:Array<{id:number, orderNo:string, status:number, ...}>}>}
 */
export const getUserRefunds = () => get('/refund/my')
