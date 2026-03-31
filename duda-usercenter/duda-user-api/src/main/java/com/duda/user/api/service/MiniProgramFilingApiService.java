package com.duda.user.api.service;

import com.duda.user.dto.miniprogram.MiniProgramFilingDTO;

import java.util.List;

/**
 * 小程序备案API服务接口
 */
public interface MiniProgramFilingApiService {

    MiniProgramFilingDTO getFilingById(Long id);

    List<MiniProgramFilingDTO> listFilingsByTenantId(Long tenantId);

    MiniProgramFilingDTO getFilingByMiniProgramId(Long miniProgramId);

    List<MiniProgramFilingDTO> listFilingsByStatus(Long tenantId, String filingStatus);

    MiniProgramFilingDTO createFiling(MiniProgramFilingDTO filingDTO);

    void updateFiling(MiniProgramFilingDTO filingDTO);

    void deleteFiling(Long id);
}
