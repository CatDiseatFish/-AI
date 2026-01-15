package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.annotation.NoAuth;
import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.auth.PhoneLoginRequest;
import com.ym.ai_story_studio_server.dto.auth.PhoneLoginVO;
import com.ym.ai_story_studio_server.dto.auth.SendCodeRequest;
import com.ym.ai_story_studio_server.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 *
 * <p>处理用户登录、验证码发送等认证相关接口
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 发送验证码
     *
     * @param request 请求体（包含手机号）
     * @return 成功响应
     */
    @NoAuth
    @PostMapping("/phone/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        log.info("收到发送验证码请求: phone={}", maskPhone(request.phone()));
        authService.sendVerificationCode(request.phone());
        return Result.success();
    }

    /**
     * 手机号登录
     *
     * @param request 请求体（包含手机号、验证码和邀请码）
     * @return 登录成功响应（含Token和用户信息）
     */
    @NoAuth
    @PostMapping("/phone/login")
    public Result<PhoneLoginVO> phoneLogin(@Valid @RequestBody PhoneLoginRequest request) {
        log.info("收到手机号登录请求: phone={}, inviteCode={}", maskPhone(request.phone()), request.inviteCode());
        PhoneLoginVO vo = authService.phoneLogin(request.phone(), request.code(), request.inviteCode());
        return Result.success(vo);
    }

    /**
     * 微信登录（V2规划）
     *
     * @param request 请求体（包含微信授权码）
     * @return 登录成功响应（含Token和用户信息）
     */
    @NoAuth
    @PostMapping("/wechat/login")
    public Result<PhoneLoginVO> wechatLogin(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        log.info("收到微信登录请求: code={}", code);
        PhoneLoginVO vo = authService.wechatLogin(code);
        return Result.success(vo);
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
