package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.entity.TenantConfigHistory;

/**
 * 租户配置变更历史服务接口
 *
 * @author Claude Code
 * @since 2026-03-28
 */
public interface TenantConfigHistoryService extends IService<TenantConfigHistory> {

    /**
     * 记录配置变更历史
     *
     * @param oldConfig 旧配置
     * @param newConfig 新配置
     * @param operationType 操作类型
     * @return 历史记录
     */
    TenantConfigHistory recordHistory(Object oldConfig, Object newConfig, String operationType);
}
