/**
 * 用户模块 API
 *
 * 包含登录、注册、个人信息管理、头像上传等接口。
 * @module api/user
 */
import request from '../utils/request'

/**
 * 用户登录
 * @param {Object} data - { username: string, password: string }
 * @returns {Promise<{code:number, data:{token:string, userId:number, username:string, nickname:string, role:number}}>}
 */
export function login(data) {
  return request.post('/user/login', data)
}

/**
 * 用户注册
 * @param {Object} data - { username: string, password: string, nickname?: string, phone?: string }
 * @returns {Promise<{code:number}>}
 */
export function register(data) {
  return request.post('/user/register', data)
}

/**
 * 获取当前登录用户信息
 * @returns {Promise<{code:number, data:{id:number, username:string, nickname:string, role:number, ...}}>}
 */
export function getUserInfo() {
  return request.get('/user/info')
}

/**
 * 修改个人信息
 * @param {Object} data - { nickname?, phone?, email?, avatar? }
 * @returns {Promise<{code:number}>}
 */
export function updateProfile(data) {
  return request.put('/user/info', data)
}

/**
 * 修改密码
 * @param {Object} data - { oldPassword: string, newPassword: string }
 * @returns {Promise<{code:number}>}
 */
export function changePassword(data) {
  return request.put('/user/password', data)
}

/**
 * 上传头像
 * @param {File} file - 图片文件
 * @returns {Promise<{code:number, data:{url:string}}>}
 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/user/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
