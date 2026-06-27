/**
 * 评价模块 API
 *
 * 包含商品评价的添加和查询接口。
 * @module api/review
 */
import request from '../utils/request'

/**
 * 添加商品评价（需登录）
 * @param {Object} data - { goodsId: number, rating: number, content: string, orderNo?: string }
 * @returns {Promise<{code:number}>}
 */
export function addReview(data) {
  return request.post('/review', data)
}

/**
 * 获取商品的评价列表（无需登录）
 * @param {number} goodsId - 商品ID
 * @returns {Promise<{code:number, data:Array<{id:number, userId:number, rating:number, content:string, ...}>}>}
 */
export function getGoodsReviews(goodsId) {
  return request.get('/review/goods/' + goodsId)
}

/**
 * 获取我的评价列表（需登录）
 * @returns {Promise<{code:number, data:Array<{id:number, goodsId:number, rating:number, content:string, ...}>}>}
 */
export function getMyReviews() {
  return request.get('/review/my')
}
