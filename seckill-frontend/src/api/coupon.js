/**
 * 优惠券模块 API
 *
 * 包含优惠券查询、领取、我的优惠券等接口。
 * @module api/coupon
 */
import request from '../utils/request'

/**
 * 获取可领取的优惠券列表（无需登录）
 * @returns {Promise<{code:number, data:Array<{id:number, name:string, type:number, value:number, ...}>}>}
 */
export function getAvailableCoupons() {
  return request.get('/coupon/available')
}

/**
 * 领取优惠券（需登录）
 * @param {number} couponId - 优惠券ID
 * @returns {Promise<{code:number}>}
 */
export function claimCoupon(couponId) {
  return request.post('/coupon/claim/' + couponId)
}

/**
 * 获取我的优惠券列表（需登录）
 * @returns {Promise<{code:number, data:Array<{id:number, couponName:string, status:number, ...}>}>}
 */
export function getMyCoupons() {
  return request.get('/coupon/my')
}
