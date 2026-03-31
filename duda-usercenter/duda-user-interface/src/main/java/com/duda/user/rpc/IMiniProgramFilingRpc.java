package com.duda.user.rpc;

import com.duda.user.dto.miniprogram.MiniProgramFilingDTO;

import java.util.List;

/**
 * 小程序备案RPC接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
public interface IMiniProgramFilingRpc {

    /**
     * 根据ID查询备案信息
     *
     * @param id 备案ID
     * @return 备案信息
     */
    MiniProgramFilingDTO getFilingById(Long id);

    /**
     * 根据租户ID查询备案列表
     *
     * @param tenantId 租户ID
     * @return 备案列表
     */
    List<MiniProgramFilingDTO> listFilingsByTenantId(Long tenantId);

    /**
     * 根据小程序ID查询备案
     *
     * @param miniProgramId 小程序ID
     * @return 备案信息
     */
    MiniProgramFilingDTO getFilingByMiniProgramId(Long miniProgramId);

    /**
     * 根据状态查询备案列表
     *
     * @param tenantId 租户ID
     * @param filingStatus 备案状态
     * @return 备案列表
     */
    List<MiniProgramFilingDTO> listFilingsByStatus(Long tenantId, String filingStatus);

    /**
     * 创建备案信息
     *
     * @param filingDTO 备案信息
     * @return 创建的备案信息
     */
    MiniProgramFilingDTO createFiling(MiniProgramFilingDTO filingDTO);

    /**
     * 更新备案信息
     *
     * @param filingDTO 备案信息
     */
    void updateFiling(MiniProgramFilingDTO filingDTO);

    /**
     * 删除备案信息
     *
     * @param id 备案ID
     */
    void deleteFiling(Long id);
}
