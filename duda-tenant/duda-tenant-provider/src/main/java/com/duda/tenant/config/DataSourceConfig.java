package com.duda.tenant.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源配置
 * 创建默认数据源用于系统表查询
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    /**
     * 默认数据源(用于系统表查询)
     * 从Nacos配置中心读取spring.datasource.*配置
     */
    @Bean(name = "defaultDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource defaultDataSource() {
        log.info("初始化默认数据源...");

        HikariConfig config = new HikariConfig();

        // 从Nacos配置中读取数据库连接信息
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // 连接池配置
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setConnectionTestQuery("SELECT 1");

        HikariDataSource dataSource = new HikariDataSource(config);
        log.info("默认数据源初始化成功: jdbcUrl={}", jdbcUrl);

        return dataSource;
    }
}
