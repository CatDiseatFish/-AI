package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.invite.InviteCodeVO;
import com.ym.ai_story_studio_server.dto.invite.InviteRecordVO;
import com.ym.ai_story_studio_server.dto.invite.InviteStatsVO;

import java.util.List;

/**
 * 邀请码服务接口
 *
 * <p>提供邀请码生成、使用、查询等功能,支持积分返利机制
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface InviteService {

    /**
     * 获取我的邀请码
     *
     * <p>每个用户只能有一个有效的邀请码,如果不存在则自动生成
     *
     * @param userId 用户ID
     * @return 邀请码VO
     */
    InviteCodeVO getMyInviteCode(Long userId);

    /**
     * 生成新的邀请码
     *
     * <p>如果用户已有邀请码,会将旧码禁用并生成新码
     *
     * @param userId 用户ID
     * @return 新生成的邀请码VO
     */
    InviteCodeVO generateInviteCode(Long userId);

    /**
     * 使用邀请码
     *
     * <p>新用户使用邀请码后:
     * <ul>
     *   <li>验证邀请码有效性(未过期、未超限、已启用)</li>
     *   <li>验证用户未被邀请过</li>
     *   <li>给邀请人和被邀请人发放积分</li>
     *   <li>记录邀请关系</li>
     * </ul>
     *
     * @param userId 当前用户ID(被邀请人)
     * @param code 邀请码
     */
    void applyInviteCode(Long userId, String code);

    /**
     * 获取我的邀请记录
     *
     * @param userId 用户ID
     * @return 邀请记录列表
     */
    List<InviteRecordVO> getMyInviteRecords(Long userId);

    /**
     * 获取邀请统计
     *
     * @param userId 用户ID
     * @return 邀请统计VO
     */
    InviteStatsVO getInviteStats(Long userId);
}
