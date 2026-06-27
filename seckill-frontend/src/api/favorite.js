/**
 * 收藏模块 API
 *
 * 包含商品收藏的添加、删除、查询等接口（需登录）。
 * @module api/favorite
 */
import request from '../utils/request'

/**
 * 添加商品收藏
 * @param {number} goodsId - 商品ID
 * @returns {Promise<{code:number}>}
 */
export function addFavorite(goodsId) {
  return request.post('/favorite/' + goodsId)
}

/**
 * 取消商品收藏
 * @param {number} goodsId - 商品ID
 * @returns {Promise<{code:number}>}
 */
export function removeFavorite(goodsId) {
  return request.delete('/favorite/' + goodsId)
}

/**
 * 检查商品是否已收藏
 * @param {number} goodsId - 商品ID
 * @returns {Promise<{code:number, data:boolean}>}
 */
export function isFavorite(goodsId) {
  return request.get('/favorite/check/' + goodsId)
}

/**
 * 获取收藏商品列表
 * @returns {Promise<{code:number, data:Array<{id:number, goodsName:string, goodsPrice:number, ...}>}>}
 */
export function getFavoriteList() {
  return request.get('/favorite/list')
}

/**
 * 获取收藏数量
 * @returns {Promise<{code:number, data:number}>}
 */
export function getFavoriteCount() {
  return request.get('/favorite/count')
}
