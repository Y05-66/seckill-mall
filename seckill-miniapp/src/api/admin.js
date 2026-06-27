/**
 * 管理端 API
 *
 * 包含秒杀商品管理、秒杀活动管理、订单管理、用户管理等接口。
 * 所有接口需管理员权限（role=1）。
 * @module api/admin
 */
import { get, post, put, del } from '../utils/request'

// ==================== 秒杀商品管理 ====================

/** 获取秒杀商品列表（含已结束的） */
export const getAdminGoodsList = () => get('/admin/goods/list')

/** 新增秒杀商品 */
export const addGoods = (data) => post('/admin/goods', data)

/** 编辑秒杀商品（仅未开始状态可修改） */
export const updateGoods = (id, data) => put('/admin/goods/' + id, data)

/** 删除秒杀商品（进行中不可删除） */
export const deleteGoods = (id) => del('/admin/goods/' + id)

/** 补充秒杀库存（同步更新DB和Redis） */
export const addStock = (id, count) => put('/admin/goods/' + id + '/stock?count=' + count)

// ==================== 秒杀活动管理 ====================

/** 获取秒杀活动列表 */
export const getAdminSeckillList = () => get('/admin/seckill/list')

/** 创建秒杀活动 */
export const createSeckillActivity = (data) => post('/admin/seckill', data)

/** 编辑秒杀活动（仅未开始状态可修改） */
export const updateSeckillActivity = (id, data) => put('/admin/seckill/' + id, data)

/** 删除秒杀活动 */
export const deleteSeckillActivity = (id) => del('/admin/seckill/' + id)

/**
 * 更新秒杀活动状态（手动开始/结束）
 * @param {number} id - 秒杀活动ID
 * @param {number} status - 目标状态：1=开始，2=结束
 */
export const updateSeckillStatus = (id, status) => put('/admin/seckill/' + id + '/status', { status })

// ==================== 订单管理 ====================

/**
 * 获取全部订单列表（支持筛选）
 * @param {Object} [params] - { status?: number, orderNo?: string }
 */
export const getAdminOrderList = (params) => get('/admin/order/list', params)

/** 获取订单统计数据 */
export const getOrderStatistics = () => get('/admin/order/statistics')

// ==================== 用户管理 ====================

/** 获取全部用户列表 */
export const getAdminUserList = () => get('/admin/user/list')

/**
 * 修改用户角色
 * @param {number} id - 用户ID
 * @param {number} role - 目标角色：0=普通用户，1=管理员
 */
export const updateUserRole = (id, role) => put('/admin/user/' + id + '/role?role=' + role)

/**
 * 启用/禁用用户
 * @param {number} id - 用户ID
 * @param {number} status - 目标状态：0=启用，1=禁用
 */
export const updateUserStatus = (id, status) => put('/admin/user/' + id + '/status?status=' + status)
