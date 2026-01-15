package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.dto.character.CharacterVO;
import com.ym.ai_story_studio_server.dto.character.CreateCharacterRequest;
import com.ym.ai_story_studio_server.dto.character.UpdateCharacterRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 角色库服务接口
 *
 * <p>提供全局角色库的CRUD功能（跨项目复用）
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
public interface CharacterLibraryService {

    /**
     * 获取角色库列表(支持搜索和筛选)
     *
     * @param userId 当前用户ID
     * @param categoryId 分类ID(可选)
     * @param keyword 搜索关键词(可选)
     * @return 角色VO列表
     */
    List<CharacterVO> getCharacterList(Long userId, Long categoryId, String keyword);

    /**
     * 创建角色
     *
     * @param userId 当前用户ID
     * @param request 创建角色请求
     * @return 角色VO
     */
    CharacterVO createCharacter(Long userId, CreateCharacterRequest request);

    /**
     * 更新角色
     *
     * @param userId 当前用户ID
     * @param characterId 角色ID
     * @param request 更新角色请求
     */
    void updateCharacter(Long userId, Long characterId, UpdateCharacterRequest request);

    /**
     * 删除角色(软删除)
     *
     * @param userId 当前用户ID
     * @param characterId 角色ID
     */
    void deleteCharacter(Long userId, Long characterId);

    /**
     * 上传角色缩略图
     *
     * @param userId 当前用户ID
     * @param characterId 角色ID
     * @param file 图片文件
     * @return 上传后的URL
     */
    String uploadThumbnail(Long userId, Long characterId, MultipartFile file);
}
