package com.duda.tenant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duda.tenant.entity.TenantConfig;
import com.duda.tenant.mapper.TenantConfigMapper;
import com.duda.tenant.service.TenantConfigHistoryService;
import com.duda.tenant.service.TenantConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 租户配置服务实现
 *
 * @author Claude Code
 * @since 2026-03-28
 */
@Slf4j
@Service
public class TenantConfigServiceImpl extends ServiceImpl<TenantConfigMapper, TenantConfig> implements TenantConfigService {

    @Autowired
    private TenantConfigHistoryService tenantConfigHistoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantConfig createConfig(TenantConfig config) {
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        if (config.getIsEnabled() == null) {
            config.setIsEnabled(1);
        }
        if (config.getVersion() == null) {
            config.setVersion(0);
        }
        save(config);
        log.info("创建租户配置成功: configId={}, tenantId={}, configKey={}",
                config.getId(), config.getTenantId(), config.getConfigKey());
        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantConfig updateConfig(TenantConfig config) {
        TenantConfig oldConfig = getById(config.getId());
        if (oldConfig == null) {
            log.warn("更新租户配置失败，配置不存在: configId={}", config.getId());
            return null;
        }

        // 记录变更历史
        // tenantConfigHistoryService.recordHistory(oldConfig, config, "update");

        config.setUpdateTime(LocalDateTime.now());
        config.setVersion(oldConfig.getVersion() + 1);
        updateById(config);
        log.info("更新租户配置成功: configId={}, configKey={}", config.getId(), config.getConfigKey());
        return config;
    }

    @Override
    public TenantConfig getByTenantIdAndKey(Long tenantId, String configKey) {
        LambdaQueryWrapper<TenantConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantConfig::getTenantId, tenantId)
                .eq(TenantConfig::getConfigKey, configKey);
        return getOne(wrapper);
    }

    @Override
    public List<TenantConfig> listByTenantId(Long tenantId) {
        LambdaQueryWrapper<TenantConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantConfig::getTenantId, tenantId);
        return list(wrapper);
    }

    @Override
    public Map<String, String> getConfigMapByTenantId(Long tenantId) {
        List<TenantConfig> configs = listByTenantId(tenantId);
        return configs.stream()
                .filter(config -> config.getIsEnabled() != null && config.getIsEnabled() == 1)
                .collect(Collectors.toMap(
                        TenantConfig::getConfigKey,
                        TenantConfig::getConfigValue
                ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleConfig(Long configId, Boolean enabled) {
        TenantConfig config = getById(configId);
        if (config == null) {
            log.warn("启用/禁用租户配置失败，配置不存在: configId={}", configId);
            return false;
        }
        config.setIsEnabled(enabled ? 1 : 0);
        config.setUpdateTime(LocalDateTime.now());
        updateById(config);
        log.info("启用/禁用租户配置成功: configId={}, enabled={}", configId, enabled);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteConfig(Long configId) {
        boolean result = removeById(configId);
        if (result) {
            log.info("删除租户配置成功: configId={}", configId);
        } else {
            log.warn("删除租户配置失败，配置不存在: configId={}", configId);
        }
        return result;
    }
}
