package com.duda.user.service.miniprogram;

import com.duda.user.dto.miniprogram.MiniProgramCertificationDTO;

import java.util.List;

/**
 * 小程序认证服务接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
public interface MiniProgramCertificationService {

    /**
     * 根据ID查询认证信息（带缓存）
     *
     * @param id 认证ID
     * @return 认证信息
     */
    MiniProgramCertificationDTO getCertificationById(Long id);

    /**
     * 根据租户ID查询认证列表（带缓存）
     *
     * @param tenantId 租户ID
     * @return 认证列表
     */
    List<MiniProgramCertificationDTO> listCertificationsByTenantId(Long tenantId);

    /**
     * 根据小程序ID查询认证信息（带缓存）
     *
     * @param miniProgramId 小程序ID
     * @return 认证信息
     */
    MiniProgramCertificationDTO getCertificationByMiniProgramId(Long miniProgramId);

    /**
     * 根据状态查询认证列表（带缓存）
     *
     * @param tenantId 租户ID
     * @param certificationStatus 认证状态
     * @return 认证列表
     */
    List<MiniProgramCertificationDTO> listCertificationsByStatus(Long tenantId, String certificationStatus);

    /**
     * 创建认证信息
     *
     * @param certificationDTO 认证信息
     * @return 创建的认证信息
     */
    MiniProgramCertificationDTO createCertification(MiniProgramCertificationDTO certificationDTO);

    /**
     * 更新认证信息
     *
     * @param certificationDTO 认证信息
     */
    void updateCertification(MiniProgramCertificationDTO certificationDTO);

    /**
     * 删除认证信息
     *
     * @param id 认证ID
     */
    void deleteCertification(Long id);
}
