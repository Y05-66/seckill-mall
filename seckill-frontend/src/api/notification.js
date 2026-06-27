/**
 * 消息通知模块 API
 *
 * 包含通知列表、未读数量、标记已读等接口（需登录）。
 * @module api/notification
 */
import request from '../utils/request'

/**
 * 获取通知列表
 * @returns {Promise<{code:number, data:Array<{id:number, title:string, content:string, isRead:number, ...}>}>}
 */
export function getNotifications() {
  return request.get('/notification/list')
}

/**
 * 获取未读通知数量
 * @returns {Promise<{code:number, data:number}>}
 */
export function getUnreadCount() {
  return request.get('/notification/unread-count')
}

/**
 * 标记单条通知为已读
 * @param {number} id - 通知ID
 * @returns {Promise<{code:number}>}
 */
export function markAsRead(id) {
  return request.put('/notification/' + id + '/read')
}

/**
 * 标记所有通知为已读
 * @returns {Promise<{code:number}>}
 */
export function markAllAsRead() {
  return request.put('/notification/read-all')
}
