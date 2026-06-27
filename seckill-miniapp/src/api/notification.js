import { get, put } from '../utils/request'

export const getNotifications = () => get('/notification/list')
export const getUnreadCount = () => get('/notification/unread-count')
export const markAsRead = (id) => put('/notification/' + id + '/read')
export const markAllAsRead = () => put('/notification/read-all')
