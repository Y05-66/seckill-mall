package com.seckill.mall.controller;

import com.seckill.mall.common.BusinessException;
import com.seckill.mall.common.Result;
import com.seckill.mall.dto.LoginRequest;
import com.seckill.mall.dto.RegisterRequest;
import com.seckill.mall.dto.UserDTO;
import com.seckill.mall.redis.RedisService;
import com.seckill.mall.redis.keyprefix.LoginKey;
import com.seckill.mall.redis.keyprefix.UserKey;
import com.seckill.mall.security.LoginUser;
import com.seckill.mall.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 用户控制器
 * <p>
 * 处理用户相关的HTTP请求，包括用户注册、登录和获取用户信息。
 * 基础路径：/user
 * </p>
 */
@Api(tags = "用户模块")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    /** 用户业务服务 */
    private final UserService userService;

    /** Redis服务，用于登录限流 */
    private final RedisService redisService;

    /** 每分钟最大登录尝试次数 */
    private static final int LOGIN_RATE_LIMIT = 5;

    /** 文件上传目录 */
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /** 文件访问基础URL */
    @Value("${file.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    /** 允许上传的图片扩展名白名单 */
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    /**
     * 用户注册接口
     * <p>
     * 通过@Valid注解自动校验注册请求参数（用户名3-20位、密码6-20位等），
     * 校验失败时由GlobalExceptionHandler捕获并返回参数错误信息。
     * </p>
     *
     * @param request 注册请求参数（用户名、密码、昵称等）
     * @return 注册成功返回成功结果
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    /**
     * 用户登录接口
     * <p>
     * 验证用户名密码，登录成功返回JWT Token及用户基本信息。
     * Token需在后续请求的Authorization头中携带：Bearer {token}
     * </p>
     *
     * @param request 登录请求参数（用户名、密码）
     * @return 包含token和用户信息的Map
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        // 登录限流：基于IP地址，每分钟最多5次失败尝试
        // 先检查当前计数（不增加），如果已超限则拒绝
        String clientIp = getClientIp(httpRequest);
        Long loginAttempts = redisService.get(LoginKey.RATE_LIMIT, clientIp);
        if (loginAttempts != null && loginAttempts >= LOGIN_RATE_LIMIT) {
            throw new BusinessException(429, "登录尝试次数过多，请1分钟后再试");
        }

        try {
            Map<String, Object> data = userService.login(request);
            // 登录成功，清除限流计数
            redisService.delete(LoginKey.RATE_LIMIT, clientIp);
            return Result.success(data);
        } catch (Exception e) {
            // 登录失败，增加限流计数（先增加再检查，确保第5次失败后第6次被拦截）
            Long currentCount = redisService.incrWithExpire(LoginKey.RATE_LIMIT, clientIp, 60);
            if (currentCount >= LOGIN_RATE_LIMIT) {
                throw new BusinessException(429, "登录尝试次数过多，请1分钟后再试");
            }
            throw e;
        }
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
        // 多级代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取当前登录用户信息接口
     * <p>
     * 通过@AuthenticationPrincipal注解从Spring Security上下文中获取当前登录用户，
     * 再根据用户ID查询完整的用户信息。
     * </p>
     *
     * @param loginUser 当前登录用户（由Spring Security自动注入）
     * @return 用户信息DTO（不包含密码）
     */
    /**
     * 退出登录（将Token加入Redis黑名单）
     *
     * @param loginUser 当前登录用户
     * @return 操作结果
     */
    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result<?> logout(@AuthenticationPrincipal LoginUser loginUser,
                            javax.servlet.http.HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            // 将Token加入黑名单，25小时后自动过期（比Token有效期多1小时）
            redisService.set(UserKey.TOKEN_BLACKLIST, token, "1");
        }
        return Result.success();
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/info")
    public Result<UserDTO> getUserInfo(@AuthenticationPrincipal LoginUser loginUser) {
        UserDTO userDTO = userService.getUserInfo(loginUser.getUserId());
        return Result.success(userDTO);
    }

    /**
     * 修改个人信息
     */
    @ApiOperation("修改个人信息")
    @PutMapping("/info")
    public Result<?> updateProfile(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Map<String, String> body) {
        userService.updateProfile(
                loginUser.getUserId(),
                body.get("nickname"),
                body.get("phone"),
                body.get("email"),
                body.get("avatar"));
        return Result.success();
    }

    /**
     * 修改密码
     */
    @ApiOperation("修改密码")
    @PutMapping("/password")
    public Result<?> changePassword(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Map<String, String> body) {
        userService.changePassword(
                loginUser.getUserId(),
                body.get("oldPassword"),
                body.get("newPassword"));
        return Result.success();
    }

    /**
     * 重置密码（忘记密码时使用，需提供用户名和新密码）
     * <p>简化版：直接根据用户名重置密码。生产环境应增加邮箱/手机验证码校验。</p>
     *
     * @param body 包含 username 和 newPassword
     * @return 操作结果
     */
    @ApiOperation("重置密码")
    @PostMapping("/reset-password")
    public Result<?> resetPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String newPassword = body.get("newPassword");
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码至少6位");
        }
        userService.resetPassword(username.trim(), newPassword);
        return Result.success();
    }

    /**
     * 上传头像
     */
    @ApiOperation("上传头像")
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam("file") MultipartFile file) throws IOException {
        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }
        // 校验文件大小（最大5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("图片大小不能超过5MB");
        }

        // 创建上传目录
        File dir = new File(uploadDir + "/avatar");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成唯一文件名并验证扩展名
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase() : ".jpg";
        // 验证扩展名白名单（防止上传恶意文件）
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(ext)) {
            return Result.error("只支持 JPG、PNG、GIF、WebP 格式的图片");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        // 保存文件
        File dest = new File(dir, fileName);
        file.transferTo(dest);

        // 返回访问URL
        String url = baseUrl + "/avatar/" + fileName;
        Map<String, String> data = new HashMap<>();
        data.put("url", url);

        // 自动更新用户头像
        userService.updateProfile(loginUser.getUserId(), null, null, null, url);

        return Result.success(data);
    }
}
