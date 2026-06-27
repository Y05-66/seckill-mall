/**
 * 商品模块 API
 *
 * 包含普通商品和秒杀商品的查询接口（无需登录）。
 * @module api/goods
 */
import { get } from '../utils/request'

/**
 * 获取普通商品列表
 * @returns {Promise<{code:number, data:Array<{id:number, goodsName:string, goodsPrice:number, ...}>}>}
 */
export const getGoodsList = () => get('/goods/list')

/**
 * 获取普通商品详情
 * @param {number} id - 商品ID
 * @returns {Promise<{code:number, data:{id:number, goodsName:string, goodsPrice:number, ...}}>}
 */
export const getGoodsDetail = (id) => get('/goods/' + id)

/**
 * 获取秒杀商品列表（含状态和倒计时）
 * @returns {Promise<{code:number, data:Array<{id:number, seckillPrice:number, status:number, remainSeconds:number, ...}>}>}
 */
export const getSeckillGoodsList = () => get('/goods/seckill/list')

/**
 * 获取秒杀商品详情
 * @param {number} id - 秒杀商品ID
 * @returns {Promise<{code:number, data:{id:number, seckillPrice:number, stockCount:number, ...}}>}
 */
export const getSeckillGoodsDetail = (id) => get('/goods/seckill/' + id)
