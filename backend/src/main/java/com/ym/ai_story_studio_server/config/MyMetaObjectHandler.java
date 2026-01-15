// {{CODE-Cycle-Integration:
//   Task_ID: [#T001]
//   Timestamp: 2025-12-26T17:30:00+08:00
//   Phase: D-Develop
//   Context-Analysis: "Analyzed User.java entity, AuthServiceImpl login flow. Root cause: missing MetaObjectHandler for auto-filling timestamps."
//   Principle_Applied: "Verification-Mindset-Loop, KISS, SOLID"
// }}
// {{START_MODIFICATIONS}}
package com.ym.ai_story_studio_server.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 元数据处理器
 * <p>自动填充实体类中的 createdAt 和 updatedAt 字段
 *
 * @author Roo (Prometheus)
 * @since 1.0.0
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作时自动填充
     * <p>填充字段: createdAt, updatedAt
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");

        LocalDateTime now = LocalDateTime.now();

        // 填充创建时间
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);

        // 填充更新时间
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);

        log.debug("插入填充完成: createdAt={}, updatedAt={}", now, now);
    }

    /**
     * 更新操作时自动填充
     * <p>填充字段: updatedAt
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");

        LocalDateTime now = LocalDateTime.now();

        // 强制填充更新时间（覆盖已有值）
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, now);

        // 如果 strictUpdateFill 不生效，尝试直接设置
        if (metaObject.getValue("updatedAt") == null) {
            this.fillStrategy(metaObject, "updatedAt", now);
        } else {
            // 强制覆盖 updatedAt 字段
            metaObject.setValue("updatedAt", now);
        }

        log.debug("更新填充完成: updatedAt={}", now);
    }
}
// {{END_MODIFICATIONS}}
