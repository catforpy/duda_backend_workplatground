package com.duda.user.provider.service;

import com.duda.user.dto.MiniProgramCertificationDTO;

import java.util.List;

/**
 * 小程序认证Service接口
 */
public interface MiniProgramCertificationService {

    MiniProgramCertificationDTO getCertificationById(Long id);

    List<MiniProgramCertificationDTO> listCertificationsByTenantId(Long tenantId);

    MiniProgramCertificationDTO getCertificationByMiniProgram(Long tenantId, Long miniProgramId);

    List<MiniProgramCertificationDTO> listCertificationsByStatus(Long tenantId, String status);

    MiniProgramCertificationDTO createCertification(MiniProgramCertificationDTO dto);

    void updateCertification(MiniProgramCertificationDTO dto);

    void deleteCertification(Long id);
}
