/**
 * 验证码模块 API
 *
 * 包含验证码获取接口，用于秒杀前的身份验证。
 * @module api/captcha
 */
import request from '../utils/request'

/**
 * 获取图形验证码
 * @returns {Promise<{code:number, data:{captchaId:string, image:string}}>}
 * captchaId: 验证码ID（用于后续校验）
 * image: Base64编码的验证码图片
 */
export function getCaptcha() {
  return request.get('/captcha')
}
