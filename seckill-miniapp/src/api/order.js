/**
 * 订单模块 API
 *
 * 包含订单列表、详情、支付、取消接口（需登录）。
 * @module api/order
 */
import { get, post, del } from '../utils/request'

/**
 * 获取当前用户订单列表
 * @returns {Promise<{code:number, data:Array<{orderNo:string, goodsName:string, status:number, ...}>}>}
 */
export const getOrderList = () => get('/order/list')

/**
 * 获取订单详情
 * @param {string} orderNo - 订单号
 * @returns {Promise<{code:number, data:{orderNo:string, goodsName:string, status:number, ...}}>}
 */
export const getOrderDetail = (orderNo) => get('/order/' + orderNo)

/**
 * 支付订单（模拟支付）
 * @param {string} orderNo - 订单号
 * @returns {Promise<{code:number}>}
 */
export const payOrder = (orderNo) => post('/order/pay/' + orderNo)

/**
 * 取消订单（回滚库存）
 * @param {string} orderNo - 订单号
 * @returns {Promise<{code:number}>}
 */
export const cancelOrder = (orderNo) => post('/order/cancel/' + orderNo)

/**
 * 删除订单（仅限已取消、已超时、已退款的订单）
 * @param {string} orderNo - 订单号
 * @returns {Promise<{code:number}>}
 */
export const deleteOrder = (orderNo) => del('/order/' + orderNo)
