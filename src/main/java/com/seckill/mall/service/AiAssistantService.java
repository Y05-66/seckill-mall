package com.seckill.mall.service;

import com.seckill.mall.entity.AiConversation;

import java.util.List;

/**
 * AI购物助手服务接口
 */
public interface AiAssistantService {

    /**
     * 发送消息给AI助手并获取回复
     *
     * @param userId         用户ID
     * @param message        用户消息
     * @param conversationId 会话ID（null则自动创建新会话）
     * @param model          模型名称（null则使用默认模型）
     * @return AI回复
     */
    ChatResult chat(Long userId, String message, Long conversationId, String model);

    /**
     * 创建新会话
     *
     * @param userId 用户ID
     * @return 新会话
     */
    AiConversation createConversation(Long userId);

    /**
     * 获取用户的会话列表（按更新时间倒序）
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<AiConversation> getConversations(Long userId);

    /**
     * 删除会话（含消息历史）
     *
     * @param userId         用户ID
     * @param conversationId 会话ID
     */
    void deleteConversation(Long userId, Long conversationId);

    /**
     * 获取指定会话的消息历史
     *
     * @param userId         用户ID
     * @param conversationId 会话ID
     * @return 消息列表
     */
    List<ChatMessage> getHistory(Long userId, Long conversationId);

    /**
     * 对话结果
     */
    class ChatResult {
        private String reply;
        private Long conversationId;

        public ChatResult() {}

        public ChatResult(String reply, Long conversationId) {
            this.reply = reply;
            this.conversationId = conversationId;
        }

        public String getReply() { return reply; }
        public void setReply(String reply) { this.reply = reply; }
        public Long getConversationId() { return conversationId; }
        public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    }

    /**
     * 对话消息类
     */
    class ChatMessage {
        private String role;
        private String content;
        private long timestamp;

        public ChatMessage() {}

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
            this.timestamp = System.currentTimeMillis();
        }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
