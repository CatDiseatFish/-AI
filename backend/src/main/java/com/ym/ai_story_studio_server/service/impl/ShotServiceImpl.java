package com.ym.ai_story_studio_server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ym.ai_story_studio_server.client.VectorEngineClient;
import com.ym.ai_story_studio_server.config.AiProperties;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.dto.shot.*;
import com.ym.ai_story_studio_server.entity.*;
import com.ym.ai_story_studio_server.mapper.*;
import com.ym.ai_story_studio_server.service.ShotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分镜服务实现类
 *
 * <p>实现分镜的CRUD、排序、绑定管理等业务逻辑
 *
 * {{CODE-Cycle-Integration:
 *   Task_ID: [#Backend-Fix-ShotNo-Duplicate-TwoPhase]
 *   Timestamp: 2026-01-03T15:00:00+08:00
 *   Phase: [D-Develop]
 *   Context-Analysis: "Fixed duplicate key error in reorderAllActiveShots by implementing two-phase update strategy: Phase 1 - Set all shot_no to negative temp values (-shotId) to avoid conflicts with deleted records. Phase 2 - Set shot_no to final sequential values (1,2,3...). This ensures no unique constraint violations during reordering."
 *   Principle_Applied: "Advanced-Debugging-Protocol, Root-Cause-Analysis, Two-Phase-Commit-Pattern"
 * }}
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShotServiceImpl implements ShotService {

    private final StoryboardShotMapper shotMapper;
    private final ShotBindingMapper bindingMapper;
    private final ProjectMapper projectMapper;
    private final ProjectCharacterMapper projectCharacterMapper;
    private final ProjectSceneMapper projectSceneMapper;
    private final ProjectPropMapper projectPropMapper;
    private final PropLibraryMapper propLibraryMapper;
    private final CharacterLibraryMapper characterLibraryMapper;
    private final SceneLibraryMapper sceneLibraryMapper;
    private final AssetMapper assetMapper;
    private final AssetVersionMapper assetVersionMapper;
    private final VectorEngineClient vectorEngineClient;
    private final AiProperties aiProperties;

    @Override
    public List<ShotVO> getShotList(Long userId, Long projectId) {
        log.info("查询分镜列表: userId={}, projectId={}", userId, projectId);

        // 1. 验证项目存在且属于当前用户
        Project project = validateProjectOwnership(userId, projectId);

        // 2. 查询项目下所有未删除的分镜(按shot_no升序)
        LambdaQueryWrapper<StoryboardShot> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StoryboardShot::getProjectId, projectId)
                .isNull(StoryboardShot::getDeletedAt)
                .orderByAsc(StoryboardShot::getShotNo);

        List<StoryboardShot> shots = shotMapper.selectList(queryWrapper);
        log.info("查询到{}条分镜记录", shots.size());

        if (shots.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 批量查询所有分镜的绑定关系
        List<Long> shotIds = shots.stream()
                .map(StoryboardShot::getId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<ShotBinding> bindingWrapper = new LambdaQueryWrapper<>();
        bindingWrapper.in(ShotBinding::getShotId, shotIds);
        List<ShotBinding> bindings = bindingMapper.selectList(bindingWrapper);
        log.info("查询到{}条绑定关系", bindings.size());
        
        // 打印每个分镜的绑定关系数量
        for (Long shotId : shotIds) {
            long count = bindings.stream().filter(b -> b.getShotId().equals(shotId)).count();
            log.info("分镜ID={} 的绑定关系数量: {}", shotId, count);
        }

        // 4. 按shotId分组绑定关系
        Map<Long, List<ShotBinding>> bindingMap = bindings.stream()
                .collect(Collectors.groupingBy(ShotBinding::getShotId));

        // 5. 转换为VO列表
        List<ShotVO> voList = shots.stream()
                .map(shot -> convertToVO(shot, bindingMap.get(shot.getId())))
                .collect(Collectors.toList());

        log.info("分镜列表转换完成,返回{}条记录", voList.size());
        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShotVO createShot(Long userId, Long projectId, CreateShotRequest request) {
        log.info("创建分镜: userId={}, projectId={}, scriptText={}", userId, projectId, request.scriptText());

        // 1. 验证项目存在且属于当前用户
        Project project = validateProjectOwnership(userId, projectId);

        // 2. 获取当前项目最大的shot_no值 (包括已删除的记录,防止唯一约束冲突)
        LambdaQueryWrapper<StoryboardShot> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StoryboardShot::getProjectId, projectId)
                .orderByDesc(StoryboardShot::getShotNo)
                .last("LIMIT 1");

        StoryboardShot maxShot = shotMapper.selectOne(queryWrapper);
        Integer tempShotNo = (maxShot == null) ? 1 : maxShot.getShotNo() + 1;

        log.info("分配临时分镜序号: {} (避免唯一约束冲突)", tempShotNo);

        // 3. 创建分镜实体
        StoryboardShot shot = new StoryboardShot();
        shot.setProjectId(projectId);
        shot.setShotNo(tempShotNo);
        shot.setScriptText(request.scriptText());

        // 4. 保存到数据库
        int result = shotMapper.insert(shot);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "分镜创建失败");
        }

        log.info("分镜创建成功: shotId={}, tempShotNo={}", shot.getId(), shot.getShotNo());

        // 5. 重新整理所有未删除分镜的序号,确保连续性 (1,2,3...)
        reorderAllActiveShots(projectId);

        // 6. 重新查询分镜(获取重排后的正确 shot_no)
        StoryboardShot refreshedShot = shotMapper.selectById(shot.getId());

        log.info("分镜序号已重排: shotId={}, finalShotNo={}", refreshedShot.getId(), refreshedShot.getShotNo());

        // 7. 返回ShotVO(无绑定关系,资产状态为空)
        return new ShotVO(
                refreshedShot.getId(),
                refreshedShot.getShotNo(),
                refreshedShot.getScriptText(),
                new ArrayList<>(),
                null,
                new ArrayList<>(),
                createEmptyAssetStatus(),
                createEmptyAssetStatus(),
                refreshedShot.getCreatedAt(),
                refreshedShot.getUpdatedAt()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShot(Long userId, Long projectId, Long shotId, UpdateShotRequest request) {
        log.info("更新分镜: userId={}, projectId={}, shotId={}, newScriptText={}",
                userId, projectId, shotId, request.scriptText());

        // 1. 查询分镜是否存在且未删除
        StoryboardShot shot = shotMapper.selectById(shotId);
        if (shot == null || shot.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "分镜不存在");
        }

        // 2. 验证分镜属于指定项目
        if (!shot.getProjectId().equals(projectId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED, "无权限访问该分镜");
        }

        // 3. 验证项目属于当前用户
        validateProjectOwnership(userId, projectId);

        // 4. 更新剧本文本
        shot.setScriptText(request.scriptText());
        int result = shotMapper.updateById(shot);

        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "分镜更新失败");
        }

        log.info("分镜更新成功: shotId={}", shotId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShot(Long userId, Long projectId, Long shotId) {
        log.info("删除分镜: userId={}, projectId={}, shotId={}", userId, projectId, shotId);

        // 1. 查询分镜是否存在且未删除
        StoryboardShot shot = shotMapper.selectById(shotId);
        if (shot == null || shot.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "分镜不存在");
        }

        // 2. 验证分镜属于指定项目
        if (!shot.getProjectId().equals(projectId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED, "无权限访问该分镜");
        }

        // 3. 验证项目属于当前用户
        validateProjectOwnership(userId, projectId);

        // 4. 软删除分镜
        shot.setDeletedAt(LocalDateTime.now());
        int result = shotMapper.updateById(shot);

        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "分镜删除失败");
        }

        // 5. 删除相关绑定记录(物理删除)
        LambdaQueryWrapper<ShotBinding> bindingWrapper = new LambdaQueryWrapper<>();
        bindingWrapper.eq(ShotBinding::getShotId, shotId);
        int bindingResult = bindingMapper.delete(bindingWrapper);

        log.info("分镜删除成功: shotId={}, 删除了{}条绑定记录", shotId, bindingResult);

        // 6. 重新整理所有未删除分镜的序号,确保连续性 (1,2,3...)
        reorderAllActiveShots(projectId);
        log.info("分镜序号已重排,删除后保持连续性");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reorderShots(Long userId, Long projectId, ReorderShotsRequest request) {
        log.info("调整分镜顺序: userId={}, projectId={}, shotIds={}",
                userId, projectId, request.shotIds());

        // 1. 验证项目存在且属于当前用户
        validateProjectOwnership(userId, projectId);

        List<Long> shotIds = request.shotIds();
        if (shotIds.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "分镜ID列表不能为空");
        }

        // 2. 批量查询所有待调整的分镜
        List<StoryboardShot> shots = shotMapper.selectBatchIds(shotIds);

        // 3. 验证所有分镜都存在且未删除
        if (shots.size() != shotIds.size()) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "部分分镜不存在");
        }

        // 4. 验证所有分镜都属于该项目
        boolean allBelongToProject = shots.stream()
                .allMatch(shot -> shot.getProjectId().equals(projectId) && shot.getDeletedAt() == null);

        if (!allBelongToProject) {
            throw new BusinessException(ResultCode.ACCESS_DENIED, "部分分镜不属于该项目");
        }

        // 5. 采用两阶段更新策略避免唯一键冲突
        Map<Long, StoryboardShot> shotMap = shots.stream()
                .collect(Collectors.toMap(StoryboardShot::getId, shot -> shot));

        // 计算安全的临时序号基数（避免与现有序号冲突）
        int maxShotNo = shots.stream()
                .mapToInt(StoryboardShot::getShotNo)
                .max()
                .orElse(0);
        int tempBase = maxShotNo + 1000;

        // 第一阶段：将所有shotNo设置为临时值
        for (int i = 0; i < shotIds.size(); i++) {
            Long shotId = shotIds.get(i);
            StoryboardShot shot = shotMap.get(shotId);
            shot.setShotNo(tempBase + i);
            shotMapper.updateById(shot);
        }

        // 第二阶段：将所有shotNo设置为目标值
        for (int i = 0; i < shotIds.size(); i++) {
            Long shotId = shotIds.get(i);
            StoryboardShot shot = shotMap.get(shotId);
            shot.setShotNo(i + 1);
            shotMapper.updateById(shot);
        }

        log.info("分镜顺序调整成功,共调整{}条分镜", shotIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBinding(Long userId, Long projectId, Long shotId, CreateBindingRequest request) {
        log.info("创建绑定关系: userId={}, projectId={}, shotId={}, bindType={}, bindId={}",
                userId, projectId, shotId, request.bindType(), request.bindId());

        // 1. 查询分镜是否存在且未删除
        StoryboardShot shot = shotMapper.selectById(shotId);
        if (shot == null || shot.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "分镜不存在");
        }

        // 2. 验证分镜属于指定项目
        if (!shot.getProjectId().equals(projectId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED, "无权限访问该分镜");
        }

        // 3. 验证项目属于当前用户
        validateProjectOwnership(userId, projectId);

        // 4. 根据bindType验证绑定对象是否存在
        String bindType = request.bindType();
        Long bindId = request.bindId();

        if ("PCHAR".equals(bindType)) {
            // 验证项目角色是否存在
            ProjectCharacter character = projectCharacterMapper.selectById(bindId);
            if (character == null ) {
                throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "项目角色不存在");
            }
            if (!character.getProjectId().equals(projectId)) {
                throw new BusinessException(ResultCode.ACCESS_DENIED, "该角色不属于该项目");
            }
        } else if ("PSCENE".equals(bindType)) {
            // 验证项目场景是否存在
            ProjectScene scene = projectSceneMapper.selectById(bindId);
            if (scene == null ) {
                throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "项目场景不存在");
            }
            if (!scene.getProjectId().equals(projectId)) {
                throw new BusinessException(ResultCode.ACCESS_DENIED, "该项目场景不属于该项目");
            }
        } else if ("PPROP".equals(bindType)) {
            // 验证项目道具是否存在
            ProjectProp prop = projectPropMapper.selectById(bindId);
            if (prop == null) {
                throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "项目道具不存在");
            }
            if (!prop.getProjectId().equals(projectId)) {
                throw new BusinessException(ResultCode.ACCESS_DENIED, "该道具不属于该项目");
            }
        }

        // 5. 检查是否已存在相同的绑定(防止重复)
        LambdaQueryWrapper<ShotBinding> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(ShotBinding::getShotId, shotId)
                .eq(ShotBinding::getBindType, bindType)
                .eq(ShotBinding::getBindId, bindId);

        long count = bindingMapper.selectCount(checkWrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "该绑定关系已存在");
        }

        // 6. 创建绑定记录
        ShotBinding binding = new ShotBinding();
        binding.setShotId(shotId);
        binding.setBindType(bindType);
        binding.setBindId(bindId);

        int result = bindingMapper.insert(binding);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "绑定创建失败");
        }

        log.info("绑定关系创建成功: bindingId={}", binding.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBinding(Long userId, Long projectId, Long shotId, Long bindingId) {
        log.info("删除绑定关系: userId={}, projectId={}, shotId={}, bindingId={}",
                userId, projectId, shotId, bindingId);

        // 1. 查询绑定记录是否存在
        ShotBinding binding = bindingMapper.selectById(bindingId);
        if (binding == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "绑定记录不存在");
        }

        // 2. 验证绑定记录属于指定分镜
        if (!binding.getShotId().equals(shotId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED, "该绑定记录不属于该分镜");
        }

        // 3. 查询分镜是否存在且未删除
        StoryboardShot shot = shotMapper.selectById(shotId);
        if (shot == null || shot.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "分镜不存在");
        }

        // 4. 验证分镜属于指定项目
        if (!shot.getProjectId().equals(projectId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED, "无权限访问该分镜");
        }

        // 5. 验证项目属于当前用户
        validateProjectOwnership(userId, projectId);

        // 6. 物理删除绑定记录
        int result = bindingMapper.deleteById(bindingId);
        if (result <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "绑定删除失败");
        }

        log.info("绑定关系删除成功: bindingId={}", bindingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ShotVO> parseScriptAndCreateShots(Long userId, Long projectId, ParseScriptRequest request) {
        log.info("AI解析剧本: userId={}, projectId={}, scriptLength={}", userId, projectId, request.fullScript().length());

        // 1. 验证项目存在且属于当前用户
        validateProjectOwnership(userId, projectId);

        // 2. 调用AI解析剧本，拆分成多条
        List<String> scriptSegments = parseScriptToSegments(request.fullScript());
        log.info("AI解析完成，拆分为{}条分镜", scriptSegments.size());

        if (scriptSegments.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "AI解析结果为空，请检查剧本内容");
        }

        // 3. 批量创建分镜
        List<ShotVO> createdShots = new ArrayList<>();
        for (String segment : scriptSegments) {
            if (segment.isBlank()) {
                continue;
            }
            ShotVO shot = createShot(userId, projectId, new CreateShotRequest(segment.trim()));
            createdShots.add(shot);
        }

        log.info("批量创建分镜完成: count={}", createdShots.size());
        return createdShots;
    }

    /**
     * 解析完整剧本，拆分成分镜段落
     *
     * @param fullScript 完整剧本文本
     * @return 分镜段落列表
     */
    private List<String> parseScriptToSegments(String fullScript) {
        String systemPrompt = """
            一、你是一个专业的剧本大师，请将以下文案转换为剧本格式
            
            要求：
            1.将内容分镜化，每个镜头的字数严格控制在99字以内
            2.保持文案所有情节，角色对话全部保留，不得增删改一字
            3.严格按照剧本格式输出
            
            格式规范:
            -场景编号格式:场x-y，如"场1-1"代表第1集第一个场景
            -时间信息:夜/日，清晰表明是晚上还是白天
            -地点类型:外/内，明确是室外还是室内
            -具体地点:如别墅、酒店、广场等
            -使用括号标注镜头和动作
            -不要使用**等非剧本格式的符号
            -允许使用的景别：中景、近景、特写
            -中景：用于展示两人互动、相对位置或上半身动作
            -近景：用于常规对话、面部表情捕捉
            -特写：用于强调手部动作、物品细节或眼神微表情
            
            输出格式要求：
            1. 直接输出每个分镜场景
            2. 每个分镜之间用 "---" 分隔
            3. 不要在开头汇总角色或场景
            
            示例格式:
            场1-1 日 内 咖啡厅
            出场人物:林辰、林清雪
            （中景）林辰和林清雪面对面坐着，桌上放着咖啡，叶辰说：“我想和你谈谈我们的未来。”
            （近景）叶辰表情严肃，镜头切至林清雪抬头看他，林清雪说：“什么意思？”
            （特写）叶辰的手从包里拿出户口本放在桌上，叶辰说：“我想和你结婚。”
            ---
            场1-2 日 内 咖啡厅
            出场人物:林辰、林清雪
            （近景）林清雪震惊地看着户口本，林清雪说：“可是...我爸不同意。”
            （特写）叶辰紧紧握住林清雪的手，叶辰说：“我会说服他的，相信我。”
            （中景）林清雪流泪，随后坚定地点头。林清雪说：“我相信你。”
            ---
            
            直接输出分镜内容，不要其他说明。
            """;

        String prompt = systemPrompt + "\n\n用户文案：\n" + fullScript;

        // 调用AI生成
        AiProperties.Text textConfig = aiProperties.getText();
        VectorEngineClient.TextApiResponse response = vectorEngineClient.generateText(
                prompt,
                textConfig.getModel(),
                textConfig.getMaxTokens(),
                0.3,  // 使用较低的温度以获得更稳定的输出
                0.9
        );

        // 提取生成的文本
        String generatedText = response.choices() == null || response.choices().isEmpty()
                ? ""
                : response.choices().get(0).message().content();

        log.debug("AI解析结果: {}", generatedText);

        // 按分隔符拆分
        return Arrays.stream(generatedText.split("---"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 验证项目存在且属于当前用户
     *
     * @param userId 用户ID
     * @param projectId 项目ID
     * @return 项目实体
     */
    private Project validateProjectOwnership(Long userId, Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null || project.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "项目不存在");
        }
        if (!project.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED, "无权限访问该项目");
        }
        return project;
    }

    /**
     * 重新整理项目所有未删除分镜的序号,确保连续性
     *
     * <p>将所有未删除分镜的 shot_no 重新排列为 1, 2, 3...,避免序号跳号
     * <p>使用两阶段更新策略避免与已删除记录的唯一约束冲突
     *
     * @param projectId 项目ID
     */
    private void reorderAllActiveShots(Long projectId) {
        // 1. 物理删除所有已软删除的分镜(避免唯一约束冲突)
        LambdaQueryWrapper<StoryboardShot> deleteQuery = new LambdaQueryWrapper<>();
        deleteQuery.eq(StoryboardShot::getProjectId, projectId)
                .isNotNull(StoryboardShot::getDeletedAt);
        int deletedCount = shotMapper.delete(deleteQuery);
        if (deletedCount > 0) {
            log.debug("物理删除了{}条已软删除的分镜记录", deletedCount);
        }

        // 2. 查询所有未删除的分镜,按当前 shot_no 升序排列
        LambdaQueryWrapper<StoryboardShot> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StoryboardShot::getProjectId, projectId)
                .isNull(StoryboardShot::getDeletedAt)
                .orderByAsc(StoryboardShot::getShotNo);

        List<StoryboardShot> activeShots = shotMapper.selectList(queryWrapper);

        if (activeShots.isEmpty()) {
            return;
        }

        // 3. 查询当前项目的最大 shot_no,用于计算安全的临时值
        LambdaQueryWrapper<StoryboardShot> maxQuery = new LambdaQueryWrapper<>();
        maxQuery.eq(StoryboardShot::getProjectId, projectId)
                .orderByDesc(StoryboardShot::getShotNo)
                .last("LIMIT 1");
        StoryboardShot maxShot = shotMapper.selectOne(maxQuery);
        int tempBase = (maxShot == null ? 0 : maxShot.getShotNo()) + 1000;

        // 4. 第一阶段：将所有分镜的 shot_no 设置为安全的临时值(maxShotNo+1000+索引)
        for (int i = 0; i < activeShots.size(); i++) {
            StoryboardShot shot = activeShots.get(i);
            shot.setShotNo(tempBase + i);
            shotMapper.updateById(shot);
        }

        // 5. 第二阶段：将所有分镜的 shot_no 设置为目标连续值 (1, 2, 3...)
        // 此时已删除记录已被物理删除,不会有唯一约束冲突
        for (int i = 0; i < activeShots.size(); i++) {
            StoryboardShot shot = activeShots.get(i);
            shot.setShotNo(i + 1);
            shotMapper.updateById(shot);
        }

        log.debug("重新整理分镜序号完成: projectId={}, 调整了{}条分镜", projectId, activeShots.size());
    }

    /**
     * 将分镜实体转换为VO
     *
     * @param shot 分镜实体
     * @param bindings 绑定关系列表(可为null)
     * @return 分镜VO
     */
    private ShotVO convertToVO(StoryboardShot shot, List<ShotBinding> bindings) {
        List<BoundCharacterVO> characters = new ArrayList<>();
        BoundSceneVO scene = null;
        List<BoundPropVO> props = new ArrayList<>();

        if (bindings != null && !bindings.isEmpty()) {
            // 分离角色绑定、场景绑定和道具绑定
            List<Long> characterIds = new ArrayList<>();
            Long sceneId = null;
            Long sceneBindingId = null;
            List<Long> propIds = new ArrayList<>();

            for (ShotBinding binding : bindings) {
                if ("PCHAR".equals(binding.getBindType())) {
                    characterIds.add(binding.getBindId());
                } else if ("PSCENE".equals(binding.getBindType())) {
                    sceneId = binding.getBindId();
                    sceneBindingId = binding.getId();
                } else if ("PPROP".equals(binding.getBindType())) {
                    propIds.add(binding.getBindId());
                }
            }

            // 查询角色信息
            if (!characterIds.isEmpty()) {
                List<ProjectCharacter> projectCharacters = projectCharacterMapper.selectBatchIds(characterIds);
                Map<Long, ProjectCharacter> charMap = projectCharacters.stream()
                        .collect(Collectors.toMap(ProjectCharacter::getId, c -> c));

                // 获取所有关联的角色库ID，批量查询缩略图
                List<Long> libraryCharacterIds = projectCharacters.stream()
                        .map(ProjectCharacter::getLibraryCharacterId)
                        .filter(id -> id != null)
                        .distinct()
                        .collect(Collectors.toList());

                Map<Long, String> thumbnailMap = new java.util.HashMap<>();
                if (!libraryCharacterIds.isEmpty()) {
                    List<CharacterLibrary> libraryCharacters = characterLibraryMapper.selectBatchIds(libraryCharacterIds);
                    thumbnailMap = libraryCharacters.stream()
                            .filter(c -> c.getThumbnailUrl() != null)
                            .collect(Collectors.toMap(CharacterLibrary::getId, CharacterLibrary::getThumbnailUrl));
                }

                for (ShotBinding binding : bindings) {
                    if ("PCHAR".equals(binding.getBindType())) {
                        ProjectCharacter character = charMap.get(binding.getBindId());
                        if (character != null) {
                            String thumbnailUrl = null;
                            
                            System.out.println("========== 开始处理角色 ==========");
                            System.out.println("角色名称: " + character.getDisplayName());
                            System.out.println("角色ID: " + character.getId());
                            System.out.println("库角色ID: " + character.getLibraryCharacterId());
                            
                            // 优先使用库的缩略图
                            if (character.getLibraryCharacterId() != null) {
                                thumbnailUrl = thumbnailMap.get(character.getLibraryCharacterId());
                                System.out.println("库缩略图URL: " + thumbnailUrl);
                                log.debug("角色[{}] 库ID:{}, 库缩略图:{}", character.getDisplayName(), character.getLibraryCharacterId(), thumbnailUrl);
                            }
                            
                            // 如果没有库缩略图，查询角色的资产版本作为缩略图
                            if (thumbnailUrl == null) {
                                System.out.println("库缩略图为空，查询角色资产...");
                                AssetStatusVO assetStatus = getAssetStatus(character.getId(), "PCHAR", "IMAGE");
                                System.out.println("资产ID: " + (assetStatus != null ? assetStatus.assetId() : "null"));
                                System.out.println("资产URL: " + (assetStatus != null ? assetStatus.currentUrl() : "null"));
                                log.debug("角色[{}] 资产状态: assetId={}, url={}", character.getDisplayName(), 
                                    assetStatus != null ? assetStatus.assetId() : null,
                                    assetStatus != null ? assetStatus.currentUrl() : null);
                                if (assetStatus != null && assetStatus.currentUrl() != null) {
                                    thumbnailUrl = assetStatus.currentUrl();
                                }
                            }
                            
                            System.out.println("最终thumbnailUrl: " + thumbnailUrl);
                            System.out.println("==========================================\n");
                            log.info("角色[{}] 最终thumbnailUrl: {}", character.getDisplayName(), thumbnailUrl);
                            characters.add(new BoundCharacterVO(
                                    binding.getId(),
                                    character.getId(),
                                    character.getDisplayName(),
                                    thumbnailUrl
                            ));
                        }
                    }
                }
            }

            // 查询场景信息
            if (sceneId != null) {
                ProjectScene projectScene = projectSceneMapper.selectById(sceneId);
                if (projectScene != null) {
                    String sceneThumbnailUrl = null;
                    
                    System.out.println("========== 开始处理场景 ==========");
                    System.out.println("场景名称: " + projectScene.getDisplayName());
                    System.out.println("场景ID: " + projectScene.getId());
                    System.out.println("库场景ID: " + projectScene.getLibrarySceneId());
                    
                    // 优先使用库的缩略图
                    if (projectScene.getLibrarySceneId() != null) {
                        SceneLibrary sceneLibrary = sceneLibraryMapper.selectById(projectScene.getLibrarySceneId());
                        if (sceneLibrary != null) {
                            sceneThumbnailUrl = sceneLibrary.getThumbnailUrl();
                            System.out.println("库缩略图URL: " + sceneThumbnailUrl);
                            log.debug("场景[{}] 库ID:{}, 库缩略图:{}", projectScene.getDisplayName(), projectScene.getLibrarySceneId(), sceneThumbnailUrl);
                        }
                    }
                    
                    // 如果没有库缩略图，查询场景的资产版本作为缩略图
                    if (sceneThumbnailUrl == null) {
                        System.out.println("库缩略图为空，查询场景资产...");
                        AssetStatusVO assetStatus = getAssetStatus(projectScene.getId(), "PSCENE", "IMAGE");
                        System.out.println("资产ID: " + (assetStatus != null ? assetStatus.assetId() : "null"));
                        System.out.println("资产URL: " + (assetStatus != null ? assetStatus.currentUrl() : "null"));
                        log.debug("场景[{}] 资产状态: assetId={}, url={}", projectScene.getDisplayName(), 
                            assetStatus != null ? assetStatus.assetId() : null,
                            assetStatus != null ? assetStatus.currentUrl() : null);
                        if (assetStatus != null && assetStatus.currentUrl() != null) {
                            sceneThumbnailUrl = assetStatus.currentUrl();
                        }
                    }
                    
                    System.out.println("最终thumbnailUrl: " + sceneThumbnailUrl);
                    System.out.println("==========================================\n");
                    log.info("场景[{}] 最终thumbnailUrl: {}", projectScene.getDisplayName(), sceneThumbnailUrl);
                    scene = new BoundSceneVO(
                            sceneBindingId,
                            projectScene.getId(),
                            projectScene.getDisplayName(),
                            sceneThumbnailUrl
                    );
                }
            }

            // 查询道具信息
            if (!propIds.isEmpty()) {
                List<ProjectProp> projectProps = projectPropMapper.selectBatchIds(propIds);
                Map<Long, ProjectProp> propMap = projectProps.stream()
                        .collect(Collectors.toMap(ProjectProp::getId, p -> p));

                // 获取所有关联的道具库ID，批量查询缩略图
                List<Long> libraryPropIds = projectProps.stream()
                        .map(ProjectProp::getLibraryPropId)
                        .filter(id -> id != null)
                        .distinct()
                        .collect(Collectors.toList());

                Map<Long, String> propThumbnailMap = new java.util.HashMap<>();
                if (!libraryPropIds.isEmpty()) {
                    List<PropLibrary> libraryProps = propLibraryMapper.selectBatchIds(libraryPropIds);
                    propThumbnailMap = libraryProps.stream()
                            .filter(p -> p.getThumbnailUrl() != null)
                            .collect(Collectors.toMap(PropLibrary::getId, PropLibrary::getThumbnailUrl));
                }

                for (ShotBinding binding : bindings) {
                    if ("PPROP".equals(binding.getBindType())) {
                        ProjectProp prop = propMap.get(binding.getBindId());
                        if (prop != null) {
                            String thumbnailUrl = null;
                            
                            System.out.println("========== 开始处理道具 ==========");
                            System.out.println("道具名称: " + prop.getDisplayName());
                            System.out.println("道具ID: " + prop.getId());
                            System.out.println("库道具ID: " + prop.getLibraryPropId());
                            
                            // 优先使用库的缩略图
                            if (prop.getLibraryPropId() != null) {
                                thumbnailUrl = propThumbnailMap.get(prop.getLibraryPropId());
                                System.out.println("库缩略图URL: " + thumbnailUrl);
                                log.debug("道具[{}] 库ID:{}, 库缩略图:{}", prop.getDisplayName(), prop.getLibraryPropId(), thumbnailUrl);
                            }
                            
                            // 如果没有库缩略图，查询道具的资产版本作为缩略图
                            if (thumbnailUrl == null) {
                                System.out.println("库缩略图为空，查询道具资产...");
                                AssetStatusVO assetStatus = getAssetStatus(prop.getId(), "PPROP", "IMAGE");
                                System.out.println("资产ID: " + (assetStatus != null ? assetStatus.assetId() : "null"));
                                System.out.println("资产URL: " + (assetStatus != null ? assetStatus.currentUrl() : "null"));
                                log.debug("道具[{}] 资产状态: assetId={}, url={}", prop.getDisplayName(), 
                                    assetStatus != null ? assetStatus.assetId() : null,
                                    assetStatus != null ? assetStatus.currentUrl() : null);
                                if (assetStatus != null && assetStatus.currentUrl() != null) {
                                    thumbnailUrl = assetStatus.currentUrl();
                                }
                            }
                            
                            System.out.println("最终thumbnailUrl: " + thumbnailUrl);
                            System.out.println("==========================================\n");
                            log.info("道具[{}] 最终thumbnailUrl: {}", prop.getDisplayName(), thumbnailUrl);
                            props.add(new BoundPropVO(
                                    binding.getId(),
                                    prop.getId(),
                                    prop.getDisplayName(),
                                    thumbnailUrl
                            ));
                        }
                    }
                }
            }
        }

        // 查询分镜图资产状态
        AssetStatusVO shotImageStatus = getAssetStatus(shot.getId(), "SHOT", "SHOT_IMG");
        
        // 查询视频资产状态
        AssetStatusVO videoStatus = getAssetStatus(shot.getId(), "SHOT", "VIDEO");
        
        // 返回ShotVO
        return new ShotVO(
                shot.getId(),
                shot.getShotNo(),
                shot.getScriptText(),
                characters,
                scene,
                props,
                shotImageStatus,
                videoStatus,
                shot.getCreatedAt(),
                shot.getUpdatedAt()
        );
    }
    
    /**
     * 查询资产状态
     * 
     * @param ownerId 归属对象ID
     * @param ownerType 归属对象类型
     * @param assetType 资产类型
     * @return 资产状态VO
     */
    private AssetStatusVO getAssetStatus(Long ownerId, String ownerType, String assetType) {
        // 查询资产（按创建时间降序，取最新的一条）
        LambdaQueryWrapper<Asset> assetQuery = new LambdaQueryWrapper<>();
        assetQuery.eq(Asset::getOwnerId, ownerId)
                .eq(Asset::getOwnerType, ownerType)
                .eq(Asset::getAssetType, assetType)
                .orderByDesc(Asset::getCreatedAt)
                .last("LIMIT 1");
        Asset asset = assetMapper.selectOne(assetQuery);
        
        if (asset == null) {
            return createEmptyAssetStatus();
        }
        
        // 查询当前版本
        LambdaQueryWrapper<AssetVersion> versionQuery = new LambdaQueryWrapper<>();
        versionQuery.eq(AssetVersion::getAssetId, asset.getId())
                .orderByDesc(AssetVersion::getVersionNo)
                .last("LIMIT 1");
        AssetVersion currentVersion = assetVersionMapper.selectOne(versionQuery);
        
        if (currentVersion == null) {
            return createEmptyAssetStatus();
        }
        
        // 查询总版本数
        LambdaQueryWrapper<AssetVersion> countQuery = new LambdaQueryWrapper<>();
        countQuery.eq(AssetVersion::getAssetId, asset.getId());
        long totalVersions = assetVersionMapper.selectCount(countQuery);
        
        // 构建资产状态VO
        return new AssetStatusVO(
                asset.getId(),
                currentVersion.getId(),
                currentVersion.getUrl(),
                currentVersion.getStatus(),
                (int) totalVersions
        );
    }

    /**
     * 创建空的资产状态VO
     *
     * @return 空资产状态VO
     */
    private AssetStatusVO createEmptyAssetStatus() {
        return new AssetStatusVO(null, null, null, "NONE", 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShotImageAsset(Long userId, Long projectId, Long shotId) {
        log.info("删除分镜图资产: userId={}, projectId={}, shotId={}", userId, projectId, shotId);

        // 1. 查询分镜是否存在且未删除
        StoryboardShot shot = shotMapper.selectById(shotId);
        if (shot == null || shot.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "分镜不存在");
        }

        // 2. 验证分镜属于指定项目
        if (!shot.getProjectId().equals(projectId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED, "无权限访问该分镜");
        }

        // 3. 验证项目属于当前用户
        validateProjectOwnership(userId, projectId);

        // 4. 查询分镜图资产
        LambdaQueryWrapper<Asset> assetQuery = new LambdaQueryWrapper<>();
        assetQuery.eq(Asset::getOwnerId, shotId)
                .eq(Asset::getOwnerType, "SHOT")
                .eq(Asset::getAssetType, "SHOT_IMG")
                .orderByDesc(Asset::getCreatedAt)
                .last("LIMIT 1");

        Asset asset = assetMapper.selectOne(assetQuery);
        if (asset == null) {
            log.info("分镜图资产不存在，无需删除: shotId={}", shotId);
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "分镜图资产不存在");
        }

        // 5. 删除资产的所有版本
        LambdaQueryWrapper<AssetVersion> versionQuery = new LambdaQueryWrapper<>();
        versionQuery.eq(AssetVersion::getAssetId, asset.getId());
        int versionCount = assetVersionMapper.delete(versionQuery);

        // 6. 删除资产
        int assetResult = assetMapper.deleteById(asset.getId());

        if (assetResult <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "资产删除失败");
        }

        log.info("分镜图资产删除成功: shotId={}, assetId={}, 删除了{}个版本", 
                shotId, asset.getId(), versionCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVideoAsset(Long userId, Long projectId, Long shotId) {
        log.info("删除视频资产: userId={}, projectId={}, shotId={}", userId, projectId, shotId);

        // 1. 查询分镜是否存在且未删除
        StoryboardShot shot = shotMapper.selectById(shotId);
        if (shot == null || shot.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "分镜不存在");
        }

        // 2. 验证分镜属于指定项目
        if (!shot.getProjectId().equals(projectId)) {
            throw new BusinessException(ResultCode.ACCESS_DENIED, "无权限访问该分镜");
        }

        // 3. 验证项目属于当前用户
        validateProjectOwnership(userId, projectId);

        // 4. 查询视频资产
        LambdaQueryWrapper<Asset> assetQuery = new LambdaQueryWrapper<>();
        assetQuery.eq(Asset::getOwnerId, shotId)
                .eq(Asset::getOwnerType, "SHOT")
                .eq(Asset::getAssetType, "VIDEO")
                .orderByDesc(Asset::getCreatedAt)
                .last("LIMIT 1");

        Asset asset = assetMapper.selectOne(assetQuery);
        if (asset == null) {
            log.info("视频资产不存在，无需删除: shotId={}", shotId);
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "视频资产不存在");
        }

        // 5. 删除资产的所有版本
        LambdaQueryWrapper<AssetVersion> versionQuery = new LambdaQueryWrapper<>();
        versionQuery.eq(AssetVersion::getAssetId, asset.getId());
        int versionCount = assetVersionMapper.delete(versionQuery);

        // 6. 删除资产
        int assetResult = assetMapper.deleteById(asset.getId());

        if (assetResult <= 0) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "资产删除失败");
        }

        log.info("视频资产删除成功: shotId={}, assetId={}, 删除了{}个版本", 
                shotId, asset.getId(), versionCount);
    }
}
