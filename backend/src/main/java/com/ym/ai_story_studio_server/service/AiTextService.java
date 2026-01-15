package com.ym.ai_story_studio_server.service;

import com.ym.ai_story_studio_server.client.VectorEngineClient;
import com.ym.ai_story_studio_server.common.ResultCode;
import com.ym.ai_story_studio_server.config.AiProperties;
import com.ym.ai_story_studio_server.dto.ai.TextGenerateRequest;
import com.ym.ai_story_studio_server.dto.ai.TextGenerateResponse;
import com.ym.ai_story_studio_server.entity.Job;
import com.ym.ai_story_studio_server.exception.BusinessException;
import com.ym.ai_story_studio_server.mapper.JobMapper;
import com.ym.ai_story_studio_server.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * AI文本生成服务
 *
 * <p>提供AI文本生成能力,支持多种大语言模型(LLM)
 *
 * <p><strong>核心功能:</strong>
 * <ul>
 *   <li>调用向量引擎API生成文本内容</li>
 *   <li>自动应用配置中的默认参数</li>
 *   <li>创建和管理任务记录</li>
 *   <li>自动进行<strong>固定积分计费</strong></li>
 *   <li>记录使用情况和费用明细</li>
 * </ul>
 *
 * <p><strong>支持的模型:</strong>
 * <ul>
 *   <li>gemini-3-pro-preview - Google Gemini大语言模型(默认)</li>
 * </ul>
 *
 * <p><strong>参数说明:</strong>
 * <ul>
 *   <li>maxTokens - <strong>系统内置最大值(4096)</strong>,前端无需传入</li>
 *   <li>temperature - 温度参数(0-1,值越大越随机,默认0.7)</li>
 *   <li>topP - 核采样参数(0-1,默认0.9)</li>
 * </ul>
 *
 * <p><strong>使用示例:</strong>
 * <pre>
 * // ✅ 新的请求格式（无需传入maxTokens）
 * TextGenerateRequest request = new TextGenerateRequest(
 *     "写一个科幻短故事,主角是一个AI",
 *     0.8,     // temperature
 *     0.95,    // topP
 *     1L       // projectId
 * );
 *
 * TextGenerateResponse response = aiTextService.generateText(request);
 * System.out.println("生成的文本: " + response.text());
 * System.out.println("消耗积分: " + response.costPoints());  // 固定扣费
 * </pre>
 *
 * <p><strong>计费规则:</strong>
 * <ul>
 *   <li>✅ <strong>固定扣费模式</strong>: 每次文本生成扣除固定积分,不按token数计费</li>
 *   <li>费用从用户积分钱包扣除,具体单价由pricing_rules表配置</li>
 *   <li>quantity始终为1,表示"一次文本生成"</li>
 * </ul>
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiTextService {

    private final VectorEngineClient vectorEngineClient;
    private final ChargingService chargingService;
    private final JobMapper jobMapper;
    private final AiProperties aiProperties;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    /**
     * 异步生成文本（工具箱使用）
     *
     * <p>立即返回jobId，在后台异步处理生成任务
     *
     * @param request 文本生成请求参数
     * @return 文本生成响应（仅包含jobId和model）
     */
    public TextGenerateResponse generateTextAsync(TextGenerateRequest request) {
        Long userId = UserContext.getUserId();
        log.info("异步文本生成任务提交 - userId: {}, promptLength: {}",
                userId, request.prompt().length());

        // 1. 应用默认配置
        AiProperties.Text textConfig = aiProperties.getText();
        String model = textConfig.getModel();

        // 2. 创建任务记录
        Job job = createJob(userId, request.projectId(), model, request.prompt());
        log.info("Job created - jobId: {}, jobType: TEXT_GENERATION", job.getId());

        // 3. 异步执行生成
        executeTextGenerationAsync(job.getId(), request);

        // 4. 立即返回jobId
        return new TextGenerateResponse(
                null,  // text将在异步生成后写入Job
                null,  // tokensUsed
                model,
                null,  // costPoints将在生成完成后计算
                job.getId()  // 返回jobId
        );
    }

    /**
     * 异步执行文本生成任务
     */
    @org.springframework.scheduling.annotation.Async("taskExecutor")
    public void executeTextGenerationAsync(Long jobId, TextGenerateRequest request) {
        try {
            // 从数据库加载Job
            Job job = jobMapper.selectById(jobId);
            if (job == null) {
                log.error("任务不存在 - jobId: {}", jobId);
                return;
            }

            // 设置用户上下文
            UserContext.setUserId(job.getUserId());

            // 应用默认配置
            AiProperties.Text textConfig = aiProperties.getText();
            String model = textConfig.getModel();
            Integer maxTokens = textConfig.getMaxTokens();
            Double temperature = request.temperature() != null ? request.temperature() : textConfig.getTemperature();
            Double topP = request.topP() != null ? request.topP() : textConfig.getTopP();

            log.info("开始异步生成文本 - jobId: {}", jobId);

            // ✅ 步骤1: 先进行积分扣费检查(预扣费)
            Map<String, Object> metaData = new HashMap<>();
            metaData.put("model", model);
            metaData.put("maxTokens", maxTokens);
            metaData.put("temperature", temperature);
            metaData.put("topP", topP);
            metaData.put("prompt", request.prompt());

            ChargingService.ChargingResult chargingResult = chargingService.charge(
                    ChargingService.ChargingRequest.builder()
                            .jobId(job.getId())
                            .bizType("TEXT_GENERATION")
                            .modelCode(model)
                            .quantity(1)
                            .metaData(metaData)
                            .build()
            );

            log.info("预扣费完成 - jobId: {}, cost: {}, balanceAfter: {}", 
                    jobId, chargingResult.getTotalCost(), chargingResult.getBalanceAfter());

            // ✅ 步骤2: 调用向量引擎API生成文本
            VectorEngineClient.TextApiResponse apiResponse = vectorEngineClient.generateText(
                    request.prompt(),
                    model,
                    maxTokens,
                    temperature,
                    topP
            );

            // 提取生成的文本
            String generatedText = apiResponse.choices() == null || apiResponse.choices().isEmpty()
                    ? ""
                    : apiResponse.choices().get(0).message().content();

            // 处理token使用量
            Integer tokensUsed = 0;
            if (apiResponse.usage() != null && apiResponse.usage().totalTokens() != null) {
                tokensUsed = apiResponse.usage().totalTokens();
            }

            log.info("文本生成成功 - jobId: {}, tokensUsed: {}", jobId, tokensUsed);

            // ✅ 步骤3: 更新任务状态为成功
            updateJobSuccess(job, model, request.prompt(), generatedText, tokensUsed);

            log.info("异步文本生成完成 - jobId: {}, cost: {}", jobId, chargingResult.getTotalCost());

        } catch (Exception e) {
            log.error("异步文本生成失败 - jobId: {}", jobId, e);
            Job job = jobMapper.selectById(jobId);
            if (job != null) {
                UserContext.setUserId(job.getUserId());
                updateJobFailed(job, e.getMessage());
            }
        } finally {
            UserContext.clear();
        }
    }

    /**
     * 生成文本
     *
     * <p>调用大语言模型生成文本内容,完整流程包括:
     * <ol>
     *   <li>参数验证和默认值应用</li>
     *   <li>创建任务记录</li>
     *   <li>调用向量引擎API生成文本</li>
     *   <li>进行<strong>固定积分计费</strong>(每次生成扣除固定积分,不按token数计费)</li>
     *   <li>更新任务状态为成功</li>
     *   <li>返回生成结果</li>
     * </ol>
     *
     * <p><strong>计费模式变更说明:</strong>
     * <ul>
     *   <li>✅ <strong>新模式(固定扣费)</strong>: 每次文本生成扣除固定积分,quantity=1</li>
     *   <li>❌ <strong>旧模式(按token)</strong>: 按实际或估算token数计费,已废弃</li>
     * </ul>
     *
     * <p><strong>maxTokens参数说明:</strong>
     * <ul>
     *   <li>maxTokens已改为系统内置,固定使用配置文件中的最大值(默认4096)</li>
     *   <li>前端无需传入此参数,由后端统一控制</li>
     * </ul>
     *
     * <p><strong>注意事项:</strong>
     * <ul>
     *   <li>该方法使用@Transactional确保任务创建和计费的原子性</li>
     *   <li>如果API调用失败,任务状态会更新为FAILED</li>
     *   <li>积分余额不足会抛出异常,不会创建任务</li>
     * </ul>
     *
     * @param request 文本生成请求参数
     * @return 文本生成响应(包含生成的文本、消耗token数、费用等)
     * @throws BusinessException 当API调用失败、余额不足或参数错误时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public TextGenerateResponse generateText(TextGenerateRequest request) {
        Long userId = UserContext.getUserId();
        log.info("Starting text generation - userId: {}, promptLength: {}",
                userId, request.prompt().length());

        // 1. 应用默认配置
        AiProperties.Text textConfig = aiProperties.getText();
        String model = textConfig.getModel();
        // ✅ maxTokens改为系统内置,固定使用配置文件最大值,前端不再传入
        Integer maxTokens = textConfig.getMaxTokens();
        Double temperature = request.temperature() != null ? request.temperature() : textConfig.getTemperature();
        Double topP = request.topP() != null ? request.topP() : textConfig.getTopP();

        log.debug("Applied config - model: {}, maxTokens: {}, temperature: {}, topP: {}",
                model, maxTokens, temperature, topP);

        // 2. 创建任务记录 (✅ 修复: 传入 prompt)
        Job job = createJob(userId, request.projectId(), model, request.prompt());
        log.info("Job created - jobId: {}, jobType: TEXT_GENERATION", job.getId());

        try {
            // 3. 进行积分计费(预扣费) - ✅ 先扣费后生成
            log.debug("Starting charging process (pre-charge, fixed pricing mode: quantity=1)...");
            Map<String, Object> chargingMetaData = new HashMap<>();
            chargingMetaData.put("model", model);
            chargingMetaData.put("maxTokens", maxTokens);
            chargingMetaData.put("temperature", temperature);
            chargingMetaData.put("topP", topP);
            chargingMetaData.put("prompt", request.prompt());

            ChargingService.ChargingResult chargingResult = chargingService.charge(
                    ChargingService.ChargingRequest.builder()
                            .jobId(job.getId())
                            .bizType("TEXT_GENERATION")
                            .modelCode(model)
                            .quantity(1)  // ✅ 固定值1,表示"一次文本生成",实现固定扣费
                            .metaData(chargingMetaData)
                            .build()
            );

            log.info("Pre-charging completed - jobId: {}, cost: {} points (fixed pricing), balanceAfter: {}",
                    job.getId(), chargingResult.getTotalCost(), chargingResult.getBalanceAfter());

            // 4. 调用向量引擎API生成文本
            log.debug("Calling VectorEngineClient.generateText...");
            VectorEngineClient.TextApiResponse apiResponse = vectorEngineClient.generateText(
                    request.prompt(),
                    model,
                    maxTokens,
                    temperature,
                    topP
            );

            // 调试：打印完整API响应用于诊断
            log.debug("API Response - id: {}, model: {}, usage: {}, choices: {}",
                    apiResponse.id(), apiResponse.model(), apiResponse.usage(), apiResponse.choices());

            // 提取生成的文本
            String generatedText = apiResponse.choices() == null || apiResponse.choices().isEmpty()
                    ? ""
                    : apiResponse.choices().get(0).message().content();

            // 处理token使用量（仅用于日志和返回，不影响计费）
            Integer tokensUsed = 0;
            if (apiResponse.usage() != null && apiResponse.usage().totalTokens() != null) {
                tokensUsed = apiResponse.usage().totalTokens();
            } else if (apiResponse.usage() != null) {
                // 尝试使用promptTokens + completionTokens
                Integer pt = apiResponse.usage().promptTokens();
                Integer ct = apiResponse.usage().completionTokens();
                if (pt != null && ct != null) {
                    tokensUsed = pt + ct;
                }
            }

            // 如果无法获取实际token数,使用0作为占位符(不影响计费)
            if (tokensUsed == 0 || tokensUsed == null) {
                log.warn("Unable to get actual token usage from API, setting to 0 for logging. usage={}",
                        apiResponse.usage());
                tokensUsed = 0;
            }

            log.info("Text generation succeeded - jobId: {}, tokensUsed: {}, textLength: {}",
                    job.getId(), tokensUsed, generatedText.length());

            // 5. 更新任务状态为成功 (✅ 修复: 传入 model, prompt, text)
            updateJobSuccess(job, model, request.prompt(), generatedText, tokensUsed);

            // 6. 返回响应
            return new TextGenerateResponse(
                    generatedText,
                    tokensUsed,
                    model,
                    chargingResult.getTotalCost(),
                    null  // 同步生成不返回jobId
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // 任务失败,更新任务状态
            log.error("Text generation failed - jobId: {}", job.getId(), e);
            updateJobFailed(job, e.getMessage());
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "文本生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建任务记录
     *
     * @param userId 用户ID
     * @param projectId 项目ID
     * @param model 使用的模型
     * @param prompt 用户提示词
     * @return 任务对象
     */
    private Job createJob(Long userId, Long projectId, String model, String prompt) {
        Job job = new Job();
        job.setUserId(userId);
        // 工具箱功能时projectId为null,使用0表示"不关联项目"
        job.setProjectId(projectId != null ? projectId : 0L);
        job.setJobType("TEXT_GENERATION");
        job.setStatus("RUNNING");
        job.setProgress(0);
        job.setTotalItems(1);
        job.setDoneItems(0);

        // ✅ 修复: 使用 ObjectMapper 安全地序列化 JSON (自动处理特殊字符转义)
        try {
            Map<String, Object> metaData = new HashMap<>();
            metaData.put("model", model);
            metaData.put("prompt", prompt);
            job.setMetaJson(objectMapper.writeValueAsString(metaData));
        } catch (Exception e) {
            log.error("Failed to serialize meta_json", e);
            job.setMetaJson(String.format("{\"model\":\"%s\",\"prompt\":\"\"}", model));
        }

        jobMapper.insert(job);
        return job;
    }

    /**
     * 更新任务状态为成功
     *
     * @param job 任务对象
     * @param model 使用的模型
     * @param prompt 用户提示词
     * @param text 生成的文本内容
     * @param tokensUsed 使用的token数
     */
    private void updateJobSuccess(Job job, String model, String prompt, String text, Integer tokensUsed) {
        job.setStatus("SUCCEEDED");
        job.setProgress(100);
        job.setDoneItems(1);

        // ✅ 修复: 使用 ObjectMapper 安全地序列化 JSON
        try {
            Map<String, Object> metaData = new HashMap<>();
            metaData.put("model", model);
            metaData.put("prompt", prompt);
            metaData.put("text", text);
            metaData.put("tokensUsed", tokensUsed);
            job.setMetaJson(objectMapper.writeValueAsString(metaData));
        } catch (Exception e) {
            log.error("Failed to serialize meta_json", e);
        }

        jobMapper.updateById(job);
    }

    /**
     * 更新任务状态为失败
     *
     * @param job 任务对象
     * @param errorMessage 错误信息
     */
    private void updateJobFailed(Job job, String errorMessage) {
        job.setStatus("FAILED");
        job.setErrorMessage(errorMessage);
        jobMapper.updateById(job);
    }
}
