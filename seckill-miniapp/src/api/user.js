/**
 * 用户模块 API
 *
 * 包含登录、注册、获取用户信息接口。
 * @module api/user
 */
import { get, post, put } from '../utils/request'
import { API_HOST } from '../config'

// 上传文件基础地址（与 request.js 保持一致）
// #ifdef H5
const UPLOAD_BASE = '/api'
// #endif
// #ifdef MP-WEIXIN || MP-ALIPAY || APP-PLUS
const UPLOAD_BASE = API_HOST
// #endif

// 上传文件
function uploadFile(url, filePath, name = 'file') {
  return new Promise((resolve, reject) => {
    let token = ''
    try { token = uni.getStorageSync('token') || '' } catch (e) {}
    uni.uploadFile({
      url: UPLOAD_BASE + url,
      filePath,
      name,
      header: { Authorization: token ? 'Bearer ' + token : '' },
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (data.code === 200) resolve(data)
          else reject(data)
        } catch (e) { reject(e) }
      },
      fail: reject
    })
  })
}

/**
 * 用户登录
 * @param {Object} data - { username: string, password: string }
 * @returns {Promise<{code:number, data:{token:string, userId:number, username:string, nickname:string, role:number}}>}
 */
export const login = (data) => post('/user/login', data)

/**
 * 用户注册
 * @param {Object} data - { username: string, password: string, nickname?: string, phone?: string }
 * @returns {Promise<{code:number}>}
 */
export const register = (data) => post('/user/register', data)

/**
 * 获取当前登录用户信息（需 Token）
 * @returns {Promise<{code:number, data:{id:number, username:string, nickname:string, role:number, ...}}>}
 */
export const getUserInfo = () => get('/user/info')

/**
 * 修改个人信息
 * @param {Object} data - { nickname?, phone?, email?, avatar? }
 */
export const updateProfile = (data) => put('/user/info', data)

/**
 * 修改密码
 * @param {Object} data - { oldPassword: string, newPassword: string }
 */
export const changePassword = (data) => put('/user/password', data)

/**
 * 上传头像
 * @param {string} filePath - 本地文件路径
 */
export const uploadAvatar = (filePath) => uploadFile('/user/avatar', filePath)
