package com.seckill.mall.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.entity.AiConversation;
import com.seckill.mall.entity.AiMessage;
import com.seckill.mall.mapper.AiConversationMapper;
import com.seckill.mall.mapper.AiMessageMapper;
import com.seckill.mall.service.AiAssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * AI购物助手服务实现类
 * 使用阿里云百练平台 DashScope SDK
 * 消息历史持久化到数据库，AI上下文窗口使用内存缓存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:qwen-turbo}")
    private String model;

    @Value("${ai.max-tokens:1500}")
    private int maxTokens;

    @Value("${ai.system-prompt:你是购物助手}")
    private String systemPrompt;

    /** AI上下文窗口缓存（仅用于 DashScope API 调用，key = "user:{userId}:conv:{convId}"） */
    private final ConcurrentMap<String, List<Message>> contextCache = new ConcurrentHashMap<>();

    /** 最大上下文消息数 */
    private static final int MAX_HISTORY_SIZE = 20;

    /** 缓存最大条目数，超出时清理最旧的条目 */
    private static final int MAX_CACHE_SIZE = 500;

    /**
     * 发送消息给AI助手并获取回复
     * <p>
     * 流程：自动创建会话（如无） → 持久化用户消息 → 调用AI API → 持久化回复 → 更新会话标题
     * </p>
     *
     * @param userId         用户ID
     * @param message        用户消息内容
     * @param conversationId 会话ID（null则自动创建新会话）
     * @param model          AI模型名称（null则使用默认模型）
     * @return AI回复结果，包含回复内容和会话ID
     */
    @Override
    public ChatResult chat(Long userId, String message, Long conversationId, String model) {
        // 如果没有传会话ID，自动创建新会话
        if (conversationId == null) {
            AiConversation conv = createConversation(userId);
            conversationId = conv.getId();
        }

        // 使用指定模型或默认模型
        String useModel = (model != null && !model.isEmpty()) ? model : this.model;

        // 持久化用户消息
        saveMessage(conversationId, "user", message);

        String reply;
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("sk-your")) {
            reply = getFallbackResponse(message);
        } else {
            reply = callAiApi(userId, conversationId, message, useModel);
        }

        // 持久化AI回复
        saveMessage(conversationId, "assistant", reply);

        // 自动设置会话标题：取第一条用户消息前20字
        AiConversation conv = conversationMapper.selectById(conversationId);
        if (conv != null && "新对话".equals(conv.getTitle())) {
            String title = message.length() > 20 ? message.substring(0, 20) + "..." : message;
            conv.setTitle(title);
            conversationMapper.updateById(conv);
        }

        return new ChatResult(reply, conversationId);
    }

    /**
     * 保存消息到数据库
     */
    private void saveMessage(Long conversationId, String role, String content) {
        AiMessage msg = new AiMessage();
        msg.setConversationId(conversationId);
        msg.setRole(role);
        msg.setContent(content);
        messageMapper.insert(msg);
    }

    /**
     * 调用DashScope AI API获取回复
     * <p>
     * 维护内存中的上下文窗口（最近20条消息），用于AI理解对话历史。
     * 超过缓存上限时清空全部缓存以避免OOM。
     * </p>
     *
     * @param userId         用户ID
     * @param conversationId 会话ID
     * @param message        用户消息
     * @param useModel       AI模型名称
     * @return AI回复文本
     */
    private String callAiApi(Long userId, Long conversationId, String message, String useModel) {
        try {
            // 设置全局 API Key（DashScope SDK 要求）
            synchronized (Constants.class) {
                Constants.apiKey = apiKey;
            }

            // 缓存条目过多时清理
            if (contextCache.size() > MAX_CACHE_SIZE) {
                contextCache.clear();
            }

            String historyKey = "user:" + userId + ":conv:" + conversationId;
            List<Message> history = contextCache.computeIfAbsent(
                    historyKey, k -> {
                        // 从数据库加载历史消息作为上下文
                        List<AiMessage> dbMessages = messageMapper.selectList(
                                new LambdaQueryWrapper<AiMessage>()
                                        .eq(AiMessage::getConversationId, conversationId)
                                        .orderByAsc(AiMessage::getCreateTime)
                                        .last("LIMIT " + MAX_HISTORY_SIZE));
                        List<Message> msgs = Collections.synchronizedList(new ArrayList<>());
                        for (AiMessage dbMsg : dbMessages) {
                            String role = "user".equals(dbMsg.getRole()) ? Role.USER.getValue() : Role.ASSISTANT.getValue();
                            msgs.add(Message.builder().role(role).content(dbMsg.getContent()).build());
                        }
                        return msgs;
                    });

            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(message)
                    .build();

            List<Message> messages = new ArrayList<>();
            synchronized (history) {
                history.add(userMsg);
                while (history.size() > MAX_HISTORY_SIZE) {
                    history.remove(0);
                }
                messages.add(Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content(systemPrompt)
                        .build());
                messages.addAll(history);
            }

            GenerationParam param = GenerationParam.builder()
                    .model(useModel)
                    .messages(messages)
                    .maxTokens(maxTokens)
                    .build();

            Generation gen = new Generation();
            GenerationResult result = gen.call(param);
            String reply = result.getOutput().getChoices().get(0).getMessage().getContent();

            synchronized (history) {
                history.add(Message.builder()
                        .role(Role.ASSISTANT.getValue())
                        .content(reply)
                        .build());
            }

            return reply;

        } catch (NoApiKeyException e) {
            log.error("AI API Key 未配置", e);
            return "抱歉，AI助手暂时不可用（API Key未配置）";
        } catch (ApiException e) {
            log.error("AI API 调用失败: {}", e.getMessage(), e);
            return getFallbackResponse(message);
        } catch (InputRequiredException e) {
            log.error("AI 输入参数错误", e);
            return "抱歉，消息格式有误，请重新输入";
        } catch (Exception e) {
            log.error("AI 助手异常: {}", e.getMessage(), e);
            return getFallbackResponse(message);
        }
    }

    /**
     * 创建新会话
     *
     * @param userId 用户ID
     * @return 新创建的会话对象
     */
    @Override
    public AiConversation createConversation(Long userId) {
        AiConversation conv = new AiConversation();
        conv.setUserId(userId);
        conv.setTitle("新对话");
        conversationMapper.insert(conv);
        return conv;
    }

    /**
     * 获取用户的会话列表（按更新时间倒序）
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    @Override
    public List<AiConversation> getConversations(Long userId) {
        return conversationMapper.selectList(
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, userId)
                        .orderByDesc(AiConversation::getUpdateTime));
    }

    /**
     * 删除会话及其所有消息
     *
     * @param userId         用户ID（校验归属）
     * @param conversationId 会话ID
     */
    @Override
    public void deleteConversation(Long userId, Long conversationId) {
        AiConversation conv = conversationMapper.selectById(conversationId);
        if (conv != null && conv.getUserId().equals(userId)) {
            // 删除会话下的所有消息
            messageMapper.delete(
                    new LambdaQueryWrapper<AiMessage>()
                            .eq(AiMessage::getConversationId, conversationId));
            conversationMapper.deleteById(conversationId);
            contextCache.remove("user:" + userId + ":conv:" + conversationId);
        }
    }

    /**
     * 获取指定会话的消息历史（从数据库读取）
     *
     * @param userId         用户ID
     * @param conversationId 会话ID
     * @return 消息列表，按时间正序
     */
    @Override
    public List<ChatMessage> getHistory(Long userId, Long conversationId) {
        // 从数据库读取消息历史
        List<AiMessage> dbMessages = messageMapper.selectList(
                new LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getConversationId, conversationId)
                        .orderByAsc(AiMessage::getCreateTime));
        List<ChatMessage> result = new ArrayList<>();
        for (AiMessage msg : dbMessages) {
            result.add(new ChatMessage(msg.getRole(), msg.getContent()));
        }
        return result;
    }

    /**
     * 降级响应（当AI服务不可用时）
     */
    private String getFallbackResponse(String message) {
        String msg = message.toLowerCase();

        if (msg.contains("秒杀") || msg.contains("抢购")) {
            return "🔥 秒杀活动说明：\n" +
                    "1. 秒杀商品在指定时间开售，数量有限先到先得\n" +
                    "2. 需要先完成验证码验证\n" +
                    "3. 每个用户每件商品限购1件\n" +
                    "4. 秒杀成功后请在15分钟内完成支付\n\n" +
                    "您可以访问「限时秒杀」页面查看当前活动商品！";
        }

        if (msg.contains("订单") || msg.contains("支付")) {
            return "📋 订单相关说明：\n" +
                    "1. 秒杀成功后会自动生成订单\n" +
                    "2. 请在15分钟内完成支付，超时订单将自动取消\n" +
                    "3. 支付完成后可在「我的订单」查看\n" +
                    "4. 如需退款，请在订单详情中申请\n\n" +
                    "更多问题请联系客服：400-888-8888";
        }

        if (msg.contains("优惠券") || msg.contains("折扣")) {
            return "🎫 优惠券使用说明：\n" +
                    "1. 在「优惠券」页面可领取可用优惠券\n" +
                    "2. 每个优惠券有使用条件和有效期\n" +
                    "3. 下单时可选择使用优惠券\n" +
                    "4. 每个订单限用一张优惠券\n\n" +
                    "快去领取优惠券享受更多优惠吧！";
        }

        if (msg.contains("购物车")) {
            return "🛒 购物车说明：\n" +
                    "1. 浏览商品时点击「加入购物车」\n" +
                    "2. 在购物车中可调整数量或删除商品\n" +
                    "3. 勾选商品后点击「去结算」\n" +
                    "4. 秒杀商品不经过购物车，直接下单\n\n" +
                    "小提示：库存紧张的商品建议尽快下单哦！";
        }

        if (msg.contains("配送") || msg.contains("快递") || msg.contains("物流")) {
            return "🚚 配送说明：\n" +
                    "1. 下单成功后48小时内发货\n" +
                    "2. 默认快递为顺丰/中通\n" +
                    "3. 可在订单详情中查看物流信息\n" +
                    "4. 偏远地区可能需要加收运费\n\n" +
                    "如有物流问题请联系客服处理！";
        }

        if (msg.contains("退") || msg.contains("换")) {
            return "🔄 退换货政策：\n" +
                    "1. 支持7天无理由退换货\n" +
                    "2. 商品需保持原包装完好\n" +
                    "3. 在订单详情中申请退款\n" +
                    "4. 审核通过后3-5个工作日内退款\n\n" +
                    "如有质量问题可联系客服优先处理！";
        }

        return "👋 你好！我是小秒，秒杀商城的购物助手！\n\n" +
                "我可以帮你：\n" +
                "• 了解秒杀活动规则\n" +
                "• 查询订单和物流\n" +
                "• 解答优惠券使用问题\n" +
                "• 处理退换货咨询\n" +
                "• 推荐热销商品\n\n" +
                "有什么可以帮您的吗？";
    }
}
