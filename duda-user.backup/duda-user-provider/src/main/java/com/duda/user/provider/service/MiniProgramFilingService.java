package com.duda.user.provider.service;

import com.duda.user.dto.MiniProgramFilingDTO;

import java.util.List;

/**
 * 小程序备案Service接口
 */
public interface MiniProgramFilingService {

    MiniProgramFilingDTO getFilingById(Long id);

    List<MiniProgramFilingDTO> listFilingsByTenantId(Long tenantId);

    MiniProgramFilingDTO getFilingByMiniProgram(Long tenantId, Long miniProgramId);

    List<MiniProgramFilingDTO> listFilingsByStatus(Long tenantId, String status);

    MiniProgramFilingDTO createFiling(MiniProgramFilingDTO dto);

    void updateFiling(MiniProgramFilingDTO dto);

    void deleteFiling(Long id);
}
