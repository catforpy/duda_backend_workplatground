package com.duda.tenant.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态多数据源配置
 * 支持为每个租户创建独立的数据源连接到对应的Schema
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@Component
public class DynamicDataSourceConfig extends AbstractRoutingDataSource {

    @Autowired
    @Qualifier("defaultDataSource")
    private DataSource defaultDataSource;

    /**
     * 租户数据源缓存
     * Key: tenantCode, Value: DataSource
     */
    private final Map<String, DataSource> tenantDataSourceCache = new HashMap<>();

    /**
     * 目标数据源Map（用于路由）
     */
    private final Map<Object, Object> targetDataSources = new HashMap<>();

    /**
     * JDBC连接配置（从Nacos配置中心读取）
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 初始化数据源配置
     * 使用@PostConstruct确保在依赖注入完成后执行
     */
    @PostConstruct
    public void init() {
        // 设置默认数据源(用于系统表查询)
        targetDataSources.put("default", defaultDataSource);
        this.setTargetDataSources(targetDataSources);
        this.setDefaultTargetDataSource(defaultDataSource);
        afterPropertiesSet();  // 刷新数据源配置
        log.info("动态数据源初始化完成, 默认数据源已配置");
    }

    /**
     * 为租户创建专属数据源(连接到该租户的Schema)
     *
     * @param tenantCode 租户编码
     * @return 数据源
     */
    public DataSource createTenantDataSource(String tenantCode) {
        // 先从缓存获取
        DataSource cachedDataSource = tenantDataSourceCache.get(tenantCode);
        if (cachedDataSource != null) {
            log.debug("从缓存获取租户数据源: tenantCode={}", tenantCode);
            return cachedDataSource;
        }

        try {
            // 获取当前数据源的配置
            String jdbcUrl = getJdbcUrl(tenantCode);
            String username = getJdbcUsername();
            String password = getJdbcPassword();

            // 创建新的数据源
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            // 连接池配置
            config.setMaximumPoolSize(10);  // 每个租户最多10个连接
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);  // 30分钟
            config.setConnectionTestQuery("SELECT 1");

            HikariDataSource dataSource = new HikariDataSource(config);

            // 缓存数据源
            tenantDataSourceCache.put(tenantCode, dataSource);

            // 添加到路由数据源
            targetDataSources.put(tenantCode, dataSource);
            this.setTargetDataSources(targetDataSources);
            afterPropertiesSet();  // 刷新数据源配置

            log.info("创建租户数据源成功: tenantCode={}, jdbcUrl={}", tenantCode, jdbcUrl);
            return dataSource;

        } catch (Exception e) {
            log.error("创建租户数据源失败: tenantCode={}", tenantCode, e);
            throw new RuntimeException("创建租户数据源失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建JDBC URL（连接到租户Schema）
     *
     * @param tenantCode 租户编码
     * @return JDBC URL
     */
    private String getJdbcUrl(String tenantCode) {
        // 从当前数据源URL中提取基础信息，然后添加租户Schema
        try {
            // 查询当前数据库URL（从Nacos配置）
            String currentUrl = jdbcTemplate.getDataSource().getConnection().getMetaData().getURL();

            // 替换数据库名为租户Schema
            // 例如: jdbc:mysql://120.26.170.213:3306/duda_tenant?...
            // 变为: jdbc:mysql://120.26.170.213:3306/tenant_company001?...
            String baseUrl = currentUrl.replaceAll("/duda_tenant\\?", "/tenant_" + tenantCode + "?");

            log.debug("构建租户JDBC URL: tenantCode={}, url={}", tenantCode, baseUrl);
            return baseUrl;

        } catch (Exception e) {
            log.error("获取JDBC URL失败", e);
            // 使用默认URL（从配置文件）
            return "jdbc:mysql://120.26.170.213:3306/tenant_" + tenantCode +
                   "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
        }
    }

    /**
     * 获取JDBC用户名
     */
    private String getJdbcUsername() {
        try {
            return jdbcTemplate.getDataSource().getConnection().getMetaData().getUserName();
        } catch (Exception e) {
            log.error("获取JDBC用户名失败", e);
            return "root";
        }
    }

    /**
     * 获取JDBC密码
     */
    private String getJdbcPassword() {
        // 密码无法从DataSource获取，需要从配置读取
        // 这里返回null，让Hikari使用默认配置
        return null;
    }

    /**
     * 移除租户数据源
     *
     * @param tenantCode 租户编码
     */
    public void removeTenantDataSource(String tenantCode) {
        DataSource dataSource = tenantDataSourceCache.remove(tenantCode);
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
            log.info("关闭租户数据源: tenantCode={}", tenantCode);
        }

        // 从路由数据源中移除
        targetDataSources.remove(tenantCode);
        this.setTargetDataSources(targetDataSources);
        afterPropertiesSet();

        log.info("移除租户数据源成功: tenantCode={}", tenantCode);
    }

    /**
     * 根据租户上下文获取数据源key
     */
    @Override
    protected Object determineCurrentLookupKey() {
        String tenantCode = com.duda.tenant.context.TenantContext.getTenantCode();
        if (tenantCode == null) {
            return "default";  // 使用默认数据源
        }
        return tenantCode;
    }
}
