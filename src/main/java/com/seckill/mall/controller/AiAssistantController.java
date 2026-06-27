package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.entity.AiConversation;
import com.seckill.mall.security.LoginUser;
import com.seckill.mall.service.AiAssistantService;
import com.seckill.mall.service.AiAssistantService.ChatMessage;
import com.seckill.mall.service.AiAssistantService.ChatResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * AI购物助手控制器
 * <p>
 * 聊天接口支持匿名访问（使用IP作为会话标识），
 * 会话管理接口需要登录。
 * </p>
 */
@Api(tags = "AI购物助手")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    /**
     * 发送消息给AI助手（支持匿名访问）
     * <p>未登录用户使用IP地址作为会话标识</p>
     *
     * @param loginUser 当前登录用户（可为null，匿名用户使用IP标识）
     * @param body      请求体，包含 message（必填）、conversationId（可选）、model（可选）
     * @param request   HTTP请求（用于获取匿名用户IP）
     * @return AI回复内容和会话ID
     */
    @ApiOperation("发送消息")
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String message = body.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Result.error("消息不能为空");
        }

        if (message.length() > 500) {
            return Result.error("消息长度不能超过500字");
        }

        // 解析会话ID
        Long conversationId = null;
        String convIdStr = body.get("conversationId");
        if (convIdStr != null && !convIdStr.isEmpty()) {
            try {
                conversationId = Long.parseLong(convIdStr);
            } catch (NumberFormatException e) {
                return Result.error("会话ID格式错误");
            }
        }

        Long userId;
        if (loginUser != null) {
            userId = loginUser.getUserId();
        } else {
            String clientIp = getClientIp(request);
            userId = (long) clientIp.hashCode();
        }

        // 解析并校验模型参数
        String model = body.get("model");
        if (model != null && !model.isEmpty()) {
            List<String> allowedModels = List.of("qwen-turbo", "qwen-plus", "qwen-max");
            if (!allowedModels.contains(model)) {
                return Result.error("不支持的模型: " + model);
            }
        }

        ChatResult chatResult = aiAssistantService.chat(userId, message.trim(), conversationId, model);
        return Result.success(Map.of(
                "reply", chatResult.getReply(),
                "conversationId", chatResult.getConversationId()
        ));
    }

    /**
     * 获取可用模型列表
     *
     * @return 模型列表，每个模型包含 id、name、desc
     */
    @ApiOperation("获取可用模型列表")
    @GetMapping("/models")
    public Result<List<Map<String, String>>> getModels() {
        List<Map<String, String>> models = List.of(
                Map.of("id", "qwen-turbo", "name", "通义千问-极速", "desc", "响应最快，适合简单问答"),
                Map.of("id", "qwen-plus", "name", "通义千问-增强", "desc", "平衡性能与效果"),
                Map.of("id", "qwen-max", "name", "通义千问-旗舰", "desc", "最强能力，适合复杂任务")
        );
        return Result.success(models);
    }

    /**
     * 创建新会话（需要登录）
     *
     * @param loginUser 当前登录用户
     * @return 新会话的ID
     */
    @ApiOperation("创建新会话")
    @PostMapping("/conversation")
    public Result<Map<String, Object>> createConversation(@AuthenticationPrincipal LoginUser loginUser) {
        if (loginUser == null) {
            return Result.error(401, "请先登录");
        }
        AiConversation conv = aiAssistantService.createConversation(loginUser.getUserId());
        return Result.success(Map.of("id", conv.getId()));
    }

    /**
     * 获取会话列表（需要登录）
     *
     * @param loginUser 当前登录用户
     * @return 会话列表，按更新时间倒序
     */
    @ApiOperation("获取会话列表")
    @GetMapping("/conversations")
    public Result<List<AiConversation>> getConversations(@AuthenticationPrincipal LoginUser loginUser) {
        if (loginUser == null) {
            return Result.success(List.of());
        }
        return Result.success(aiAssistantService.getConversations(loginUser.getUserId()));
    }

    /**
     * 删除会话（需要登录）
     *
     * @param loginUser 当前登录用户
     * @param id        会话ID
     * @return 删除成功返回成功结果
     */
    @ApiOperation("删除会话")
    @DeleteMapping("/conversation/{id}")
    public Result<?> deleteConversation(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id) {
        if (loginUser == null) {
            return Result.error(401, "请先登录");
        }
        aiAssistantService.deleteConversation(loginUser.getUserId(), id);
        return Result.success();
    }

    /**
     * 获取指定会话的消息历史（需要登录）
     *
     * @param loginUser      当前登录用户
     * @param conversationId 会话ID
     * @return 消息列表，按时间正序
     */
    @ApiOperation("获取会话历史")
    @GetMapping("/history")
    public Result<List<ChatMessage>> getHistory(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam Long conversationId) {
        if (loginUser == null) {
            return Result.success(List.of());
        }
        return Result.success(aiAssistantService.getHistory(loginUser.getUserId(), conversationId));
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
