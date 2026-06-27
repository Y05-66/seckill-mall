/**
 * AI购物助手 API
 */
import request from '../utils/request'

/**
 * 发送消息给AI助手
 * @param {string} message - 用户消息
 * @param {number|null} conversationId - 会话ID
 * @returns {Promise<{code:number, data:{reply:string, conversationId:number}}>}
 */
export const sendAiMessage = (message, conversationId, signal, model) =>
  request.post('/ai/chat', { message, conversationId: conversationId || undefined, model: model || undefined }, { signal, timeout: 60000 })

/**
 * 获取可用模型列表
 * @returns {Promise<{code:number, data:Array<{id:string, name:string, desc:string}>}>}
 */
export const getModels = () => request.get('/ai/models')

/**
 * 创建新会话
 * @returns {Promise<{code:number, data:{id:number}}>}
 */
export const createConversation = () => request.post('/ai/conversation')

/**
 * 获取会话列表
 * @returns {Promise<{code:number, data:Array}>}
 */
export const getConversations = () => request.get('/ai/conversations')

/**
 * 删除会话
 * @param {number} id - 会话ID
 * @returns {Promise<{code:number}>}
 */
export const deleteConversation = (id) => request.delete(`/ai/conversation/${id}`)

/**
 * 获取指定会话的消息历史
 * @param {number} conversationId - 会话ID
 * @returns {Promise<{code:number, data:Array}>}
 */
export const getAiHistory = (conversationId) => request.get('/ai/history', { params: { conversationId } })
