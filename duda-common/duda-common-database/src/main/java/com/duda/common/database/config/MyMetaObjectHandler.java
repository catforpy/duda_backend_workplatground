package com.duda.common.database.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充配置
 *
 * 自动填充：
 * - createTime：创建时自动填充
 * - updateTime：创建和更新时自动填充
 * - version：创建时自动填充
 *
 * @author DudaNexus
 * @since 2026-03-10
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");

        LocalDateTime now = LocalDateTime.now();

        // 填充创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);

        // 填充更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);

        // 填充版本号
        this.strictInsertFill(metaObject, "version", Integer.class, 0);
    }

    /**
     * 更新时自动填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");

        // 填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
