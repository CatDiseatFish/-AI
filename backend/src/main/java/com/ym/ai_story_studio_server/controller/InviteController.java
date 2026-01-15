package com.ym.ai_story_studio_server.controller;

import com.ym.ai_story_studio_server.common.Result;
import com.ym.ai_story_studio_server.dto.invite.ApplyInviteRequest;
import com.ym.ai_story_studio_server.dto.invite.InviteCodeVO;
import com.ym.ai_story_studio_server.dto.invite.InviteRecordVO;
import com.ym.ai_story_studio_server.dto.invite.InviteStatsVO;
import com.ym.ai_story_studio_server.service.InviteService;
import com.ym.ai_story_studio_server.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 邀请码控制器
 *
 * <p>提供邀请码相关的API接口,包括:
 * <ul>
 *   <li>获取我的邀请码</li>
 *   <li>生成新邀请码</li>
 *   <li>使用邀请码</li>
 *   <li>查询邀请记录</li>
 *   <li>查询邀请统计</li>
 * </ul>
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/invite")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;

    /**
     * 获取我的邀请码
     *
     * <p>每个用户只能有一个有效的邀请码,如果不存在则自动生成
     *
     * @return 邀请码VO
     */
    @GetMapping("/my-code")
    public Result<InviteCodeVO> getMyInviteCode() {
        Long userId = UserContext.getUserId();
        log.info("获取邀请码: userId={}", userId);

        InviteCodeVO inviteCode = inviteService.getMyInviteCode(userId);
        return Result.success(inviteCode);
    }

    /**
     * 生成新的邀请码
     *
     * <p>会将用户的旧邀请码禁用,并生成新的邀请码
     *
     * @return 新生成的邀请码VO
     */
    @PostMapping("/generate-code")
    public Result<InviteCodeVO> generateInviteCode() {
        Long userId = UserContext.getUserId();
        log.info("生成新邀请码: userId={}", userId);

        InviteCodeVO inviteCode = inviteService.generateInviteCode(userId);
        return Result.success("邀请码生成成功", inviteCode);
    }

    /**
     * 使用邀请码
     *
     * <p>新用户使用邀请码后,邀请人和被邀请人都会获得积分奖励
     *
     * @param request 使用邀请码请求
     * @return 成功响应
     */
    @PostMapping("/apply")
    public Result<Void> applyInviteCode(@Valid @RequestBody ApplyInviteRequest request) {
        Long userId = UserContext.getUserId();
        log.info("使用邀请码: userId={}, code={}", userId, request.code());

        inviteService.applyInviteCode(userId, request.code());
        return Result.success("邀请码使用成功,积分已发放",null);
    }

    /**
     * 获取我的邀请记录
     *
     * <p>查询我作为邀请人邀请的所有用户记录
     *
     * @return 邀请记录列表
     */
    @GetMapping("/records")
    public Result<List<InviteRecordVO>> getMyInviteRecords() {
        Long userId = UserContext.getUserId();
        log.info("查询邀请记录: userId={}", userId);

        List<InviteRecordVO> records = inviteService.getMyInviteRecords(userId);
        return Result.success(records);
    }

    /**
     * 获取邀请统计
     *
     * <p>查询总邀请人数和总获得积分
     *
     * @return 邀请统计VO
     */
    @GetMapping("/stats")
    public Result<InviteStatsVO> getInviteStats() {
        Long userId = UserContext.getUserId();
        log.info("查询邀请统计: userId={}", userId);

        InviteStatsVO stats = inviteService.getInviteStats(userId);
        return Result.success(stats);
    }
}
