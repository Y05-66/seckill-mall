/**
 * 秒杀模块 API
 *
 * 包含验证码获取、执行秒杀、轮询结果接口。
 * @module api/seckill
 */
import { get, post } from '../utils/request'

/**
 * 获取算术验证码（图片 Base64 + captchaId）
 * @returns {Promise<{code:number, data:{captchaId:string, captchaImg:string}}>}
 */
export const getCaptcha = () => get('/captcha')

/**
 * 执行秒杀
 * @param {number} seckillGoodsId - 秒杀商品ID
 * @param {string} [captchaId] - 验证码ID（可选）
 * @param {string} [captchaCode] - 用户输入的验证码答案（可选）
 * @returns {Promise<{code:number, data:{status:string, orderNo?:string}}>}
 *   status: "success" | "queuing"
 */
export const doSeckill = (seckillGoodsId, captchaId, captchaCode) => {
  const data = { seckillGoodsId }
  if (captchaId) data.captchaId = captchaId
  if (captchaCode) data.captchaCode = captchaCode
  return post('/seckill/do', data)
}

/**
 * 轮询秒杀结果
 * @param {number} seckillGoodsId - 秒杀商品ID
 * @returns {Promise<{code:number, data:{status:string, orderNo?:string, msg?:string}}>}
 *   status: "queuing" | "success" | "fail"
 */
export const getSeckillResult = (seckillGoodsId) => get('/seckill/result/' + seckillGoodsId)
