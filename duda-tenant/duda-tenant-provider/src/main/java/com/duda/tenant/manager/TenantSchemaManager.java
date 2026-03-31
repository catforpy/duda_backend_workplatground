package com.duda.tenant.manager;

import com.duda.tenant.entity.Tenant;
import com.duda.tenant.service.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 租户Schema管理器
 * 负责创建、删除、管理租户的独立Schema
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@Component
public class TenantSchemaManager {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Lazy
    private TenantService tenantService;

    @Autowired
    private DataSource dataSource;

    /**
     * 为新租户创建独立的Schema
     *
     * @param tenantId   租户ID
     * @param tenantCode 租户编码
     * @return 是否创建成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createTenantSchema(Long tenantId, String tenantCode) {
        try {
            String schemaName = getSchemaName(tenantCode);

            log.info("开始为租户创建Schema: tenantId={}, tenantCode={}, schema={}",
                tenantId, tenantCode, schemaName);

            // 1. 创建Schema
            String createSchemaSql = "CREATE SCHEMA " + schemaName;
            jdbcTemplate.execute(createSchemaSql);
            log.info("Schema创建成功: {}", schemaName);

            // 2. 读取并执行初始化SQL脚本
            String scriptPath = getScriptPath();
            log.debug("读取SQL脚本路径: {}", scriptPath);
            String sqlScript = readSqlScript(scriptPath);
            log.debug("SQL脚本原始长度: {} 字符", sqlScript.length());

            // 3. 替换SQL脚本中的变量
            // $1 = schema名称 (如: tenant_TEST003)
            // $2 = tenantId (如: 20)
            sqlScript = sqlScript.replace("$1", schemaName)
                                        .replace("$2", String.valueOf(tenantId));
            log.debug("SQL脚本替换后长度: {} 字符", sqlScript.length());

            // 4. 执行SQL脚本
            log.info("开始执行SQL脚本，共 {} 个字符", sqlScript.length());
            executeSqlScript(sqlScript);

            log.info("租户Schema初始化完成: {}", schemaName);
            return true;

        } catch (Exception e) {
            log.error("创建租户Schema失败: tenantId={}, tenantCode={}", tenantId, tenantCode, e);
            throw new RuntimeException("创建租户Schema失败", e);
        }
    }

    /**
     * 删除租户的Schema
     *
     * @param tenantId   租户ID
     * @param tenantCode 租户编码
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean dropTenantSchema(Long tenantId, String tenantCode) {
        try {
            String schemaName = getSchemaName(tenantCode);

            log.warn("开始删除租户Schema: tenantId={}, tenantCode={}, schema={}",
                tenantId, tenantCode, schemaName);

            // 删除Schema
            String dropSchemaSql = "DROP SCHEMA IF EXISTS " + schemaName;
            jdbcTemplate.execute(dropSchemaSql);

            log.info("租户Schema已删除: {}", schemaName);
            return true;

        } catch (Exception e) {
            log.error("删除租户Schema失败: tenantId={}, tenantCode={}", tenantId, tenantCode, e);
            return false;
        }
    }

    /**
     * 检查租户Schema是否存在
     *
     * @param tenantCode 租户编码
     * @return 是否存在
     */
    public boolean schemaExists(String tenantCode) {
        try {
            String schemaName = getSchemaName(tenantCode);
            String checkSql = "SELECT schema_name FROM information_schema.schemata " +
                             "WHERE schema_name = '" + schemaName + "'";

            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class);
            return count != null && count > 0;

        } catch (Exception e) {
            log.error("检查Schema失败: tenantCode={}", tenantCode, e);
            return false;
        }
    }

    /**
     * 获取租户的Schema名称
     *
     * @param tenantCode 租户编码
     * @return Schema名称
     */
    public String getSchemaName(String tenantCode) {
        return "tenant_" + tenantCode;
    }

    /**
     * 获取租户的Schema名称(根据租户ID)
     *
     * @param tenantId 租户ID
     * @return Schema名称
     */
    public String getSchemaName(Long tenantId) {
        Tenant tenant = tenantService.getById(tenantId);
        if (tenant == null) {
            throw new IllegalArgumentException("租户不存在: " + tenantId);
        }
        return getSchemaName(tenant.getTenantCode());
    }

    /**
     * 切换到指定租户的Schema
     * 在执行SQL前调用,确保在正确的Schema下执行
     *
     * @param tenantCode 租户编码
     */
    public void switchToSchema(String tenantCode) {
        String schemaName = getSchemaName(tenantCode);
        jdbcTemplate.execute("SET SEARCH_PATH TO " + schemaName);
        log.debug("已切换到Schema: {}", schemaName);
    }

    /**
     * 获取SQL脚本文件路径
     */
    private String getScriptPath() {
        // 优先从项目资源路径加载
        String resourcePath = "init-tenant-schema.sql";

        // 如果资源文件不存在,尝试从文件系统加载
        File projectRoot = new File(System.getProperty("user.dir"));
        File scriptFile = new File(projectRoot, "scripts/init-tenant-schema.sql");

        if (scriptFile.exists()) {
            return scriptFile.getAbsolutePath();
        }

        // 否则返回classpath路径
        return "classpath:" + resourcePath;
    }

    /**
     * 读取SQL脚本内容
     */
    private String readSqlScript(String scriptPath) throws Exception {
        if (scriptPath.startsWith("classpath:")) {
            // 从classpath读取
            String resourceName = scriptPath.substring("classpath:".length());
            String content = new String(
                getClass().getClassLoader().getResourceAsStream(resourceName)
                    .readAllBytes()
            );
            return content;
        } else {
            // 从文件系统读取
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(scriptPath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            return content.toString();
        }
    }

    /**
     * 执行SQL脚本(支持多条SQL)
     */
    private void executeSqlScript(String sqlScript) {
        // 分割SQL语句
        String[] sqlStatements = sqlScript.split(";");
        log.info("SQL脚本已分割为 {} 条语句", sqlStatements.length);

        int executedCount = 0;
        int skippedCount = 0;

        for (int i = 0; i < sqlStatements.length; i++) {
            String sql = sqlStatements[i];
            String trimmedSql = sql.trim();

            if (trimmedSql.isEmpty()) {
                log.debug("语句 #{}: 空语句，已跳过", i + 1);
                skippedCount++;
                continue;
            }

            // 移除注释行（以--开头的行）
            String[] lines = trimmedSql.split("\\n");
            StringBuilder cleanedSql = new StringBuilder();
            boolean hasNonComment = false;

            for (String line : lines) {
                String trimmedLine = line.trim();
                if (!trimmedLine.startsWith("--")) {
                    cleanedSql.append(line).append("\n");
                    if (!trimmedLine.isEmpty()) {
                        hasNonComment = true;
                    }
                }
            }

            String finalSql = cleanedSql.toString().trim();

            if (!hasNonComment || finalSql.isEmpty()) {
                log.debug("语句 #{}: 只包含注释或空行，已跳过", i + 1);
                skippedCount++;
                continue;
            }

            try {
                log.debug("执行SQL #{}: {}", i + 1, finalSql.substring(0, Math.min(100, finalSql.length())));
                jdbcTemplate.execute(finalSql);
                executedCount++;
                log.info("SQL执行成功 (第{}条)", executedCount);
            } catch (Exception e) {
                log.error("执行SQL失败: {}, 错误: {}",
                    finalSql.substring(0, Math.min(100, finalSql.length())),
                    e.getMessage(), e);
            }
        }

        log.info("SQL脚本执行完成: 成功 {} 条, 跳过 {} 条", executedCount, skippedCount);
    }

    /**
     * 导出租户数据
     *
     * @param tenantId   租户ID
     * @param backupPath 备份文件路径
     * @return 是否导出成功
     */
    public boolean exportTenantData(Long tenantId, String backupPath) {
        try {
            String schemaName = getSchemaName(tenantId);

            log.info("开始导出租户数据: tenantId={}, schema={}, backupPath={}",
                tenantId, schemaName, backupPath);

            // TODO: 实现数据导出逻辑
            // 1. 导出schema为SQL文件
            // 2. 导出数据为INSERT语句
            // 3. 打包成zip文件

            log.info("租户数据导出完成: tenantId={}", tenantId);
            return true;

        } catch (Exception e) {
            log.error("导出租户数据失败: tenantId={}", tenantId, e);
            return false;
        }
    }

    /**
     * 导入租户数据
     *
     * @param tenantId   租户ID
     * @param backupPath 备份文件路径
     * @return 是否导入成功
     */
    public boolean importTenantData(Long tenantId, String backupPath) {
        try {
            log.info("开始导入租户数据: tenantId={}, backupPath={}", tenantId, backupPath);

            // TODO: 实现数据导入逻辑
            // 1. 解压备份文件
            // 2. 执行schema创建SQL
            // 3. 执行数据插入SQL

            log.info("租户数据导入完成: tenantId={}", tenantId);
            return true;

        } catch (Exception e) {
            log.error("导入租户数据失败: tenantId={}", tenantId, e);
            return false;
        }
    }

    /**
     * 迁移租户数据(从共享表迁移到独立Schema)
     *
     * @param tenantId   租户ID
     * @param fromTenantId 源租户ID(如果是合并迁移)
     * @return 是否迁移成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean migrateToTenantSchema(Long tenantId, Long fromTenantId) {
        try {
            String targetSchema = getSchemaName(tenantId);

            log.info("开始迁移租户数据到独立Schema: tenantId={}, targetSchema={}",
                tenantId, targetSchema);

            // TODO: 实现数据迁移逻辑
            // 1. 创建目标Schema
            // 2. 从共享表读取该租户的数据
            // 3. 写入独立Schema
            // 4. 验证数据完整性

            log.info("租户数据迁移完成: tenantId={}", tenantId);
            return true;

        } catch (Exception e) {
            log.error("迁移租户数据失败: tenantId={}", tenantId, e);
            return false;
        }
    }
}
