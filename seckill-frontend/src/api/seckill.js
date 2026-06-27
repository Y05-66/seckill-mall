/**
 * 秒杀模块 API
 *
 * 包含执行秒杀和查询秒杀结果接口（需登录）。
 * @module api/seckill
 */
import request from '../utils/request'

/**
 * 执行秒杀
 * @param {number} seckillGoodsId - 秒杀商品ID
 * @param {string} captchaId - 验证码ID
 * @param {string} captchaCode - 验证码答案
 * @returns {Promise<{code:number, data:{status:string, orderNo?:string}}>}
 */
export function doSeckill(seckillGoodsId, captchaId, captchaCode) {
  const data = { seckillGoodsId }
  if (captchaId) data.captchaId = captchaId
  if (captchaCode) data.captchaCode = captchaCode
  return request.post('/seckill/do', data)
}

/**
 * 查询秒杀结果（前端轮询调用）
 * @param {number} seckillGoodsId - 秒杀商品ID
 * @returns {Promise<{code:number, data:{status:string, orderNo?:string, msg?:string}}>}
 * status: "queuing"=排队中, "success"=成功, "fail"=失败
 */
export function getSeckillResult(seckillGoodsId) {
  return request.get('/seckill/result/' + seckillGoodsId)
}
