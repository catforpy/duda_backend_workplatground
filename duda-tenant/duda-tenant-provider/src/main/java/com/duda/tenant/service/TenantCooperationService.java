package com.duda.tenant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duda.tenant.api.dto.TenantCooperationDTO;
import com.duda.tenant.entity.TenantCooperation;

import java.util.List;

/**
 * 合作管理表服务接口
 *
 * @author Claude Code
 * @since 2026-03-30
 */
public interface TenantCooperationService extends IService<TenantCooperation> {

    /**
     * 创建合作
     */
    TenantCooperation createCooperation(TenantCooperationDTO dto);

    /**
     * 更新合作
     */
    TenantCooperation updateCooperation(TenantCooperationDTO dto);

    /**
     * 根据ID查询DTO
     */
    TenantCooperationDTO getCooperationDTO(Long id);

    /**
     * 根据合作编码查询
     */
    TenantCooperationDTO getByCooperationCode(String cooperationCode);

    /**
     * 查询租户的所有合作
     */
    List<TenantCooperationDTO> listByTenant(Long tenantId);

    /**
     * 暂停合作
     */
    Boolean suspend(Long id);

    /**
     * 激活合作
     */
    Boolean activate(Long id);

    /**
     * 终止合作
     */
    Boolean terminate(Long id);
}
