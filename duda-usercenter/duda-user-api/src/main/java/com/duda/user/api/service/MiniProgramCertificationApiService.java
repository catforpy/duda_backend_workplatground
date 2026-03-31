package com.duda.user.api.service;

import com.duda.user.dto.miniprogram.MiniProgramCertificationDTO;

import java.util.List;

/**
 * 小程序认证API服务接口
 *
 * @author DudaNexus
 * @since 2026-03-27
 */
public interface MiniProgramCertificationApiService {

    MiniProgramCertificationDTO getCertificationById(Long id);

    List<MiniProgramCertificationDTO> listCertificationsByTenantId(Long tenantId);

    MiniProgramCertificationDTO getCertificationByMiniProgramId(Long miniProgramId);

    List<MiniProgramCertificationDTO> listCertificationsByStatus(Long tenantId, String certificationStatus);

    MiniProgramCertificationDTO createCertification(MiniProgramCertificationDTO certificationDTO);

    void updateCertification(MiniProgramCertificationDTO certificationDTO);

    void deleteCertification(Long id);
}
