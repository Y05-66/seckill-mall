/**
 * 收货地址模块 API
 *
 * 包含收货地址的增删改查、设置默认地址等接口（需登录）。
 * @module api/address
 */
import request from '../utils/request'

/**
 * 添加收货地址
 * @param {Object} data - { receiverName, receiverPhone, province, city, district, detailAddress }
 * @returns {Promise<{code:number}>}
 */
export function addAddress(data) {
  return request.post('/address', data)
}

/**
 * 获取收货地址列表
 * @returns {Promise<{code:number, data:Array<{id:number, receiverName:string, receiverPhone:string, ...}>}>}
 */
export function getAddressList() {
  return request.get('/address/list')
}

/**
 * 获取单个收货地址详情
 * @param {number} addressId - 地址ID
 * @returns {Promise<{code:number, data:{id:number, receiverName:string, ...}}>}
 */
export function getAddress(addressId) {
  return request.get('/address/' + addressId)
}

/**
 * 修改收货地址
 * @param {number} addressId - 地址ID
 * @param {Object} data - { receiverName?, receiverPhone?, province?, city?, district?, detailAddress? }
 * @returns {Promise<{code:number}>}
 */
export function updateAddress(addressId, data) {
  return request.put('/address/' + addressId, data)
}

/**
 * 删除收货地址
 * @param {number} addressId - 地址ID
 * @returns {Promise<{code:number}>}
 */
export function deleteAddress(addressId) {
  return request.delete('/address/' + addressId)
}

/**
 * 设置默认收货地址
 * @param {number} addressId - 地址ID
 * @returns {Promise<{code:number}>}
 */
export function setDefaultAddress(addressId) {
  return request.put('/address/' + addressId + '/default')
}

/**
 * 获取默认收货地址
 * @returns {Promise<{code:number, data:{id:number, receiverName:string, ...}}>}
 */
export function getDefaultAddress() {
  return request.get('/address/default')
}
