/**
 * 积分模块 API
 *
 * 包含积分查询和积分日志接口（需登录）。
 * @module api/points
 */
import request from '../utils/request'

/**
 * 获取我的积分信息
 * @returns {Promise<{code:number, data:{userId:number, points:number, totalEarned:number, totalUsed:number, ...}>>}
 */
export function getMyPoints() {
  return request.get('/points/my')
}

/**
 * 获取积分变动日志
 * @returns {Promise<{code:number, data:Array<{id:number, points:number, type:string, description:string, ...}>}>}
 */
export function getPointsLog() {
  return request.get('/points/log')
}
